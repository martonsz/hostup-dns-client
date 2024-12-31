import base64
import json
import re
import requests
from app_config import AppConfig


class ToManyRequestsException(Exception):
    """Rate limit reached"""

    pass

class TokenException(Exception):
    """Token Exception"""

    pass

class HostUpClient:
    def __init__(self, config: AppConfig) -> None:
        self.config = config

    def _get_jwt(self) -> str:
        # TODO get cached jwt if it has not expired
        # jwt = self._get_cached_jwt()
        jwt, expiration = self._authenticate()
        # TODO store new JWT somhwere
        return jwt

    def _authenticate(self) -> tuple[str, int]:
        payload = {"username": self.config.username, "password": self.config.password}
        response = requests.post(f"{self.config.api_endpoint}/login", data=payload)
        if response.status_code == 429:
            raise ToManyRequestsException(
                f"To many requests. Response from API: {response.text}"
            )
        if response.status_code == 200 and "json" in response.headers["Content-Type"]:
            api_response = response.json()
            if "token" in api_response:
                return api_response["token"], _get_expiration(api_response["token"])
            else:
                raise Exception(f"Failed to login. Reponse from API: {response.text}")
        else:
            raise Exception(
                f"Failed to login. Status code: {response.status_code}. Response from API: {response.text}"
            )


def _get_expiration(jwt: str) -> int:
    payload_b64 = jwt.split(".")[1]

    # Add padding if necessary
    payload_b64 = payload_b64 + '=' * (4 - len(payload_b64) % 4)

    decoded = base64.urlsafe_b64decode(payload_b64).decode("utf-8")
    jsonObj = json.loads(decoded)
    if "exp" in jsonObj:
        return jsonObj["exp"]
    else:
        raise TokenException(f"Could not find expiration in JWT: {jwt}")


if __name__ == "__main__":
    config = AppConfig()
    host_up_client = HostUpClient(config)
    jwt = host_up_client._get_jwt()
    print(f"jwt: {jwt}")
