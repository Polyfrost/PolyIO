package dev.deftu.filestream.store;

import dev.deftu.filestream.api.Store;
import dev.deftu.filestream.util.HashingHelper;

import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * @author xtrm
 */
public class FastHashSchema implements Store.ObjectSchema {

    public static final FastHashSchema INSTANCE = new FastHashSchema(HashingHelper.SHA256);

    private final MessageDigest digest;

    public FastHashSchema(MessageDigest digest) {
        this.digest = digest;
    }

    @Override
    public Path getObjectPath(Path storeRoot, String name) {
        String nameHash = HashingHelper.hash(name, digest);
        return storeRoot.resolve(nameHash.substring(0, 2))
                .resolve(nameHash.substring(0, 4))
                .resolve(nameHash);
    }

}
