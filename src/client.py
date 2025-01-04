import jwt_store

from app_config import AppConfig
from model.authentication import Authentication
from model.list_dns import List_DNS, Zone

from requests import Request, Response, Session
from typing import List


class ToManyRequestsException(Exception):
    """Rate limit reached"""

    pass


class HostUpClient:
    def __init__(self, config: AppConfig = AppConfig()) -> None:
        self.config = config

    def list_dns(self) -> List[str]:
        response = self._send_request("GET", "/dns")
        return List_DNS.model_validate_json(response.text)

    def _get_jwt(self) -> Authentication:
        authentication = jwt_store.load_jwt(self.config.authentication_file_path)
        if authentication:
            return authentication
        authentication = self._authenticate()
        jwt_store.save_jwt(authentication, self.config.authentication_file_path)
        return authentication

    def _authenticate(self) -> Authentication:
        payload = {"username": self.config.username, "password": self.config.password}
        response = self._send_request("POST", "/login", payload, False)
        return Authentication.model_validate_json(response.text)

    def _send_request(
        self, method: str, path: str, payload: dict = None, authenticate: bool = True
    ) -> Response:
        request = Request(method, f"{self.config.api_endpoint}{path}")
        if payload:
            request.data = payload
        if authenticate:
            authentication = self._get_jwt()
            request.headers["Authorization"] = f"Bearer {authentication.token}"
        session = Session()
        response = session.send(request.prepare())
        if response.status_code == 429:
            raise ToManyRequestsException(
                f"To many requests. Response from API: {response.text}"
            )
        if response.status_code == 200:
            return response
        else:
            raise Exception(
                f"Failed with request. Status code: {response.status_code}. Response from API: {response.text}"
            )


if __name__ == "__main__":
    host_up_client = HostUpClient()
    list_dns = host_up_client.list_dns()
    print(list_dns)
