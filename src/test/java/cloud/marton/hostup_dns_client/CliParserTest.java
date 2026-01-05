package cloud.marton.hostup_dns_client;

import cloud.marton.hostup_dns_client.CliParser.CliOptions;
import cloud.marton.hostup_dns_client.CliParser.LegoAction;
import cloud.marton.hostup_dns_client.exceptions.CliParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CliParserTest {

    private CliParser parser(Map<String, String> env) {
        return new CliParser(env::get);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-a", "--add-record"})
    void addRecord(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        String[] flags = new String[]{
                flag,
                "123",
                "TXT",
                "test.example.com.",
                "my value",
                "500"
        };
        CliOptions opts = p.parseArgs(flags);
        assertNotNull(opts.addRecord());
        assertEquals(123, opts.addRecord().zoneId());
        assertEquals("TXT", opts.addRecord().type());
        assertEquals("test.example.com.", opts.addRecord().domain());
        assertEquals("my value", opts.addRecord().value());
        assertEquals(500, opts.addRecord().ttl());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("env-key", opts.apiKey());
        assertFalse(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-d", "--delete-domain"})
    void deleteDomain(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        String[] flags = new String[]{flag, "foo.example.com"};
        CliOptions opts = p.parseArgs(flags);
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNotNull(opts.deleteDomain());
        assertEquals("foo.example.com", opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("env-key", opts.apiKey());
        assertFalse(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-D", "--delete-record"})
    void deleteRecord(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        String[] flags = new String[]{flag, "123", "3000"};
        CliOptions opts = p.parseArgs(flags);
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNotNull(opts.deleteRecord());
        assertEquals(123, opts.deleteRecord().zoneId());
        assertEquals(3000, opts.deleteRecord().recordId());
        assertEquals("env-key", opts.apiKey());
        assertFalse(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-l", "--list-zones"})
    void listZones(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        CliOptions opts = p.parseArgs(new String[]{flag});
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("env-key", opts.apiKey());
        assertTrue(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "--list-records"})
    void listRecords(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        String[] flags = new String[]{flag, "123"};
        CliOptions opts = p.parseArgs(flags);
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("env-key", opts.apiKey());
        assertFalse(opts.listZones());
        assertNotNull(opts.listRecords());
        assertEquals(123, opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "--list-records"})
    void listRecordsMissingArgument(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{flag}));
        assertTrue(ex.getMessage().contains("Missing value for"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-r", "--list-records"})
    void listRecordsInvalidArg(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        String[] flags = new String[]{flag, "not-an-integer"};
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(flags));
        assertTrue(ex.getMessage().contains("Invalid integer for"));
    }

    @Test
    void allowEmptyStrings() throws Exception {
        CliParser p = parser(Map.of());
        CliOptions opts = p.parseArgs(new String[]{" ", "-k", "api-key", "-b", "https://env.example/api/", " ", "--list-zones", " "});
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("api-key", opts.apiKey());
        assertTrue(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @ParameterizedTest
    @CsvSource({
            "-k,-b",
            "-k,--base-uri",
            "--api-key,-b",
            "--api-key,--base-uri"
    })
    void overridesApiKeyAndBaseUriFromArgs(String apiKeyFlag, String baseUriFlag) throws Exception {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "env-key"));
        CliOptions opts = p.parseArgs(new String[]{apiKeyFlag, "cli-key", baseUriFlag, "https://cli.example/"});
        assertNull(opts.addRecord());
        assertEquals(URI.create("https://cli.example/"), opts.baseUri());
        assertNull(opts.deleteDomain());
        assertNull(opts.deleteRecord());
        assertEquals("cli-key", opts.apiKey());
        assertFalse(opts.listZones());
        assertNull(opts.listRecords());
        assertFalse(opts.version());
        assertFalse(opts.help());
        assertNull(opts.legoArgs());
    }

    @Test
    void missingApiKeyFailsWhenNotHelpOrVersion() {
        CliParser p = parser(Map.of());
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{}));
        assertTrue(ex.getMessage().contains("api-key is required"));
    }

    @Test
    void missingValueForApiKeyFlagFails() {
        CliParser p = parser(Map.of());
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{"-k"}));
        assertTrue(ex.getMessage().contains("Missing value"));
    }

    @Test
    void missingValueForBaseUriFlagFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{"-b"}));
        assertTrue(ex.getMessage().contains("Missing value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--nope", "present"})
    void unknownFlagFails(String flag) {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{flag}));
        assertTrue(ex.getMessage().contains("Unknown argument"));
    }

    @Test
    void invalidUriFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"-b", "http:// bad uri"}));
        assertTrue(ex.getMessage().contains("Invalid URI"));
    }

    @ParameterizedTest
    @EnumSource(LegoAction.class)
    void legoArgsPresentParses(LegoAction action) throws Exception {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliOptions opts = p.parseArgs(new String[]{action.name(), "_acme-challenge.example.", "token"});
        CliParser.LegoArgs legoArgs = opts.legoArgs();
        assertNotNull(legoArgs);
        assertEquals(action, legoArgs.action());
        assertEquals("_acme-challenge.example.", legoArgs.domain());
        assertEquals("token", legoArgs.value());
    }

    @Test
    void legoArgsInvalidActionFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"bad", "_acme", "v"}));
        assertTrue(ex.getMessage().contains("action must be"));
    }

    @Test
    void mixingPositionalArgumentFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"-k", "api-key", "present", "_acme-challenge.example.", "token"}));
        assertTrue(ex.getMessage().contains("Unknown argument"));
    }

    @Test
    void dashPositionalArgumentFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"-present", "_acme-challenge.example.", "token"}));
        assertTrue(ex.getMessage().contains("Unknown argument"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-h", "--help", "-v", "--version"})
    void helpAndVersionFlagsDoNotRequireApiKey(String flag) {
        CliParser p = new CliParser();
        assertDoesNotThrow(() -> p.parseArgs(new String[]{flag}));
    }

    @Test
    void usageNotEmpty() {
        assertFalse(CliParser.getUsage().isBlank());
    }
}
