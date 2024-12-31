import os
from configparser import ConfigParser


class AppConfigException(Exception):
    """Custom exception for configuration errors."""

    pass


class AppConfig:
    DEFAULT_API_ENDPOINT = "https://min.hostup.se/api"
    DEFAULT_CONFIG_FILE_PATH = os.path.join(os.path.dirname(__file__), "config.ini")

    CONFIG_FILE_PATH_ENV_NAME = "APP_CONFIG_FILE_PATH"
    USERNAME_ENV_NAME = "APP_USERNAME"
    PASSWORD_ENV_NAME = "APP_PASSWORD"
    API_ENDPOINT_ENV_NAME = "APP_API_ENDPOINT"

    def __init__(
        self, username: str = None, password: str = None, api_endpoint: str = None
    ) -> None:
        self._username = None
        self._password = None
        self._api_endpoint = None
        args_config = {
            "username": username,
            "password": password,
            "api_endpoint": api_endpoint,
        }
        self._args_config = {
            key: value for key, value in args_config.items() if value is not None
        }
        self.load_configuration()

    @property
    def username(self):
        return self._username

    @property
    def password(self):
        return self._password

    @property
    def api_endpoint(self):
        return self._api_endpoint

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
                file_path = env_file_path

        if not os.path.exists(file_path):
            return {}

        try:
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
