package top.mores.intelligencearchive.server.content.repository;

import java.util.Objects;

/** ResourceManager 中一份 archives 子目录的稳定定位键。 */
record ArchiveResourceKey(String namespace, String directory) {
    ArchiveResourceKey {
        namespace = requireText(namespace, "namespace");
        directory = requireText(directory, "directory");
        if (directory.startsWith("/") || directory.endsWith("/") || directory.contains("..")) {
            throw new IllegalArgumentException("archive directory 非法: " + directory);
        }
    }

    String resourceId() {
        return namespace + ":archives/" + directory;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
