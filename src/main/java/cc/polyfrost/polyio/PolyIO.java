package cc.polyfrost.polyio;

import fr.stardustenterprises.plat4k.EnumFamily;
import fr.stardustenterprises.plat4k.EnumOperatingSystem;
import fr.stardustenterprises.plat4k.Platform;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author xtrm
 */
public class PolyIO {
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

        Platform platform = Platform.getCurrentPlatform();
        EnumOperatingSystem os = platform.getOperatingSystem();
        if (os == EnumOperatingSystem.MACOS) {
            storePath = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else {
            EnumFamily family = Arrays.stream(EnumFamily.values())
                    .filter(it -> it.contains(os))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unsupported platform: " + platform));
            switch (family) {
                case BSD:
                case UNIX:
                    String baseDir = System.getenv().getOrDefault(
                            "XDG_DATA_HOME",
                            System.getProperty("user.home") +
                                    File.separator + ".local" +
                                    File.separator + "share"
                    );
                    storePath = Paths.get(baseDir);
                    break;
                case WINDOWS:
                    storePath = Paths.get(System.getenv("APPDATA"));
                    if (!Files.exists(storePath)) {
                        storePath = Paths.get(System.getProperty("user.home"), "AppData", "Roaming");
                    }
                    break;
            }
        }

        if (storePath == null) {
            storePath = Paths.get(
                    System.getProperty(
                            "java.io.tmpdir",
                            Stream.of("TEMP", "TMP", "TMPDIR")
                                    .map(System::getenv)
                                    .filter(Objects::nonNull)
                                    .filter(it -> Files.exists(Paths.get(it)))
                                    .findFirst()
                                    .orElse("/tmp")
                    )
            );
        }
        if (!Files.exists(storePath)) {
            throw new RuntimeException("Could not find platform target local store: " + storePath);
        }

        return storePath;
    }
}
