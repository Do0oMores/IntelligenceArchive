package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.repository.ArchiveContentRepository;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;

import java.util.Objects;
import java.util.Optional;
import java.util.List;

/**
 * Repository 到 Application ContentService 端口的薄适配器。
 *
 * <p>Service 不读取文件、不解析 Markdown，也不复制缓存策略；这些都属于 Repository 基础设施实现。</p>
 */
public final class RepositoryArchiveContentService implements ArchiveContentService {
    private final ArchiveContentRepository repository;

    public RepositoryArchiveContentService(ArchiveContentRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository 不能为 null");
    }

    @Override
    public Optional<ArchiveContent> findByDocumentId(String documentId) {
        return repository.findContent(documentId);
    }

    @Override
    public List<String> findDocumentIds() {
        return repository.findDocumentIds();
    }
}
