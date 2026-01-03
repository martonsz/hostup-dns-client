package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.CliParserException;

import java.net.URI;
import java.util.Arrays;

public class CliParser {

    public static final String HOSTUP_DNS_CLIENT_BASE_URI = "https://cloud.hostup.se/api/";

    private static final String USAGE = """
            Usage: hostup-dns-client
              -a --api-key <key>
              -b --base-uri <uri>
              -h --help
              -v --version
              -l --list-zones List DNS zones
            
            Positional mode for Lego (https://go-acme.github.io/lego/dns/exec/)
              hostup-dns-client <action> <domain> <value>
              action: present | cleanup
            Example
              hostup-dns-client "present" "_acme-challenge.my.example.org." "MsijOYZxqyjGnFGwhjrhfg-Xgbl5r68WPda0J9EgqqI"
            
            You can also use environment variables (required for positional mode):
              HOSTUP_DNS_CLIENT_API_KEY
              HOSTUP_DNS_CLIENT_BASE_URI (optional, defaults to https://cloud.hostup.se/api/)
            """;

    public interface EnvProvider {
        String get(String key);
    }

    private final EnvProvider env;

    public CliParser() {
        this(System::getenv);
    }

    public CliParser(EnvProvider env) {
        this.env = env;
    }

    public CliOptions parseArgs(String[] args) throws CliParserException {
        String apiKey = env.get("HOSTUP_DNS_CLIENT_API_KEY");
        String baseUriString = env.get("HOSTUP_DNS_CLIENT_BASE_URI") == null ?
                HOSTUP_DNS_CLIENT_BASE_URI : env.get("HOSTUP_DNS_CLIENT_BASE_URI");
        URI baseUri = null;
        boolean help = false;
        boolean version = false;
        boolean listZones = false;
        LegoAction action = null;
        String domain = null;
        String value = null;

        boolean acmeLegoArguments = args.length == 3 && Arrays.stream(args).noneMatch(arg -> arg.startsWith("-"));
        if (acmeLegoArguments) {
            try {
                action = LegoAction.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CliParserException("action must be 'present' or 'cleanup'", e);
            }
            domain = args[1];
            value = args[2];
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.isBlank()) {
                    continue;
                }
                switch (arg) {
                    case "-h", "--help" -> help = true;
                    case "-v", "--version" -> version = true;
                    case "-l", "--list-zones" -> listZones = true;
                    case "-a", "--api-key" -> {
                        if (i + 1 >= args.length) {
                            throw new CliParserException("Missing value for " + arg);
                        }
                        apiKey = args[++i];
                    }
                    case "-b", "--base-uri" -> {
                        if (i + 1 >= args.length) {
                            throw new CliParserException("Missing value for " + arg);
                        }
                        baseUriString = args[++i];
                    }
                    default -> throw new CliParserException("Unknown argument: " + arg);
                }
            }
        }

        if (!help && !version) {
            if (apiKey == null) {
                throw new CliParserException("api-key is required");
            }
            try {
                baseUri = URI.create(baseUriString);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new CliParserException("Invalid URI for base-uri: " + baseUriString, e);
            }
        }
        return new CliOptions(apiKey, baseUri, help, version, listZones, acmeLegoArguments, action, domain, value);
    }

    public static String getUsage() {
        return USAGE;
    }

    public record CliOptions(String apiKey, URI baseUri, boolean help, boolean version, boolean listZones,
                             boolean positionalMode, LegoAction action, String domain, String value) {
    }

    public enum LegoAction {
        PRESENT,
        CLEANUP
    }

}
