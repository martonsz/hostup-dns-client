import requests
from app_config import AppConfig
from model.authentication import Authentication
from model.list_dns import List_DNS, Zone
from typing import List


class ToManyRequestsException(Exception):
    """Rate limit reached"""

    pass


class HostUpClient:
    def __init__(self, config: AppConfig) -> None:
        self.config = config

    def list_dns(self) -> List[str]:
        auth = self._get_jwt()
        headers = {"Authorization": f"Bearer {auth.token}"}

        response = requests.get(f"{self.config.api_endpoint}/dns", headers=headers)
        if response.status_code == 429:
            raise ToManyRequestsException(
                f"To many requests. Response from API: {response.text}"
            )
        if response.status_code == 200 and "json" in response.headers["Content-Type"]:
            return List_DNS.model_validate_json(response.text)

    def _get_jwt(self) -> Authentication:
        # TODO get cached jwt if it has not expired
        # authentication = self._get_cached_jwt()
        return self._authenticate()
        # TODO store new JWT somhwere
        return jwt

    def _authenticate(self) -> Authentication:
        payload = {"username": self.config.username, "password": self.config.password}
        response = requests.post(f"{self.config.api_endpoint}/login", data=payload)
        if response.status_code == 429:
            raise ToManyRequestsException(
                f"To many requests. Response from API: {response.text}"
            )
        if response.status_code == 200 and "json" in response.headers["Content-Type"]:
            return Authentication.model_validate_json(response.text)
        else:
            raise Exception(
                f"Failed to login. Status code: {response.status_code}. Response from API: {response.text}"
            )


if __name__ == "__main__":
    config = AppConfig()
    host_up_client = HostUpClient(config)
    list_dns = host_up_client.list_dns()
    print(list_dns)
