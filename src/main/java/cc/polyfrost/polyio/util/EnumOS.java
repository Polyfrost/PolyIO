package cc.polyfrost.polyio.util;

import java.util.Arrays;

/**
 * Fairly simple and stripped-down version of an Operating System detection
 * mechanism.
 *
 * @author xtrm
 * @since 0.0.9
 */
public enum EnumOS {
    WINDOWS("windows", "win"),
    OSX("macos", "osx", "macintosh", "mac"),
    UNIX_LIKE("nix", "linux", "bsd", "aix", "solaris", "sunos", "gnu");

    private final String[] identifiers;

    EnumOS(String... identifiers) {
        this.identifiers = identifiers;
    }

    public static EnumOS fetchCurrent() {
        String osName = System.getProperty("os.name").toLowerCase();
        return Arrays.stream(values())
                .filter(it -> Arrays.stream(it.identifiers).anyMatch(osName::contains))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported platform: " + osName));
    }
}
