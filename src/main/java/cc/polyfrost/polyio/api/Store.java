package cc.polyfrost.polyio.api;

import java.nio.file.Path;

/**
 * @author xtrm
 */
public interface Store {
    Path getStoreRoot();

    Path getObject(String name);

    /**
     * @author xtrm
     */
    @FunctionalInterface
    interface ObjectSchema {
        ObjectSchema DIRECT = Path::resolve;

        Path getObjectPath(Path storeRoot, String name);
    }
}
