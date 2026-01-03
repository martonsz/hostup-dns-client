package cloud.marton.hostup_dns_client.exceptions;

public class CliParserException extends Exception {

    public CliParserException(String message) {
        super(message);
    }

    public CliParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
