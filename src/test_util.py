import os
from app_config import AppConfig

def clear_env_variables() -> None:
    # Clear env variables that "accidentally" is set using .env file
    os.environ.pop(AppConfig.CONFIG_FILE_PATH_ENV_NAME, None)
    os.environ.pop(AppConfig.AUTHENTICATION_FILE_PATH_ENV_NAME, None)
    os.environ.pop(AppConfig.API_ENDPOINT_ENV_NAME, None)
    os.environ.pop(AppConfig.USERNAME_ENV_NAME, None)
    os.environ.pop(AppConfig.PASSWORD_ENV_NAME, None)
