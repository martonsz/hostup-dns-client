package cloud.marton.hostup_dns_client.exceptions;

public sealed class ApiException extends Exception permits JsonMappingException, RateLimitException, HttpErrorException {
    private final int httpStatusCode;
    private final String bodyAsString;

    public ApiException(int httpStatusCode, String errorMessage, String bodyAsString, Throwable cause) {
        super(errorMessage);
        this.httpStatusCode = httpStatusCode;
        this.bodyAsString = bodyAsString;
        initCause(cause);
    }

    public ApiException(int httpStatusCode, String errorMessage, String bodyAsString) {
        super(errorMessage);
        this.httpStatusCode = httpStatusCode;
        this.bodyAsString = bodyAsString;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    private String getBody() {
        return bodyAsString;
    }

    @Override
    public String toString() {
        return "%s {httpStatusCode=%d, message='%s', body='%s'}".formatted(
                this.getClass().getSimpleName(),
                getHttpStatusCode(),
                getMessage(),
                getBody()
        );
    }
}
