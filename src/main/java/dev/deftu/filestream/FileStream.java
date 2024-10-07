package dev.deftu.filestream;

import dev.deftu.filestream.util.OperatingSystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author xtrm
 */
public class FileStream {

    public static final String NAME = "@PROJECT_NAME@";
    public static final String VERSION = "@PROJECT_VERSION@";

    public static final String USER_AGENT =
            String.format("%s/%s", NAME, VERSION);

    public static final String GLOBAL_STORE_NAME =
            System.getProperty("polyio.globalStoreName", "Polyfrost");

    private static Path localStorage = null;

    private FileStream() {
    }

    public static Path getLocalStorage() {
        if (localStorage == null) {
            localStorage = findSystemLocalStorage();
        }

        return localStorage;
    }

    private static Path findSystemLocalStorage() {
        Path storePath = null;

        OperatingSystem os = OperatingSystem.find();
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
            case MACOS:
                storePath = Paths.get(System.getProperty("user.home"), "Library",
                        "Application Support");

                break;
            default:
                if (os.isUnixLike()) {
                    String baseDir = System.getenv().getOrDefault(
                            "XDG_DATA_HOME", System.getProperty("user.home") + File.separator +
                                    ".local" + File.separator + "share");
                    storePath = Paths.get(baseDir);
                }

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
