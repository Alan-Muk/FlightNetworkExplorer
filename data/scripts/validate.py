import csv
from pathlib import Path

DATA = Path("../raw")

files = {
    "airports": DATA / "airports.dat",
    "airlines": DATA / "airlines.dat",
    "routes": DATA / "routes.dat",
}

for name, path in files.items():
    if not path.exists():
        print(f"Missing: {path}")
        continue

    with open(path, encoding="utf-8") as f:
        rows = sum(1 for _ in f)

    print(f"{name}: {rows:,} rows")
