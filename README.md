# hostup-dns-client

A CLI tool for updating DNS records via the Hostup.se API, specifically designed for managing DNS challenges in Let's
Encrypt certificate issuance. This tool can be seamlessly integrated as an External Program provider when using Traefik
to automate DNS-based ACME challenges and certificate generation.

## Usage

TODO

## Developing

To set up a development environment for the Hostup DNS Client, follow these steps:

### Requirements

- Built with java 25. I don't guarantee compatibility with older versions.
- GraalVM Native Image installed (if building a native image)

### Build Instructions

You can use a Docker container to build the project. Run the following command in the project root directory.  
It will create a native image binary in the `dist` folder.

```shell
./build-native-image.sh
```
