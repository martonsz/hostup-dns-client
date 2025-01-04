import unittest
import os
import tempfile
import jwt_store

from model.authentication import Authentication
from unittest.mock import Mock, patch
from pathlib import Path


class TestJWTStore(unittest.TestCase):

    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.file_path = Path(os.path.join(self.temp_dir.name, "jwt.json"))

    def tearDown(self):
        self.temp_dir.cleanup()

    @patch("model.authentication.Authentication._get_expiration", Mock(return_value=100))
    def test_save_jwt(self):
        authentication = Authentication(token="token", refresh="refresh")
        jwt_store.save_jwt(authentication, self.file_path)

        self.assertTrue(self.file_path.exists())

        with open(self.file_path, "r") as f:
            authentication_from_file = Authentication.model_validate_json(f.read())
            self.assertEqual(authentication, authentication_from_file)

    @patch("jwt_store.Authentication._get_expiration", Mock(return_value=jwt_store.EXPIRE_THRESHOLD))
    @patch("time.time", Mock(return_value=0))
    def test_load_jwt(self):
        token_str = """{"token":"token","refresh":"refresh"}"""
        if self.file_path.exists():
            self.file_path.unlink()
        with open(self.file_path, "w") as outfile:
            outfile.write(token_str)

        jwt_store.load_jwt(self.file_path)

        authentication = Authentication(token="token", refresh="refresh")
        with open(self.file_path, "r") as f:
            authentication_from_file = Authentication.model_validate_json(f.read())
            self.assertEqual(authentication, authentication_from_file)

    @patch("jwt_store.Authentication._get_expiration", Mock(return_value=jwt_store.EXPIRE_THRESHOLD))
    @patch("time.time", Mock(return_value=1))
    def test_load_jwt_expires_soon(self):
        token_str = """{"token":"token","refresh":"refresh"}"""
        if self.file_path.exists():
            self.file_path.unlink()
        with open(self.file_path, "w") as outfile:
            outfile.write(token_str)

        jwt_store.load_jwt(self.file_path)
        self.assertFalse(self.file_path.exists())

    def test_load_file_not_found(self):
        auth = jwt_store.load_jwt(self.file_path.joinpath('not_found'))
        self.assertIsNone(auth)

    @patch("model.authentication.Authentication._get_expiration", Mock(return_value=100))
    def test_save_file_exists(self):
        if self.file_path.exists():
            self.file_path.unlink()
        with open(self.file_path, "w") as outfile:
            outfile.write("text")
        authentication = Authentication(token="token", refresh="refresh")
        jwt_store.save_jwt(authentication, self.file_path)

        self.assertTrue(self.file_path.exists())

        with open(self.file_path, "r") as f:
            authentication_from_file = Authentication.model_validate_json(f.read())
            self.assertEqual(authentication, authentication_from_file)

if __name__ == "__main__":
    unittest.main()
