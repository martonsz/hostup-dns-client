package cloud.marton.hostup_dns_client.exceptions;

public final class RateLimitException extends ApiException {
    public RateLimitException(int httpStatusCode, String errorMessage, String bodyAsString) {
        super(httpStatusCode, errorMessage, bodyAsString);
    }
}
