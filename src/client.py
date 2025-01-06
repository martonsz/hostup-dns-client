import jwt_store
import json
import logging

from app_config import AppConfig
from model.authentication import Authentication
from model.dns import (
    DnsDetails,
    DnsRecordPayload,
    DnsRecordResponse,
    DnsZones,
    DnsRecord,
    DnsZone,
    ApiResponse,
)
from model.error_msg import ErrorMessage

from requests import Request, Response, Session
from typing import List

logger = logging.getLogger(__name__)


class ToManyRequestsException(Exception):
    """Rate limit reached"""

    pass


class ZoneNotFoundException(Exception):
    """Zone not found"""

    pass


class ApiErrorMessageException(Exception):
    """API returned an error message"""

    def __init__(self, errorMessage: ErrorMessage) -> None:
        self.errorMessage = errorMessage


class ApiRequestFailedException(Exception):
    """API request failed"""

    pass


class HostUpClient:
    def __init__(self, config: AppConfig) -> None:
        self.config = config

    def get_zones(self) -> DnsZones:
        response = self._send_request("GET", "/dns")
        return DnsZones.model_validate_json(response.text)

    def get_dns_details(self, service_id: int, zone_id: int) -> DnsDetails:
        response = self._send_request("GET", f"/service/{service_id}/dns/{zone_id}")
        return DnsDetails.model_validate_json(response.text)

    def add_record(
        self,
        service_id: int,
        zone_id: int,
        record: DnsRecordPayload,
        delete_existing: bool = True,
    ) -> DnsRecordResponse:
        logger.debug(f"Adding record: {service_id}, {zone_id}, {record}")

        if delete_existing:
            self.delete_record_by_name(record.name)

        response = self._send_request(
            "POST",
            f"/service/{service_id}/dns/{zone_id}/records",
            record.model_dump(),
        )
        logger.debug(f"Add record response: {response.text}")
        return DnsRecordResponse.model_validate_json(response.text)

    def add_record_by_name(
        self, record: DnsRecordPayload, delete_existing=True
    ) -> DnsRecordResponse:
        logger.debug(f"Adding record by name: {record}")
        zone = self._find_zone(record.name)
        if zone:
            return self.add_record(
                zone.service_id, zone.domain_id, record, delete_existing
            )
        else:
            raise ZoneNotFoundException(
                f"Could not add record because zone was not found for: {record.name}"
            )

    def get_record_by_name(self, name: str) -> DnsRecord:
        _, record = self._find_record(name)
        return record

    def delete_record(
        self, service_id: int, zone_id: int, record_id: int
    ) -> DnsRecordResponse:
        logger.debug(f"Deleting record: {service_id}, {zone_id}, {record_id}")
        response = self._send_request(
            "DELETE", f"/service/{service_id}/dns/{zone_id}/records/{record_id}"
        )
        logger.debug(f"Delete record response: {response.text}")
        return ApiResponse.model_validate_json(response.text)

    def delete_record_by_name(self, name: str) -> list[DnsRecordResponse]:
        logger.debug(f"Deleting record by name: {name}")
        reponses = []
        while True:
            details, record = self._find_record(name)
            if record:
                r = self.delete_record(details.service_id, record.domain_id, record.id)
                reponses.append(r)
                logger.debug(f"Deleted record response: {r}")
            else:
                break
        return reponses

    def logout(self) -> None:
        authentication = jwt_store.load_jwt(self.config.authentication_file_path)
        if not authentication:
            logger.info("No JWT found. Skipping logout")
        else:
            logger.debug("Logging out")
            response = self._send_request("POST", f"/logout")
            logger.info(
                f"Logout response code: {response.status_code}, message: {response.text}"
            )
            if response.status_code == 200:
                jwt_store.delete_jwt(self.config.authentication_file_path)

    def _find_zone(self, name: str) -> DnsZone:
        logger.debug(f"Finding zone: {name}")
        zones = self.get_zones().zones
        for zone in zones:
            if zone.name in name:
                logger.debug(f"Found zone: {zone}")
                return zone
        logger.debug(f"Zone not found: {name}")
        return None

    def _find_record(self, name: str) -> tuple[DnsDetails, DnsRecord]:
        logger.debug(f"Finding record: {name}")
        zones = self.get_zones().zones
        for zone in zones:
            details = self.get_dns_details(zone.service_id, zone.domain_id)
            record = list(filter(lambda x: x.name == name, details.records))
            if record:
                logger.debug(f"Found record: {record[0]}")
                return details, record[0]
        logger.debug(f"Record not found: {name}")
        return None, None

    def _get_jwt(self) -> Authentication:
        authentication = jwt_store.load_jwt(self.config.authentication_file_path)
        if authentication:
            return authentication
        authentication = self._authenticate()
        jwt_store.save_jwt(authentication, self.config.authentication_file_path)
        return authentication

    def _authenticate(self) -> Authentication:
        logger.debug("Authenticating")
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
            jsonRepsonse = json.loads(response.text)
            if "success" in jsonRepsonse and not jsonRepsonse["success"]:
                raise ApiErrorMessageException(
                    ErrorMessage.model_validate_json(response.text)
                )
            else:
                return response
        else:
            raise ApiRequestFailedException(
                f"Failed with request. Status code: {response.status_code}. Response from API: {response.text}"
            )
