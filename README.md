# hostup-dns-client

A Python tool for updating DNS records via the Hostup.se API, specifically designed for managing DNS challenges in Let's Encrypt certificate issuance. This tool can be seamlessly integrated as an External Program provider when using Traefik to automate DNS-based ACME challenges and certificate generation.

## Build

You can use docker compose to build this project.  
I chose compose it works on both Windows and Mac.  

The following will produce a single file application in `dist/hostup-dns-client-${version}`

```shell
docker compose up
```
