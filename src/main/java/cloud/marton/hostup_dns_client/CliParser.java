package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.exceptions.CliParserException;

import java.net.URI;
import java.util.Arrays;

public class CliParser {

    public static final String HOSTUP_DNS_CLIENT_BASE_URI = "https://cloud.hostup.se/api/";

    private static final String USAGE = """
            Usage: hostup-dns-client
              -a --add-record <zoneId> <type> <domain> <value> <ttl> Add a DNS record
                     Example: -a 10111 A foo.example.org 1.2.3.4 3600
                     zoneId:  Zone ID for the domain. Use --list-zones to find the correct ID
                     type:    A, TXT, CNAME, etc.
                     domain:  e.g. "foo.example.org"
                     value:   e.g. "1.2.3.4" for A record, or "some text" for TXT record
                     ttl:     Time to live in seconds
              -b --base-uri <uri>                    Base URI for the Hostup API (optional, defaults to %s)
              -d --delete-domain <domain>            Removes *ALL* records (A, TXT, etc) for the matching domain. E.g. "foo.example.org"
              -D --delete-record <zoneId> <recordId> Remove a single record by its ID. Use --list-records to find the record ID.
              -k --api-key <key>                     API key for authentication
              -l --list-zones                        List all DNS zones associated with an account
              -r --list-records <zoneId>             Get DNS records for a domain zone
              -v --version
              -h --help
            
            Positional mode for Lego (https://go-acme.github.io/lego/dns/exec/)
              hostup-dns-client <action> <domain> <value>
              action: present | cleanup
            Example
              hostup-dns-client "present" "_acme-challenge.my.example.org." "MsijOYZxqyjGnFGwhjrhfg-Xgbl5r68WPda0J9EgqqI"
            
            You can also use environment variables (required for LEGO mode):
              HOSTUP_DNS_CLIENT_API_KEY
              HOSTUP_DNS_CLIENT_BASE_URI (optional, defaults to https://cloud.hostup.se/api/)
            """.formatted(HOSTUP_DNS_CLIENT_BASE_URI);

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
        AddRecord addRecord = null;
        URI baseUri = null;
        String baseUriString = env.get("HOSTUP_DNS_CLIENT_BASE_URI") == null ?
                HOSTUP_DNS_CLIENT_BASE_URI : env.get("HOSTUP_DNS_CLIENT_BASE_URI");
        String deleteDomain = null;
        DeleteRecord deleteRecord = null;
        String apiKey = env.get("HOSTUP_DNS_CLIENT_API_KEY");
        boolean listZones = false;
        Integer listRecords = null;
        boolean version = false;
        boolean help = false;
        LegoArgs legoArgs = null;

        boolean legoMode = args.length == 3 && Arrays.stream(args).noneMatch(arg -> arg.startsWith("-"));
        if (legoMode) {
            String actionString = getStringArgument(args, 0, "legoAction");
            String domain = getStringArgument(args, 1, "legoDomain");
            String value = getStringArgument(args, 2, "legoValue");
            try {
                LegoAction action = LegoAction.valueOf(actionString.toUpperCase());
                legoArgs = new LegoArgs(action, domain, value);
            } catch (IllegalArgumentException e) {
                throw new CliParserException("action must be 'present' or 'cleanup'", e);
            }
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.isBlank()) {
                    continue;
                }
                switch (arg) {
                    case "-a", "--add-record" -> {
                        int zoneId = getIntArgument(args, ++i, arg + " zoneId");
                        String type = getStringArgument(args, ++i, arg + " type");
                        String domain = getStringArgument(args, ++i, arg + " domain");
                        String value = getStringArgument(args, ++i, arg + " value");
                        int ttl = getIntArgument(args, ++i, arg + " ttl");
                        addRecord = new AddRecord(zoneId, type, domain, value, ttl);
                    }
                    case "-b", "--base-uri" -> baseUriString = getStringArgument(args, ++i, arg);
                    case "-d", "--delete-domain" -> deleteDomain = getStringArgument(args, ++i, arg);
                    case "-D", "--delete-record" -> {
                        int zoneId = getIntArgument(args, ++i, arg + " zoneId");
                        int recordId = getIntArgument(args, ++i, arg + " recordId");
                        deleteRecord = new DeleteRecord(zoneId, recordId);
                    }
                    case "-k", "--api-key" -> apiKey = getStringArgument(args, ++i, arg);
                    case "-l", "--list-zones" -> listZones = true;
                    case "-r", "--list-records" -> listRecords = getIntArgument(args, ++i, arg);
                    case "-v", "--version" -> version = true;
                    case "-h", "--help" -> help = true;
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
        return new CliOptions(
                addRecord,
                baseUri,
                deleteDomain,
                deleteRecord,
                apiKey,
                listZones,
                listRecords,
                version,
                help,
                legoArgs
        );
    }

    private String getStringArgument(String[] args, int i, String argName) throws CliParserException {
        if (i >= args.length) {
            throw new CliParserException("Missing value for " + argName);
        }
        return args[i];
    }

    private int getIntArgument(String[] args, int i, String argName) throws CliParserException {
        if (i >= args.length) {
            throw new CliParserException("Missing value for " + argName);
        }
        try {
            return Integer.parseInt(args[i]);
        } catch (NumberFormatException e) {
            throw new CliParserException("Invalid integer for " + argName + ": " + args[i], e);
        }
    }

    public static String getUsage() {
        return USAGE;
    }

    public record CliOptions(
            AddRecord addRecord,
            URI baseUri,
            String deleteDomain,
            DeleteRecord deleteRecord,
            String apiKey,
            boolean listZones,
            Integer listRecords,
            boolean version,
            boolean help,
            LegoArgs legoArgs
    ) {
    }

    public enum LegoAction {
        PRESENT,
        CLEANUP
    }

    public record AddRecord(
            int zoneId,
            String type,
            String domain,
            String value,
            int ttl
    ) {
    }

    public record DeleteRecord(int zoneId, int recordId) {
    }

    public record LegoArgs(
            LegoAction action,
            String domain,
            String value
    ) {
    }
}
