package cloud.marton.hostup_dns_client.exceptions;

public final class JsonMappingException extends ApiException {
    public JsonMappingException(int httpStatusCode, String errorMessage, String bodyAsString, Throwable cause) {
        super(httpStatusCode, errorMessage, bodyAsString, cause);
    }
}
