#!/usr/bin/env python3
"""Extract only the public QAIRT SDK build components used by Local Dream CI.

The official SDK download is large. Keeping only QNN headers, SampleApp,
Android backends and relevant DSP skels avoids filling GitHub-hosted runners.
No SDK files are checked into git, cached, or uploaded as standalone artifacts.
"""
from __future__ import annotations

import argparse
from pathlib import Path, PurePosixPath
import shutil
import sys
from zipfile import ZipFile

ARCHES = ("68", "69", "73", "75", "79", "81", "85")
ANDROID_LIBS = {"libQnnHtp.so", "libQnnSystem.so"} | {
    f"libQnnHtpV{arch}Stub.so" for arch in ARCHES
}
ROOT_FILES = {"LICENSE.pdf", "NOTICE.txt", "QNN_NOTICE.txt"}


def match_sdk_path(name: str, version: str) -> PurePosixPath | None:
    parts = PurePosixPath(name).parts
    for index in range(len(parts) - 1):
        if parts[index : index + 2] == ("qairt", version):
            relative = parts[index + 2 :]
            if relative and all(part not in (".", "..", "") for part in relative):
                return PurePosixPath(*relative)
    return None


def wanted(relative: PurePosixPath) -> bool:
    parts = relative.parts
    if relative.as_posix() in ROOT_FILES:
        return True
    if parts[:2] == ("include", "QNN"):
        return True
    if parts[:4] == ("examples", "QNN", "SampleApp", "SampleApp"):
        return True
    if len(parts) == 3 and parts[:2] == ("lib", "aarch64-android"):
        return parts[2] in ANDROID_LIBS
    if len(parts) == 4 and parts[0] == "lib" and parts[2] == "unsigned":
        arch = parts[1].removeprefix("hexagon-v")
        return arch in ARCHES and parts[3] in {
            f"libQnnHtpV{arch}.so",
            f"libQnnHtpV{arch}Skel.so",
        }
    return False


def extract(archive: Path, destination: Path, version: str) -> None:
    count = 0
    with ZipFile(archive) as zf:
        for entry in zf.infolist():
            relative = match_sdk_path(entry.filename, version)
            if relative is None or not wanted(relative) or entry.is_dir():
                continue
            out = destination.joinpath(*relative.parts)
            out.parent.mkdir(parents=True, exist_ok=True)
            with zf.open(entry) as source, out.open("wb") as target:
                shutil.copyfileobj(source, target, 1024 * 1024)
            count += 1

    required = [
        "LICENSE.pdf",
        "NOTICE.txt",
        "QNN_NOTICE.txt",
        "include/QNN/QnnInterface.h",
        "include/QNN/HTP/QnnHtpDevice.h",
        "examples/QNN/SampleApp/SampleApp/src/QnnSampleApp.cpp",
        "lib/aarch64-android/libQnnHtp.so",
        "lib/aarch64-android/libQnnSystem.so",
    ]
    for arch in ARCHES:
        required.extend([
            f"lib/aarch64-android/libQnnHtpV{arch}Stub.so",
            f"lib/hexagon-v{arch}/unsigned/libQnnHtpV{arch}.so",
            f"lib/hexagon-v{arch}/unsigned/libQnnHtpV{arch}Skel.so",
        ])
    missing = [name for name in required if not (destination / name).is_file()]
    if missing:
        raise RuntimeError(
            f"QAIRT {version} archive incomplete or changed layout:\n"
            + "\n".join(missing)
        )
    print(f"Extracted {count} QAIRT {version} build files to {destination}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--archive", type=Path, required=True)
    parser.add_argument("--destination", type=Path, required=True)
    parser.add_argument("--sdk-version", required=True)
    args = parser.parse_args()
    try:
        extract(args.archive, args.destination, args.sdk_version)
    except Exception as exc:
        print(f"Error extracting QAIRT: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
