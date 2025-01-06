#!/usr/bin/env python3
import json
import logging
import os
from app_config import AppConfig
from client import HostUpClient
from model.dns import DnsRecordPayload

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
    file_handler = logging.FileHandler("app.log", mode="a")
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
    client = HostUpClient(AppConfig())

    #record = client.get_record_by_name("clienttest.marton.cloud")
    #logging.info(f"record: {record}")

    #r = client.delete_record_by_name("clienttest.marton.cloud")
    #logging.info(f"Deleted record: {r}")

    payload = DnsRecordPayload(
        name="clienttest.marton.cloud", ttl=3600, priority=10, type="TXT", content="first"
    )
    r = client.add_record_by_name(payload)
    print(r)


if __name__ == "__main__":
    main()
