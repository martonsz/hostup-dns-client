import json
import unittest
import test_util

from client import HostUpClient, ToManyRequestsException
from app_config import AppConfig
from unittest.mock import patch, Mock
from model.authentication import Authentication, TokenException


class TestClientAuth(unittest.TestCase):

    jwt_expires_year_5000 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk1NjE3NTg0MDAwfQ.Uv_CMsLoJLLv0l5dtHQ9k5kapg2BScOCXuJn3mH2jzY"
    epoch_year_5000 = 95617584000
    jwt_missing_exp = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    def setUp(self):
        test_util.clear_env_variables()
        self.config = AppConfig(username="username", password="password")

    @patch("requests.Session.send")
    def test_authenticate(self, session_send_mock):
        mock_response = Mock()
        mock_response.text = json.dumps(
            {"token": self.jwt_expires_year_5000, "refresh": "ignore"}
        )
        mock_response.status_code = 200
        session_send_mock.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        authentication = host_up_client._authenticate()
        self.assertEqual(authentication.token, self.jwt_expires_year_5000)
        self.assertEqual(authentication.expiration, self.epoch_year_5000)

    @patch("requests.Session.send")
    def test_authenticate_missing_expiration(self, session_send_mock):
        mock_response = Mock()
        mock_response.text = json.dumps(
            {"token": self.jwt_missing_exp, "refresh": "ignore"}
        )
        mock_response.status_code = 200
        session_send_mock.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        with self.assertRaises(TokenException) as context:
            host_up_client._authenticate()
        self.assertIn("Could not find expiration in token", str(context.exception))

    @patch("requests.Session.send")
    def test_rate_limit(self, session_send_mock):
        mock_response = Mock()
        mock_response.text = json.dumps({"error": "To many requests"})
        mock_response.status_code = 429
        session_send_mock.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        with self.assertRaises(ToManyRequestsException) as context:
            host_up_client._authenticate()
        self.assertIn("To many requests", str(context.exception))


if __name__ == "__main__":
    unittest.main()
