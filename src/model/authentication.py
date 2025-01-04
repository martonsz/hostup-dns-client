import base64
import json
import time

from pydantic import BaseModel, ConfigDict


class TokenException(Exception):
    """Token Exception"""

    pass


class Authentication(BaseModel):
    model_config = ConfigDict(strict=True, extra="allow")
    token: str
    refresh: str

    def model_post_init(self, __context):
        self.expiration = self._get_expiration()


    def _get_expiration(self) -> int:
        try:
            payload_b64 = self.token.split(".")[1]
        except IndexError as a:
            raise TokenException(f"Could not find payload in token: {self.token}, {a}") from a
    
        # Add padding if necessary
        payload_b64 = payload_b64 + "=" * (4 - len(payload_b64) % 4)
    
        decoded = base64.urlsafe_b64decode(payload_b64).decode("utf-8")
        jsonObj = json.loads(decoded)
        if "exp" in jsonObj:
            return jsonObj["exp"]
        else:
            raise TokenException(f"Could not find expiration in token: {self.token}")
    
    def seconds_until_expiration(self) -> int:
        return max(0, self.expiration - int(time.time()))
    
    def _time_until_epoch(self) -> str:
        current_time = int(time.time())
        time_left = max(0, self.expiration - current_time)
    
        if time_left == 0:
            return "Time has passed"
    
        days = time_left // 86400
        hours = (time_left % 86400) // 3600
        minutes = (time_left % 3600) // 60
        seconds = time_left % 60
    
        components = []
        if days > 0:
            components.append(f"{days} day{'s' if days > 1 else ''}")
        if hours > 0:
            components.append(f"{hours} hour{'s' if hours > 1 else ''}")
        if minutes > 0:
            components.append(f"{minutes} minute{'s' if minutes > 1 else ''}")
        if seconds > 0:
            components.append(f"{seconds} second{'s' if seconds > 1 else ''}")
    
        return ", ".join(components)
