package cloud.marton.hostup_dns_client.model;

public record ApiResponse(boolean success, int httpStatus, String body, HostupApiResponse parsedResponse) {
}
