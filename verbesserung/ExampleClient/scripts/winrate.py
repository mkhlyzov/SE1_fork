#!/usr/bin/env python3

import argparse
import math
import os
from pathlib import Path


def resolve_logs_dir(logs_dir):
    logs_path = Path(logs_dir)

    # If absolute path → use directly
    if logs_path.is_absolute():
        return logs_path

    # Otherwise interpret relative to project root
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent

    return project_root / logs_path


def parse_logs(log_dir):
    total_games = 0
    p1_wins = 0

    for path in Path(log_dir).glob("*.txt"):
        try:
            lines = path.read_text().strip().splitlines()
            if len(lines) < 2:
                continue

            winner_line = lines[-2]

            if "Player 1 WON!" in winner_line:
                p1_wins += 1
                total_games += 1
            elif "Player 2 WON!" in winner_line:
                total_games += 1
            else:
                print(f"Couldn't parse {path}")

        except Exception as e:
            print(f"Skipping {path}: {e}")

    return p1_wins, total_games


def winrate_confidence_interval(successes, n, confidence=0.95):
    if n == 0:
        return 0.0, (0.0, 0.0)

    p = successes / n

    # z-score for confidence level (95% default)
    z = 1.96
    if confidence != 0.95:
        # optional generalization
        from statistics import NormalDist
        z = NormalDist().inv_cdf(1 - (1 - confidence) / 2)

    z2 = z * z

    denom = 1 + z2 / n
    center = (p + z2 / (2 * n)) / denom
    half_width = z * math.sqrt((p * (1 - p) / n) + (z2 / (4 * n * n))) / denom

    lower = max(0.0, center - half_width)
    upper = min(1.0, center + half_width)

    return p, (lower, upper)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--logs_dir",
        default="logs/logs_algo",
        help="Directory containing log txt files"
    )
    args = parser.parse_args()

    logs_dir = resolve_logs_dir(args.logs_dir)

    p1_wins, total = parse_logs(logs_dir)
    winrate, (low, high) = winrate_confidence_interval(p1_wins, total)

    print(f"Logs directory: {logs_dir}")
    print(f"Total games: {total}")
    print(f"Player 1 wins: {p1_wins}")
    print(f"Winrate: {winrate:.4f}")
    print(f"95% CI: [{low:.4f}, {high:.4f}]")


if __name__ == "__main__":
    main()
