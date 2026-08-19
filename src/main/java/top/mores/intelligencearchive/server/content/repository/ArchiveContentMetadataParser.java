package top.mores.intelligencearchive.server.content.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * metadata.yml 的最小 YAML 子集解析器。
 *
 * <p>本阶段只接受顶层 {@code key: scalar}；不引入完整 YAML 库，避免为四个字段扩大运行时依赖。</p>
 */
final class ArchiveContentMetadataParser {
    ArchiveContentMetadata parse(String yaml) {
        Objects.requireNonNull(yaml, "metadata yaml 不能为 null");
        Map<String, String> values = new LinkedHashMap<>();
        String normalized = yaml.startsWith("\uFEFF") ? yaml.substring(1) : yaml;
        String[] lines = normalized.split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException("metadata 第 " + (index + 1) + " 行不是 key: value");
            }
            String key = line.substring(0, separator).trim();
            String value = unquote(line.substring(separator + 1).trim());
            if (values.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("metadata 字段重复: " + key);
            }
        }
        return new ArchiveContentMetadata(
                required(values, "documentId"),
                required(values, "title"),
                required(values, "type"),
                required(values, "version")
        );
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("metadata 缺少字段: " + key);
        }
        return value;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
