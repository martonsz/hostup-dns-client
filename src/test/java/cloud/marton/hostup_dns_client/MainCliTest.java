package cloud.marton.hostup_dns_client;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class MainCliTest {

    @Test
    void parsesApiKeyAndBaseUri() throws Exception {
        Object opts = invokeParseArgs("-a", "abc123", "-b", "https://example.com/api/");
        assertEquals("abc123", apiKey(opts));
        assertEquals(URI.create("https://example.com/api/"), baseUri(opts));
        assertFalse(help(opts));
    }

    @Test
    void parsesHelpFlag() throws Exception {
        Object opts = invokeParseArgs("-h");
        assertTrue(help(opts));
    }

//    @Test
//    void noArgsThrows() {
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invokeParseArgs(""));
//        assertTrue(ex.getMessage().contains("api-key is required"));
//    }

    @Test
    void missingApiKeyValueThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invokeParseArgs("-a"));
        assertTrue(ex.getMessage().contains("Missing value"));
    }

    @Test
    void missingBaseUriValueThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invokeParseArgs("-b"));
        assertTrue(ex.getMessage().contains("Missing value"));
    }

    @Test
    void unknownArgThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invokeParseArgs("--nope"));
        assertTrue(ex.getMessage().contains("Unknown argument"));
    }

    @Test
    void invalidUriThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> invokeParseArgs("-a", "k", "-b", "http:// bad uri"));
        assertTrue(ex.getMessage().contains("Invalid URI"));
    }

    @Test
    void runHelp() {
        assertDoesNotThrow(() -> Main.main(new String[]{"--help"}));
    }

    @Test
    void parsesVersionFlag() throws Exception {
        Object opts = invokeParseArgs("--version");
        assertTrue(version(opts));
    }

    // --- helpers ---

    private static Object invokeParseArgs(String... args) throws Exception {
        Method m = Main.class.getDeclaredMethod("parseArgs", String[].class);
        m.setAccessible(true);
        try {
            return m.invoke(null, (Object) args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof IllegalArgumentException iae) {
                throw iae;
            }
            throw ite;
        }
    }

    private static String apiKey(Object opts) throws Exception {
        Method m = opts.getClass().getDeclaredMethod("apiKey");
        m.setAccessible(true);
        return (String) m.invoke(opts);
    }

    private static URI baseUri(Object opts) throws Exception {
        Method m = opts.getClass().getDeclaredMethod("baseUri");
        m.setAccessible(true);
        return (URI) m.invoke(opts);
    }

    private static boolean help(Object opts) throws Exception {
        Method m = opts.getClass().getDeclaredMethod("help");
        m.setAccessible(true);
        return (boolean) m.invoke(opts);
    }

    private static boolean version(Object opts) throws Exception {
        Method m = opts.getClass().getDeclaredMethod("version");
        m.setAccessible(true);
        return (boolean) m.invoke(opts);
    }
}
