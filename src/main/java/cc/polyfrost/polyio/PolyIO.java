package cc.polyfrost.polyio;

import cc.polyfrost.polyio.util.EnumOS;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author xtrm
 */
public class PolyIO {
    public static final String IMPLEMENTATION_NAME =
            PolyIO.class.getPackage().getImplementationTitle();
    public static final String IMPLEMENTATION_VERSION =
            PolyIO.class.getPackage().getImplementationVersion();
    public static final String USER_AGENT =
            String.format("%s/%s", IMPLEMENTATION_NAME, IMPLEMENTATION_VERSION);

    public static final String GLOBAL_STORE_NAME =
            System.getProperty("polyio.globalStoreName", "Polyfrost");
    private static Path localStorage = null;

    private PolyIO() {
    }

    public static Path getLocalStorage() {
        if (localStorage == null) {
            localStorage = findSystemLocalStorage();
        }
        return localStorage;
    }

    private static Path findSystemLocalStorage() {
        Path storePath = null;

        EnumOS os = EnumOS.fetchCurrent();
        switch (os) {
            case WINDOWS:
                storePath = Paths.get(System.getenv("APPDATA"));
                if (!Files.exists(storePath)) {
                    storePath = Paths.get(System.getProperty("user.home"), "AppData",
//                                 use Roaming since %appdata% redirects to it
//                                 and that's the most common-ly used one,
//                                 although Local would've been more fit
                            "Roaming");
                }
                break;
            case OSX:
                storePath = Paths.get(System.getProperty("user.home"), "Library",
                        "Application Support");
                break;
            case UNIX_LIKE:
                String baseDir = System.getenv().getOrDefault(
                        "XDG_DATA_HOME", System.getProperty("user.home") + File.separator +
                                ".local" + File.separator + "share");
                storePath = Paths.get(baseDir);
                break;
        }

        if (storePath == null || !Files.exists(storePath)) {
            storePath = Paths.get(System.getProperty(
                    "java.io.tmpdir", Stream.of("TEMP", "TMP", "TMPDIR")
                            .map(System::getenv)
                            .filter(Objects::nonNull)
                            .filter(it -> Files.exists(Paths.get(it)))
                            .findFirst()
                            .orElse("/tmp")));
        }

        if (!Files.exists(storePath)) {
            throw new IllegalStateException(
                    "Could not find platform target local store: " + storePath);
        }

        return storePath;
    }
}
