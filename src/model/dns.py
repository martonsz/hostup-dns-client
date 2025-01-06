from pydantic import BaseModel, ConfigDict
from typing import List, Union


class DnsRecord(BaseModel):
    model_config = ConfigDict(extra="allow")
    id: int
    domain_id: int
    name: str
    type: str
    content: Union[str, List[str]]
    ttl: int
    prio: int
    change_date: int
    priority: int


class DnsDetails(BaseModel):
    model_config = ConfigDict(extra="allow")
    service_id: int
    name: str
    records: List[DnsRecord]


class DnsRecordPayload(BaseModel):
    model_config = ConfigDict(extra="allow")
    name: str
    ttl: int
    priority: int
    type: str
    content: Union[str, List[Union[int, str]]]


class DnsRecordResponse(BaseModel):
    model_config = ConfigDict(extra="allow")
    success: bool
    record: DnsRecordPayload
    info: List[List[str]]  # why double list?


class DnsZone(BaseModel):
    model_config = ConfigDict(extra="allow")
    domain_id: int
    name: str
    service_id: int


class DnsZones(BaseModel):
    model_config = ConfigDict(extra="allow")
    service_ids: List[int]
    zones: List[DnsZone]

class ApiResponse(BaseModel):
    model_config = ConfigDict(extra="allow")
    success: bool
