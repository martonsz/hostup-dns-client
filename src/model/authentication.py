import base64
import json
from pydantic import BaseModel, ConfigDict


class TokenException(Exception):
    """Token Exception"""

    pass


class Authentication(BaseModel):
    model_config = ConfigDict(strict=True, extra="allow")
    token: str
    refresh: str

    def model_post_init(self, __context):
        self.expiration = _get_expiration(self.token)


def _get_expiration(jwt: str) -> int:
    payload_b64 = jwt.split(".")[1]

    # Add padding if necessary
    payload_b64 = payload_b64 + "=" * (4 - len(payload_b64) % 4)

    decoded = base64.urlsafe_b64decode(payload_b64).decode("utf-8")
    jsonObj = json.loads(decoded)
    if "exp" in jsonObj:
        return jsonObj["exp"]
    else:
        raise TokenException(f"Could not find expiration in JWT: {jwt}")
