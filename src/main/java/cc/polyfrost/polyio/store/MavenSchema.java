package cc.polyfrost.polyio.store;

import cc.polyfrost.polyio.api.Store;

import java.io.File;
import java.nio.file.Path;

/**
 * @author xtrm
 */
public class MavenSchema implements Store.ObjectSchema {
    public static final MavenSchema INSTANCE = new MavenSchema();

    @Override
    public Path getObjectPath(Path storeRoot, String name) {
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
    }
}
