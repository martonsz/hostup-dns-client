#!/usr/bin/env python3
import json
import logging
import os
import argparse
import pprint
from app_config import AppConfig
from client import HostUpClient
from logging.handlers import RotatingFileHandler
from model.dns import DnsRecordPayload
from version import __version__

logger = logging.getLogger(__name__)
log_level = os.getenv("LOG_LEVEL", "INFO")
log_config_file_path = os.path.join(os.path.dirname(__file__), "logging_config.ini")


def setup_default_logger() -> logging.Logger:
    logger = logging.getLogger()
    logger.setLevel(log_level)
    formatter = logging.Formatter(
        "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    )

    # Console handler
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    # File handler
    file_handler = RotatingFileHandler(
        "app.log", maxBytes=10 * 1024 * 1024, backupCount=5
    )
    file_handler.setFormatter(formatter)

    # Adding handlers to the root logger
    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

    return logger


def setup_logger_from_config(config_file) -> None:
    if os.path.exists(config_file):
        ext = os.path.splitext(config_file)[1].lower()
        if ext == ".ini":
            logging.config.fileConfig(config_file)
        elif ext in [".json"]:
            with open(config_file, "r") as f:
                config = json.load(f)
                logging.config.dictConfig(config)
        else:
            raise ValueError(f"Unsupported log configuration file format: {ext}")
    else:
        setup_default_logger()


def main():
    setup_logger_from_config(log_config_file_path)

    args = setup_argparse()
    client = HostUpClient(AppConfig(args.username, args.password, args.api_endpoint))

    if args.zones:
        zones = client.get_zones()
        logger.info(pprint.pformat(zones.model_dump()))
        return

    if args.action == "present":
        logger.info(
            f"Adding DNS record for domain '{args.domain}' with value '{args.value}'"
        )
        response = client.add_record_by_name(
            DnsRecordPayload(
                name=args.domain, ttl=300, priority=10, type="TXT", content=args.value
            ),
            delete_existing=True,
        )
        logger.info(f"Response: {response}")
    elif args.action == "cleanup":
        logger.info(
            f"Deleting DNS record for domain '{args.domain}'"
        )
        response = client.delete_record_by_name(args.domain)
        logger.info(f"Response: {response}")


def setup_argparse() -> None:
    parser = argparse.ArgumentParser(
        description="Parse command-line arguments for DNS record management"
    )
    # Define the positional arguments
    parser.add_argument(
        "action",
        nargs="?",
        choices=["present", "cleanup"],
        help="The action to perform: 'present' or 'cleanup'",
    )
    parser.add_argument(
        "domain",
        nargs="?",
        help="The fully-qualified domain name (e.g., '_acme-challenge.my.example.org.')",
    )
    parser.add_argument(
        "value",
        nargs="?",
        help="The value for the DNS record (e.g., 'MsijOYZxqyjGnFGwhjrhfg-Xgbl5r68WPda0J9EgqqI')",
    )

    # Define the optional arguments
    parser.add_argument("-u", "--username", help="Username for authentication")
    parser.add_argument("-p", "--password", help="Password for authentication")
    parser.add_argument("-a", "--api-endpoint", help="API endpoint URL")
    parser.add_argument("-j", "--jwt-path", help="File path for storing JWT")
    parser.add_argument("-c", "--config", help="Configuration file path")
    parser.add_argument("-z", "--zones", action="store_true", help="List zones")
    parser.add_argument('--version', action='version', version=f"hostup-dns-client {__version__}")

    args = parser.parse_args()

    if not args.zones:
        if not args.action or not args.domain or not args.value:
            parser.error("The 'action', 'domain', and 'value' arguments are required")

    return args

if __name__ == "__main__":
    main()
