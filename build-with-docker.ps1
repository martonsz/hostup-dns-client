# Extract the version from src/version.py
$version = Get-Content src/version.py | Select-String '__version__' | ForEach-Object {
    ($_ -match '__version__ = "(.*)"') | Out-Null
    $matches[1]
}

# Run the Docker command
docker run --rm -ti `
    -w /workdir `
    -v "${PWD}:/workdir" `
    python:3-alpine `
    sh -c "apk add binutils && pip install -r requirements.txt && pyinstaller --onefile --name hostup-dns-client-${version} src/main.py"
