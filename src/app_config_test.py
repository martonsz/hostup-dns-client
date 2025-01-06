import os
import unittest
import test_util
from unittest.mock import patch, mock_open
from configparser import ConfigParser
from app_config import AppConfig, AppConfigException


class TestAppConfig(unittest.TestCase):

    def setUp(self):
        test_util.clear_env_variables()

    @patch.dict(
        os.environ,
        {
            AppConfig.CONFIG_FILE_PATH_ENV_NAME: __file__,
        },
    )
    @patch(
        "builtins.open",
        new_callable=mock_open,
        read_data="""
            [settings]
            username = testuser
            password = testpass
            api_endpoint = https://example.com/api
            """,
    )
    def test_parse_config_file(self, mock_file):
        config = AppConfig()
        self.assertEqual(config.username, "testuser")
        self.assertEqual(config.password, "testpass")
        self.assertEqual(config.api_endpoint, "https://example.com/api")

    @patch.dict(
        os.environ,
        {
            AppConfig.USERNAME_ENV_NAME: "",
            AppConfig.PASSWORD_ENV_NAME: "",
            AppConfig.CONFIG_FILE_PATH_ENV_NAME: __file__,
        },
    )
    @patch(
        "builtins.open",
        new_callable=mock_open,
        read_data="""
            [settings]
            username = testuser
            password = testpass
            api_endpoint = https://example.com/api
            """,
    )
    def test_parse_config_file_with_empty_environment_variable(self, mock_file):
        config = AppConfig()
        self.assertEqual(config.username, "testuser")
        self.assertEqual(config.password, "testpass")
        self.assertEqual(config.api_endpoint, "https://example.com/api")

    @patch.dict(
        os.environ,
        {
            AppConfig.USERNAME_ENV_NAME: "envuser",
            AppConfig.PASSWORD_ENV_NAME: "envpass",
            AppConfig.API_ENDPOINT_ENV_NAME: "https://env.api",
            "CONFIG_FILE_PATH": __file__,
        },
    )
    @patch(
        "builtins.open",
        new_callable=mock_open,
        read_data="""
            [settings]
            username = testuser
            password = testpass
            api_endpoint = https://example.com/api
            """,
    )
    def test_env_override(self, mock_file):
        config = AppConfig()
        self.assertEqual(config.username, "envuser")
        self.assertEqual(config.password, "envpass")
        self.assertEqual(config.api_endpoint, "https://env.api")

    @patch.dict(
        os.environ,
        {
            AppConfig.USERNAME_ENV_NAME: "envuser",
            AppConfig.PASSWORD_ENV_NAME: "envpass",
            AppConfig.API_ENDPOINT_ENV_NAME: "https://env.api",
            "CONFIG_FILE_PATH": __file__,
        },
    )
    @patch(
        "builtins.open",
        new_callable=mock_open,
        read_data="""
            [settings]
            username = testuser
            password = testpass
            api_endpoint = https://example.com/api
            """,
    )
    def test_args_override(self, mock_file):
        config = AppConfig(
            username="argsuser", password="argpass", api_endpoint="https://argsendpoint"
        )
        self.assertEqual(config.username, "argsuser")
        self.assertEqual(config.password, "argpass")
        self.assertEqual(config.api_endpoint, "https://argsendpoint")

    @patch.dict(
        os.environ,
        {
            AppConfig.USERNAME_ENV_NAME: "envuser",
            AppConfig.PASSWORD_ENV_NAME: "envpass",
            AppConfig.API_ENDPOINT_ENV_NAME: "https://env.api",
        },
    )
    def test_env(self):
        config = AppConfig()
        self.assertEqual(config.username, "envuser")
        self.assertEqual(config.password, "envpass")
        self.assertEqual(config.api_endpoint, "https://env.api")

    @patch.dict(
        os.environ,
        {
            AppConfig.PASSWORD_ENV_NAME: "envpass",
            AppConfig.CONFIG_FILE_PATH_ENV_NAME: __file__,
        },
    )
    @patch(
        "builtins.open",
        new_callable=mock_open,
        read_data="""
            [settings]
            username = testuser
            api_endpoint = https://example.com/api
            """,
    )
    def test_env_merge(self, mock_file):
        config = AppConfig()
        self.assertEqual(config.username, "testuser")
        self.assertEqual(config.password, "envpass")
        self.assertEqual(config.api_endpoint, "https://example.com/api")

    @patch.dict(
        os.environ,
        {AppConfig.PASSWORD_ENV_NAME: "envpass"},
    )
    def test_missing_username(self):
        with self.assertRaises(AppConfigException) as context:
            AppConfig()
        self.assertIn("Username is required", str(context.exception))

    @patch.dict(
        os.environ,
        {AppConfig.USERNAME_ENV_NAME: "envusername"},
    )
    def test_missing_password(self):
        with self.assertRaises(AppConfigException) as context:
            AppConfig()
        self.assertIn("Password is required", str(context.exception))

    @patch.dict(
        os.environ,
        {AppConfig.USERNAME_ENV_NAME: "", AppConfig.PASSWORD_ENV_NAME: "envpass"},
    )
    def test_empty_username(self):
        with self.assertRaises(AppConfigException) as context:
            AppConfig()
        self.assertIn("Username is required", str(context.exception))

    @patch.dict(
        os.environ,
        {AppConfig.USERNAME_ENV_NAME: "envusername", AppConfig.PASSWORD_ENV_NAME: ""},
    )
    def test_empty_password(self):
        with self.assertRaises(AppConfigException) as context:
            AppConfig()
        self.assertIn("Password is required", str(context.exception))

    def test_default_api_endpoint(self):
        config = AppConfig(username="user", password="pass")
        self.assertEqual(config.username, "user")
        self.assertEqual(config.password, "pass")
        self.assertEqual(config.api_endpoint, AppConfig.DEFAULT_API_ENDPOINT)


if __name__ == "__main__":
    unittest.main()
