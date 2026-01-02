package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.logging.LoggingConfigurator;
import cloud.marton.hostup_dns_client.model.ZonesResponse;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final String HOSTUP_DNS_CLIENT_BASE_URI = "https://cloud.hostup.se/api/";

    static {
        LoggingConfigurator.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static void main(String[] args) {
        try {
            CliOptions options = parseArgs(args);
            if (options.help) {
                printUsage();
                return;
            }
            if (options.version) {
                LOGGER.info(() -> "hostup-dns-client version " + getVersion());
                return;
            }
            HostupApiClient client = new HostupApiClient(options.apiKey, options.baseUri);

            try {
                ZonesResponse zonesResponse = client.listZones();
                LOGGER.info(() -> "zonesResponse = " + zonesResponse);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, e::getMessage);
                System.exit(1);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            printUsage();
            System.exit(1);
        }
    }

    private static CliOptions parseArgs(String[] args) {
        String apiKey = System.getenv("HOSTUP_DNS_CLIENT_API_KEY");
        String baseUriString = System.getenv("HOSTUP_DNS_CLIENT_BASE_URI") == null ?
                HOSTUP_DNS_CLIENT_BASE_URI : System.getenv("HOSTUP_DNS_CLIENT_BASE_URI");
        URI baseUri = null;
        boolean help = false;
        boolean version = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.isBlank()) {
                continue;
            }
            switch (arg) {
                case "-h", "--help" -> help = true;
                case "-v", "--version" -> version = true;
                case "-a", "--api-key" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    apiKey = args[++i];
                }
                case "-b", "--base-uri" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    baseUriString = args[++i];
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (!help && !version) {
            if (apiKey == null) {
                throw new IllegalArgumentException("api-key is required");
            }
            try {
                baseUri = URI.create(baseUriString);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URI for base-uri: " + baseUriString);
            }
        }
        return new CliOptions(apiKey, baseUri, help, version);
    }

    private static void printUsage() {
        LOGGER.info(() -> """
                Usage: hostup-dns-client
                  [-a|--api-key <key>]
                  [-b|--base-uri <uri>]
                  [-h|--help]
                  [-v|--version]
                
                You can also use environment variables:
                  HOSTUP_DNS_CLIENT_API_KEY
                  HOSTUP_DNS_CLIENT_BASE_URI
                """);
    }

    private record CliOptions(String apiKey, URI baseUri, boolean help, boolean version) {
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }
}
