package cc.polyfrost.polyio.api;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;

/**
 * @author xtrm
 */
public interface Store {
    Path getStoreRoot();

    Path getObject(String name);

    Store getSubStore(String name);

    Store getSubStore(String name, ObjectSchema objectSchema);

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

        Path getObjectPath(Path storeRoot, String name) throws IOException;
    }
}
