[![CI](https://github.com/martonsz/hostup-dns-client/actions/workflows/ci.yml/badge.svg)](https://github.com/martonsz/hostup-dns-client/actions/workflows/ci.yml)

# hostup-dns-client

A CLI tool for updating DNS records via the [Hostup.se](https://hostup.se) API, specifically designed for being
a [External Program](https://go-acme.github.io/lego/dns/exec/) DNS provider for [Lego](https://go-acme.github.io/lego/).

I needed this for automating DNS-01 challenges for Let's Encrypt certificates on
Traefik ([dnsChallenge](https://doc.traefik.io/traefik/reference/install-configuration/tls/certificate-resolvers/acme/#dnschallenge)).

## Usage

### Command line

```
Usage: hostup-dns-client
  -a --add-record <zoneId> <type> <domain> <value> <ttl> Add a DNS record
         Example: -a 10111 A foo.example.org 1.2.3.4 3600
         zoneId:  Zone ID for the domain. Use --list-zones to find the correct ID
         type:    A, TXT, CNAME, etc.
         domain:  e.g. "foo.example.org"
         value:   e.g. "1.2.3.4" for A record, or "some text" for TXT record
         ttl:     Time to live in seconds
  -b --base-uri <uri>                    Base URI for the Hostup API (optional, defaults to %s)
  -d --delete-domain <domain>            Removes *ALL* records (A, TXT, etc) for the matching domain. E.g. "foo.example.org"
  -D --delete-record <zoneId> <recordId> Remove a single record by its ID. Use --list-records to find the record ID.
  -k --api-key <key>                     API key for authentication
  -l --list-zones                        List all DNS zones associated with an account
  -r --list-records <zoneId>             Get DNS records for a domain zone
  -v --version
  -h --help

Positional mode for Lego (https://go-acme.github.io/lego/dns/exec/)
  hostup-dns-client <action> <domain> <value>
  action: present | cleanup
Example
  hostup-dns-client "present" "_acme-challenge.my.example.org." "MsijOYZxqyjGnFGwhjrhfg-Xgbl5r68WPda0J9EgqqI"

You can also use environment variables (required for LEGO mode):
  HOSTUP_DNS_CLIENT_API_KEY
  HOSTUP_DNS_CLIENT_BASE_URI (optional, defaults to https://cloud.hostup.se/api/)
```

### Traefik Configuration Example

Note: This is not a complete Traefik docker-compose configuration, just the relevant parts (environment.EXEC_PATH) for
using
the Hostup DNS Client as a DNS provider for ACME DNS-01 challenges.

```yaml
services:
  traefik:
    container_name: not-a-complete-compose-for-traefik
    image: traefik:v3
    ports:
      - 80:80
      - 443:443
    volumes:
      - ./scripts:/etc/traefik/scripts
      - ./config/traefik.yml:/etc/traefik/traefik.yml:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    environment:
      EXEC_PATH: /etc/traefik/scripts/hostup-dns-client # Path to the Hostup DNS Client binary
```

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

# TODO

- Implement Lego positional arguments
- Test native executable against a mock server
- Improve test coverage
