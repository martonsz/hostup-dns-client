package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.CliParserException;
import cloud.marton.hostup_dns_client.exceptions.HttpErrorException;
import cloud.marton.hostup_dns_client.exceptions.JsonMappingException;
import cloud.marton.hostup_dns_client.exceptions.RateLimitException;
import cloud.marton.hostup_dns_client.logging.LoggingConfigurator;
import cloud.marton.hostup_dns_client.model.ZonesResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    static {
        LoggingConfigurator.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static void main(String[] args) {
        try {
            CliParser cliParser = new CliParser();
            CliParser.CliOptions options = cliParser.parseArgs(args);

            if (options.help()) {
                LOGGER.info(() -> "Usage:\n" + CliParser.getUsage());
                return;
            }
            if (options.version()) {
                LOGGER.info(() -> "hostup-dns-client version " + getVersion());
                return;
            }
            HostupApiClient client = new HostupApiClient(options.apiKey(), options.baseUri());
            if (options.listZones()) {
                ZonesResponse zonesResponse = client.listZones();
                LOGGER.info(() -> "\n" + zonesResponse.pretty());
            }
        } catch (CliParserException e) {
            LOGGER.log(Level.SEVERE, e, () -> e.getMessage() + "\n\n" + CliParser.getUsage());
            System.exit(1);
        } catch (IOException | InterruptedException | HttpErrorException | RateLimitException |
                 JsonMappingException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            System.exit(1);
        }
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }

}
