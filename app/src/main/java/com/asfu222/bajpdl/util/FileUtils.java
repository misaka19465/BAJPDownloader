package com.asfu222.bajpdl.util;

import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.zip.CRC32;

public abstract class FileUtils {
    private static final XXHash64 xxHash64 = XXHashFactory.fastestInstance().hash64();

    // Reusable buffer pool
    private static final ThreadLocal<byte[]> bufferPool = ThreadLocal.withInitial(() -> new byte[8192]);

    public static long calculateCRC32(Path file) throws IOException {
        byte[] buffer = bufferPool.get();
        CRC32 crc32 = new CRC32();

        try (InputStream is = new BufferedInputStream(EscalatedFS.newInputStream(file))) {
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
        }

        return crc32.getValue();
    }

    public static String calculateHash64(String name) {
        byte[] data = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Long.toUnsignedString(xxHash64.hash(data, 0, data.length, 0));
    }

    public static String mapToInGamePath(String urlPath) {
        String fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);

        if (urlPath.startsWith("Android/")) {
            return "AssetBundls/" + fileName;
        } else if (urlPath.startsWith("MediaResources/")) {
            if (fileName.startsWith("MediaCatalog")) {
                return "MediaPatch/Catalog/" + fileName;
            }
            return "MediaPatch/" + fileName;
        }

        return urlPath;
    }

    private static final Set<String> STATIC_FILES = Set.of("TableCatalog.bytes", "MediaCatalog.bytes", "bundleDownloadInfo.json", "TableCatalog.hash", "MediaCatalog.hash", "bundleDownloadInfo.hash");

    public static void copyToGame(Path file, String urlPath) throws IOException {
        Path gamePath = Paths.get("/storage/emulated/0/Android/data/com.YostarJP.BlueArchive/files/")
                .resolve(mapToInGamePath(urlPath));
        String newName = file.getFileName().toString();
        if (!newName.endsWith(".bundle") && !STATIC_FILES.contains(newName)) {
            newName = calculateHash64(newName) + "_" + calculateCRC32(file);
        }
        Path newPath = gamePath.getParent().resolve(newName);
        if (!EscalatedFS.exists(newPath.getParent())) {
            EscalatedFS.createDirectories(newPath.getParent());
        }
        EscalatedFS.copy(file, newPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
