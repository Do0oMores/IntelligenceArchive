package top.mores.intelligencearchive.server.content.repository;

import java.util.List;
import java.util.Objects;

/** 一次服务器资源扫描的不可变汇总。 */
public record ContentLoadReport(
        int loaded,
        int failed,
        List<String> loadedDocumentIds,
        List<ContentLoadError> errors
) {
    public ContentLoadReport {
        if (loaded < 0 || failed < 0) {
            throw new IllegalArgumentException("loaded/failed 不能为负数");
        }
        Objects.requireNonNull(loadedDocumentIds, "loadedDocumentIds 不能为 null");
        Objects.requireNonNull(errors, "errors 不能为 null");
        loadedDocumentIds = List.copyOf(loadedDocumentIds);
        errors = List.copyOf(errors);
        if (loaded != loadedDocumentIds.size() || failed != errors.size()) {
            throw new IllegalArgumentException("加载报告计数与明细不一致");
        }
    }

    public static ContentLoadReport empty() {
        return new ContentLoadReport(0, 0, List.of(), List.of());
    }
}
