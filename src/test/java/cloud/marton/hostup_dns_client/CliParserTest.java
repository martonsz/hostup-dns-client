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
    @ValueSource(strings = {"-l", "--list-zones"})
    void parsesFlagsWithEnvDefaults(String flag) throws Exception {
        CliParser p = parser(Map.of(
                "HOSTUP_DNS_CLIENT_API_KEY", "env-key",
                "HOSTUP_DNS_CLIENT_BASE_URI", "https://env.example/api/"));
        CliOptions opts = p.parseArgs(new String[]{flag});
        assertEquals("env-key", opts.apiKey());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertTrue(opts.listZones());
        assertFalse(opts.positionalMode());
    }

    @Test
    void allowEmptyStrings() throws Exception {
        CliParser p = parser(Map.of());
        CliOptions opts = p.parseArgs(new String[]{" ", "-a", "api-key", "-b", "https://env.example/api/", " ", "--list-zones", " "});
        assertEquals("api-key", opts.apiKey());
        assertEquals(URI.create("https://env.example/api/"), opts.baseUri());
        assertTrue(opts.listZones());
        assertFalse(opts.positionalMode());
    }

    @ParameterizedTest
    @CsvSource({
            "-a,-b",
            "-a,--base-uri",
            "--api-key,-b",
            "--api-key,--base-uri"
    })
    void overridesApiKeyAndBaseUriFromArgs(String apiKeyFlag, String baseUriFlag) throws Exception {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "env-key"));
        CliOptions opts = p.parseArgs(new String[]{apiKeyFlag, "cli-key", baseUriFlag, "https://cli.example/"});
        assertEquals("cli-key", opts.apiKey());
        assertEquals(URI.create("https://cli.example/"), opts.baseUri());
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
        CliParserException ex = assertThrows(CliParserException.class, () -> p.parseArgs(new String[]{"-a"}));
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
    void positionalPresentParses(LegoAction action) throws Exception {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliOptions opts = p.parseArgs(new String[]{action.name(), "_acme-challenge.example.", "token"});
        assertTrue(opts.positionalMode());
        assertEquals(action, opts.action());
        assertEquals("_acme-challenge.example.", opts.domain());
        assertEquals("token", opts.value());
    }

    @Test
    void positionalInvalidActionFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"bad", "_acme", "v"}));
        assertTrue(ex.getMessage().contains("action must be"));
    }

    @Test
    void mixingPositionalArgumentFails() {
        CliParser p = parser(Map.of("HOSTUP_DNS_CLIENT_API_KEY", "k"));
        CliParserException ex = assertThrows(CliParserException.class,
                () -> p.parseArgs(new String[]{"-a", "api-key", "present", "_acme-challenge.example.", "token"}));
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
