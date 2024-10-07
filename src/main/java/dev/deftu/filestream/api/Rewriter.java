package dev.deftu.filestream.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xtrm
 */
@FunctionalInterface
public interface Rewriter {

    Logger logger = LogManager.getLogger(Rewriter.class);

    Rewriter DEFAULT = (origin, target) -> {
        logger.trace("Creating parent directories for {}", target);
        try {
            Files.createDirectories(target.getParent());
        } catch (IOException e) {
            logger.error("Couldn't create parent directories for {}", target, e);
        }

        if (Files.exists(target)) {
            logger.trace("Target file {} already exists, skipping", target);
            return target;
        }

        logger.trace("Creating link from {} to {}", target, origin);
        try {
            logger.trace("Trying to create symbolic link");
            Files.createSymbolicLink(target, origin);
            logger.trace("Created symbolic link");
            return target;
        } catch (UnsupportedOperationException uoe) {
            logger.warn("Symbolic links are not supported on this platform, " +
                    "falling back to hard links.");
        } catch (IOException e) {
            logger.error("Couldn't create symbolic link, falling " +
                    "back to hard link.", e);
        }

        try {
            logger.trace("Trying to create hard link");
            Files.createLink(target, origin);
            logger.trace("Created hard link");
            return target;
        } catch (UnsupportedOperationException uoe) {
            logger.warn("Hard links are not supported on this platform, " +
                    "falling back to copying.");
        } catch (IOException e) {
            logger.error("Couldn't create hard link, falling back " +
                    "to copying.", e);
        }

        try {
            logger.trace("Trying to copy file");
            Files.copy(origin, target);
            logger.trace("Copied file");
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

    @NotNull Path rewrite(@NotNull Path origin, @NotNull Path target);

}
