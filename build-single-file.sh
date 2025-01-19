#!/bin/sh
# Build single file executable with PyInstaller

version=$(grep "__version__" src/version.py | sed -E 's/__version__ = "(.*)"/\1/')
pyinstaller --onefile --name "hostup-dns-client-${version}" src/main.py
