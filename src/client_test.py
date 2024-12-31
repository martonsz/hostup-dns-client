import unittest
from unittest.mock import patch, Mock
from app_config import AppConfig
from client import HostUpClient, ToManyRequestsException, TokenException


class TestClient(unittest.TestCase):

    jwt_expires_year_5000 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk1NjE3NTg0MDAwfQ.Uv_CMsLoJLLv0l5dtHQ9k5kapg2BScOCXuJn3mH2jzY"
    epoch_year_5000 = 95617584000
    jwt_missing_exp = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    def setUp(self):
        self.config = AppConfig(username="username", password="password")

    @patch("requests.post")
    def test_authenticate(self, mock_post):
        mock_response = Mock()
        mock_response.json.return_value = {"token": self.jwt_expires_year_5000}
        mock_response.status_code = 200
        mock_response.headers = {"Content-Type": "application/json"}
        mock_post.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        jwt, expiration = host_up_client._authenticate()
        self.assertEqual(jwt, self.jwt_expires_year_5000)
        self.assertEqual(expiration, self.epoch_year_5000)


    @patch("requests.post")
    def test_rate_limit(self, mock_post):
        mock_response = Mock()
        mock_response.json.return_value = {"error": "To many requests"}
        mock_response.status_code = 429
        mock_response.headers = {"Content-Type": "application/json"}
        mock_post.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        with self.assertRaises(ToManyRequestsException) as context:
            host_up_client._authenticate()
        self.assertIn("To many requests", str(context.exception))

    @patch("requests.post")
    def test_rate_limit(self, mock_post):
        mock_response = Mock()
        mock_response.json.return_value = {"token": self.jwt_missing_exp}
        mock_response.status_code = 200
        mock_response.headers = {"Content-Type": "application/json"}
        mock_post.return_value = mock_response

        host_up_client = HostUpClient(self.config)
        with self.assertRaises(Exception) as context:
            host_up_client._authenticate()
        self.assertIn("To many requests", str(context.exception))
if __name__ == "__main__":
    unittest.main()