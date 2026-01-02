package cloud.marton.hostup_dns_client.exceptions;

public final class HttpErrorException extends ApiException {
    public HttpErrorException(int httpStatusCode, String errorMessage, String bodyAsString) {
        super(httpStatusCode, errorMessage, bodyAsString);
    }
}
