#!/bin/sh
# Create a virtual environment and install requirements
set -e

if [ -z "$( ls -A ".venv" )" ]; then
  python3 -m venv .venv
fi

if [ -d .venv/bin ]; then
  # shellcheck disable=SC1091
  . ".venv/bin/activate"
else
  # For Windows users using bash
  # shellcheck disable=SC1091
  . ".venv/Scripts/activate"
fi
python -m pip install --upgrade pip
pip install -r requirements.txt
