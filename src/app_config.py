import os
import logging
from pathlib import Path
from configparser import ConfigParser

logger = logging.getLogger(__name__)

class AppConfigException(Exception):
    """Custom exception for configuration errors."""

    pass


class AppConfig:
    DEFAULT_API_ENDPOINT = "https://min.hostup.se/api"
    DEFAULT_CONFIG_FILE_PATH = Path(os.path.join(os.path.dirname(__file__), "config.ini"))
    DEFAULT_AUTHENTICATION_FILE_PATH = os.path.join(os.path.dirname(__file__), "authentication.json")

    CONFIG_FILE_PATH_ENV_NAME = "APP_CONFIG_FILE_PATH"
    AUTHENTICATION_FILE_PATH_ENV_NAME = "AUTHENTICATION_FILE_PATH"
    USERNAME_ENV_NAME = "APP_USERNAME"
    PASSWORD_ENV_NAME = "APP_PASSWORD"
    API_ENDPOINT_ENV_NAME = "APP_API_ENDPOINT"

    def __init__(
        self, username: str = None, password: str = None, api_endpoint: str = None, authentication_file_path: str = None
    ) -> None:
        self._username = None
        self._password = None
        self._api_endpoint = None
        self._authentication_file_path = None
        args_config = {
            "username": username,
            "password": password,
            "api_endpoint": api_endpoint,
            "authentication_file_path": authentication_file_path
        }
        self._args_config = {
            key: value for key, value in args_config.items() if value is not None
        }
        self.load_configuration()

    @property
    def username(self) -> str:
        return self._username

    @property
    def password(self) -> str:
        return self._password

    @property
    def api_endpoint(self) -> str:
        return self._api_endpoint

    @property
    def authentication_file_path(self) -> Path:
        return self._authentication_file_path

    def _validate(self, config):
        if not config.get("username"):
            raise AppConfigException("Username is required and cannot be empty.")
        if not config.get("password"):
            raise AppConfigException("Password is required and cannot be empty.")

    def _load_from_file(self):

        file_path = AppConfig.DEFAULT_CONFIG_FILE_PATH

        # Fail if user has explicitly configured a file path and the file does not exist
        env_file_path = os.getenv(AppConfig.CONFIG_FILE_PATH_ENV_NAME)
        if env_file_path:
            if not os.path.exists(env_file_path):
                raise AppConfigException(
                    f"Configuration file '{env_file_path}' does not exist."
                )
            else:
                file_path = Path(env_file_path)

        if not os.path.exists(file_path):
            return {}

        try:
            logging.debug(f"Reading configuration from file: {file_path}")
            parser = ConfigParser()
            parser.read(file_path)

            file_config = {
                "username": parser.get("settings", "username", fallback=None),
                "password": parser.get("settings", "password", fallback=None),
                "api_endpoint": parser.get("settings", "api_endpoint", fallback=None),
            }
            return {
                key: value for key, value in file_config.items() if value is not None and len(value) > 0
            }
        except Exception as e:
            raise AppConfigException(f"Failed to read configuration file: {e}")

    def _load_from_env(self):
        env_config = {
            "username": os.getenv(AppConfig.USERNAME_ENV_NAME),
            "password": os.getenv(AppConfig.PASSWORD_ENV_NAME),
            "api_endpoint": os.getenv(AppConfig.API_ENDPOINT_ENV_NAME),
        }
        return {key: value for key, value in env_config.items() if value is not None and len(value) > 0}

    def load_configuration(self):
        file_config = self._load_from_file()
        env_config = self._load_from_env()

        merged_config = {
            **file_config,
            **env_config,
            **self._args_config,
        }  # Env variables override file config, Args variables override Env variables

        self._validate(merged_config)

        self._username = merged_config["username"]
        self._password = merged_config["password"]
        self._api_endpoint = merged_config.get(
            "api_endpoint", AppConfig.DEFAULT_API_ENDPOINT
        )
        self._authentication_file_path = Path(merged_config.get(
            "authentication_file_path]", AppConfig.DEFAULT_AUTHENTICATION_FILE_PATH
        ))
