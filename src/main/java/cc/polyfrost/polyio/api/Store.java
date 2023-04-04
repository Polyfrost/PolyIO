package cc.polyfrost.polyio.api;

import cc.polyfrost.polyio.store.FastHashSchema;
import cc.polyfrost.polyio.store.PolyStore;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * @author xtrm
 */
public interface Store {
    Path getStoreRoot();

    Path getObject(String name);

    Store getSubStore(String name);

    Store getSubStore(String name, ObjectSchema objectSchema);

    static @Nullable Store getGlobalStore() {
        return PolyStore.GLOBAL_STORE;
    }

    static Store of(String name) {
        return new PolyStore(getGlobalStore().getStoreRoot(), name);
    }

    static Store of(String name, ObjectSchema objectSchema) {
        return new PolyStore(getGlobalStore().getStoreRoot(), name, objectSchema);
    }

    /**
     * @author xtrm
     */
    @FunctionalInterface
    interface ObjectSchema {
        ObjectSchema DIRECT = Path::resolve;
        ObjectSchema URL_ENCODED = (storeRoot, name) ->
                storeRoot.resolve(URLEncoder.encode(name, "UTF-8"));
        ObjectSchema MAVEN = (storeRoot, name) -> {
            // groupId:artifactId:version:classifier:extension
            String[] dataBits = name.split(":");
            if (dataBits.length < 3) {
                throw new UnsupportedOperationException("Invalid maven schema");
            }

            String groupId = dataBits[0];
            String artifactId = dataBits[1];
            String version = dataBits[2];
            String classifier = dataBits.length > 3 ? dataBits[3] : null;
            String extension = dataBits.length > 4 ? dataBits[4] : "jar";

            String[] groupBits = groupId.split("\\.");
            String groupPath = String.join(File.separator, groupBits);
            String fileName = artifactId + "-" + version +
                    (classifier != null ? "-" + classifier : "") +
                    "." + extension;
            return storeRoot.resolve(groupPath)
                    .resolve(artifactId)
                    .resolve(version)
                    .resolve(fileName);
        };

        static ObjectSchema fastHash(MessageDigest digest) {
            return new FastHashSchema(digest);
        }

        Path getObjectPath(Path storeRoot, String name) throws IOException;
    }
}
