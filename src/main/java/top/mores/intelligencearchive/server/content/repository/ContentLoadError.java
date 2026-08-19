package top.mores.intelligencearchive.server.content.repository;

import java.util.Objects;

/** 单个资源目录的受控加载错误，不向调用方传播解析异常。 */
public record ContentLoadError(String resourceId, String message) {
    public ContentLoadError {
        resourceId = requireText(resourceId, "resourceId");
        message = requireText(message, "message");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
