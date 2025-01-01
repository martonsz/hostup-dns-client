from pydantic import BaseModel, ConfigDict
from typing import List


class Zone(BaseModel):
    model_config = ConfigDict(extra="allow")
    domain_id: int
    name: str
    service_id: int


class List_DNS(BaseModel):
    model_config = ConfigDict(extra="allow")
    service_ids: List[int]
    zones: List[Zone]
