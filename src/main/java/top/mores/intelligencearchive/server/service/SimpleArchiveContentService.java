package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

/**
 * Phase 3-C-1 的不可变内存 ContentService。
 *
 * <p>每个 documentId 当前只选择一个内容版本；构造后不暴露或修改内部 Map。
 * 未来版本选择和持久化可替换该端口，而 Resolve UseCase 无需改变。</p>
 */
public final class SimpleArchiveContentService implements ArchiveContentService {
    private final Map<String, ArchiveContent> contentsByDocumentId;

    public SimpleArchiveContentService(Collection<ArchiveContent> contents) {
        Objects.requireNonNull(contents, "contents 不能为 null");
        Map<String, ArchiveContent> indexedContents = new LinkedHashMap<>();
        for (ArchiveContent content : contents) {
            ArchiveContent validContent = Objects.requireNonNull(content, "contents 不能包含 null");
            ArchiveContent previous = indexedContents.put(validContent.documentId(), validContent);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "同一 documentId 不能配置多个当前内容版本: " + validContent.documentId()
                );
            }
        }
        contentsByDocumentId = Map.copyOf(indexedContents);
    }

    @Override
    public Optional<ArchiveContent> findByDocumentId(String documentId) {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (documentId.isBlank()) {
            throw new IllegalArgumentException("documentId 不能为空");
        }
        return Optional.ofNullable(contentsByDocumentId.get(documentId));
    }

    @Override
    public List<String> findDocumentIds() {
        return contentsByDocumentId.keySet().stream().sorted().toList();
    }
}
