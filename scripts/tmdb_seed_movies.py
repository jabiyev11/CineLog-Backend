#!/usr/bin/env python3
"""
Generate PostgreSQL seed SQL for the CineLog movie schema using TMDb data.

Usage:
  TMDB_API_KEY=... python3 scripts/tmdb_seed_movies.py --limit 25 > tmdb_movies.sql
  TMDB_API_KEY=... python3 scripts/tmdb_seed_movies.py --limit 50 --lists popular top_rated --output tmdb_movies.sql

TMDb attribution requirement:
  "This product uses the TMDB API but is not endorsed or certified by TMDB."
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
import urllib.parse
import urllib.request
from typing import Iterable


API_BASE = "https://api.themoviedb.org/3"
IMAGE_BASE = "https://image.tmdb.org/t/p"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate CineLog movie seed SQL from TMDb.")
    parser.add_argument("--limit", type=int, default=25, help="How many unique movies to include.")
    parser.add_argument(
        "--lists",
        nargs="+",
        default=["popular"],
        choices=["popular", "top_rated", "now_playing", "upcoming"],
        help="TMDb movie lists to pull from, in priority order.",
    )
    parser.add_argument("--language", default="en-US", help="TMDb language, default: en-US.")
    parser.add_argument("--max-cast", type=int, default=5, help="Max cast members per movie.")
    parser.add_argument("--max-images", type=int, default=4, help="Max extra images per movie.")
    parser.add_argument("--poster-size", default="w500", help="TMDb poster size, default: w500.")
    parser.add_argument("--backdrop-size", default="original", help="TMDb backdrop size, default: original.")
    parser.add_argument("--output", help="Optional output SQL file path.")
    return parser.parse_args()


def api_get(path: str, api_key: str, **params) -> dict:
    query = urllib.parse.urlencode({"api_key": api_key, **params})
    url = f"{API_BASE}{path}?{query}"
    request = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def sql_quote(value: str | None) -> str:
    if value is None:
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def build_image_url(size: str, file_path: str | None) -> str | None:
    if not file_path:
        return None
    return f"{IMAGE_BASE}/{size}{file_path}"


def trim_text(value: str | None, max_len: int) -> str | None:
    if value is None:
        return None
    compact = " ".join(value.split())
    return compact[:max_len]


def unique_preserve_order(values: Iterable[str]) -> list[str]:
    seen: set[str] = set()
    result: list[str] = []
    for value in values:
        if value and value not in seen:
            seen.add(value)
            result.append(value)
    return result


def fetch_movie_ids(api_key: str, lists: list[str], limit: int, language: str) -> list[int]:
    ids: list[int] = []
    seen: set[int] = set()
    page = 1

    while len(ids) < limit and page <= 10:
        for list_name in lists:
            payload = api_get(f"/movie/{list_name}", api_key, language=language, page=page)
            for item in payload.get("results", []):
                movie_id = item.get("id")
                if movie_id and movie_id not in seen:
                    seen.add(movie_id)
                    ids.append(movie_id)
                    if len(ids) >= limit:
                        return ids
        page += 1
        time.sleep(0.15)
    return ids


def fetch_movie_detail(
    api_key: str,
    movie_id: int,
    language: str,
    max_cast: int,
    max_images: int,
    poster_size: str,
    backdrop_size: str,
) -> dict | None:
    payload = api_get(
        f"/movie/{movie_id}",
        api_key,
        language=language,
        append_to_response="credits,images",
        include_image_language=f"{language[:2]},null",
    )

    poster_url = build_image_url(poster_size, payload.get("poster_path"))
    if not poster_url:
        return None

    crew = payload.get("credits", {}).get("crew", [])
    cast = payload.get("credits", {}).get("cast", [])
    images = payload.get("images", {}).get("backdrops", [])

    directors = unique_preserve_order(
        member.get("name", "").strip()
        for member in crew
        if member.get("job") == "Director" and member.get("name")
    )
    cast_members = unique_preserve_order(
        member.get("name", "").strip()
        for member in cast[:max_cast]
        if member.get("name")
    )
    genres = unique_preserve_order(
        genre.get("name", "").strip()
        for genre in payload.get("genres", [])
        if genre.get("name")
    )
    extra_images = unique_preserve_order(
        build_image_url(backdrop_size, image.get("file_path"))
        for image in images
        if image.get("file_path")
    )

    backdrop_url = build_image_url(backdrop_size, payload.get("backdrop_path"))
    if backdrop_url:
        extra_images = [img for img in extra_images if img != backdrop_url]

    return {
        "id": payload["id"],
        "title": trim_text(payload.get("title"), 255) or "Untitled",
        "release_year": int((payload.get("release_date") or "0000")[:4]),
        "duration_minutes": payload.get("runtime") or 1,
        "country": trim_text(
            next((c.get("name") for c in payload.get("production_countries", []) if c.get("name")), None),
            255,
        ),
        "language": trim_text(payload.get("spoken_languages", [{}])[0].get("english_name"), 255)
        if payload.get("spoken_languages")
        else trim_text(payload.get("original_language"), 255),
        "synopsis": trim_text(payload.get("overview"), 4000) or "Synopsis unavailable.",
        "poster_image_url": poster_url,
        "backdrop_image_url": backdrop_url,
        "directors": directors or ["Unknown"],
        "cast_members": cast_members,
        "genres": genres or ["Drama"],
        "image_urls": extra_images[:max_images],
    }


def generate_sql(movies: list[dict]) -> str:
    ids = ", ".join(str(movie["id"]) for movie in movies)
    lines: list[str] = []
    lines.append("BEGIN;")
    lines.append("")
    lines.append("-- This product uses the TMDB API but is not endorsed or certified by TMDB.")
    lines.append(f"-- Generated for {len(movies)} movies.")
    lines.append("")
    lines.append(f"DELETE FROM movie_image_urls WHERE movie_id IN ({ids});")
    lines.append(f"DELETE FROM movie_genres WHERE movie_id IN ({ids});")
    lines.append(f"DELETE FROM movie_cast_members WHERE movie_id IN ({ids});")
    lines.append(f"DELETE FROM movie_directors WHERE movie_id IN ({ids});")
    lines.append("")

    for movie in movies:
        lines.append(
            "INSERT INTO movies (id, title, release_year, duration_minutes, country, language, synopsis, poster_image_url, backdrop_image_url) "
            f"VALUES ({movie['id']}, {sql_quote(movie['title'])}, {movie['release_year']}, {movie['duration_minutes']}, "
            f"{sql_quote(movie['country'])}, {sql_quote(movie['language'])}, {sql_quote(movie['synopsis'])}, "
            f"{sql_quote(movie['poster_image_url'])}, {sql_quote(movie['backdrop_image_url'])}) "
            "ON CONFLICT (id) DO UPDATE SET "
            "title = EXCLUDED.title, "
            "release_year = EXCLUDED.release_year, "
            "duration_minutes = EXCLUDED.duration_minutes, "
            "country = EXCLUDED.country, "
            "language = EXCLUDED.language, "
            "synopsis = EXCLUDED.synopsis, "
            "poster_image_url = EXCLUDED.poster_image_url, "
            "backdrop_image_url = EXCLUDED.backdrop_image_url;"
        )

        for director in movie["directors"]:
            lines.append(
                f"INSERT INTO movie_directors (movie_id, director) VALUES ({movie['id']}, {sql_quote(director)});"
            )
        for cast_member in movie["cast_members"]:
            lines.append(
                f"INSERT INTO movie_cast_members (movie_id, cast_member) VALUES ({movie['id']}, {sql_quote(cast_member)});"
            )
        for genre in movie["genres"]:
            lines.append(
                f"INSERT INTO movie_genres (movie_id, genre) VALUES ({movie['id']}, {sql_quote(genre)});"
            )
        for image_url in movie["image_urls"]:
            lines.append(
                f"INSERT INTO movie_image_urls (movie_id, image_url) VALUES ({movie['id']}, {sql_quote(image_url)});"
            )
        lines.append("")

    lines.append("SELECT setval(pg_get_serial_sequence('movies', 'id'), (SELECT COALESCE(MAX(id), 1) FROM movies), true);")
    lines.append("COMMIT;")
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    api_key = os.getenv("TMDB_API_KEY")
    if not api_key:
        print("Missing TMDB_API_KEY environment variable.", file=sys.stderr)
        return 1

    movie_ids = fetch_movie_ids(api_key, args.lists, args.limit, args.language)
    if not movie_ids:
        print("No movies returned from TMDb.", file=sys.stderr)
        return 1

    movies: list[dict] = []
    for movie_id in movie_ids:
        try:
            movie = fetch_movie_detail(
                api_key=api_key,
                movie_id=movie_id,
                language=args.language,
                max_cast=args.max_cast,
                max_images=args.max_images,
                poster_size=args.poster_size,
                backdrop_size=args.backdrop_size,
            )
        except Exception as exc:  # pragma: no cover - CLI fallback
            print(f"Skipping movie {movie_id}: {exc}", file=sys.stderr)
            continue

        if movie is not None:
            movies.append(movie)
        time.sleep(0.15)

    if not movies:
        print("No usable movies were generated.", file=sys.stderr)
        return 1

    sql = generate_sql(movies)
    if args.output:
        with open(args.output, "w", encoding="utf-8") as output_file:
            output_file.write(sql)
    else:
        sys.stdout.write(sql)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
