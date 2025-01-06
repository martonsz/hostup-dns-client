
from pydantic import BaseModel, ConfigDict
from typing import List, Union

class ErrorMessage(BaseModel):
    model_config = ConfigDict(extra="allow")
    success: bool
    error: List[str]
