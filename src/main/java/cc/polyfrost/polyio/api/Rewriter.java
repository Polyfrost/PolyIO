package cc.polyfrost.polyio.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xtrm
 */
@FunctionalInterface
public interface Rewriter {
    Logger log = LogManager.getLogger(Rewriter.class);

    Rewriter DEFAULT = (origin, target) -> {
        log.trace("Creating parent directories for {}", target);
        try {
            Files.createDirectories(target.getParent());
        } catch (IOException e) {
            log.error("Couldn't create parent directories for {}", target, e);
        }

        if (Files.exists(target)) {
            log.trace("Target file {} already exists, skipping", target);
            return target;
        }

        log.trace("Creating link from {} to {}", target, origin);
        try {
            log.trace("Trying to create symbolic link");
            Files.createSymbolicLink(target, origin);
            log.trace("Created symbolic link");
            return target;
        } catch (UnsupportedOperationException uoe) {
            log.warn("Symbolic links are not supported on this platform, " +
                    "falling back to hard links.");
        } catch (IOException e) {
            log.error("Couldn't create symbolic link, falling " +
                    "back to hard link.", e);
        }

        try {
            log.trace("Trying to create hard link");
            Files.createLink(target, origin);
            log.trace("Created hard link");
            return target;
        } catch (UnsupportedOperationException uoe) {
            log.warn("Hard links are not supported on this platform, " +
                    "falling back to copying.");
        } catch (IOException e) {
            log.error("Couldn't create hard link, falling back " +
                    "to copying.", e);
        }

        try {
            log.trace("Trying to copy file");
            Files.copy(origin, target);
            log.trace("Copied file");
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format(
                            "Couldn't copy target file %s to %s",
                            origin,
                            target
                    ),
                    e
            );
        }

        return target;
    };

    Path rewrite(Path origin, Path target);
}
