#!/bin/bash

version=$(grep "__version__" src/version.py | sed -E 's/__version__ = "(.*)"/\1/')

docker run --rm -ti \
    -w /wordkir \
    -v "$(pwd)":/workdir \
    python:3-alpine \
        sh -c "pip install -r requirements &&pyinstaller --onefile --name hostup-dns-client-${version} src/main.py" 
