package top.mores.intelligencearchive.server.content.repository;

import net.minecraft.server.packs.resources.ResourceManager;
import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.loader.ArchiveContentLoader;
import top.mores.intelligencearchive.common.content.loader.StringArchiveContentSource;
import top.mores.intelligencearchive.common.content.repository.ArchiveContentRepository;
import top.mores.intelligencearchive.common.content.repository.ArchiveDocumentIdRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Minecraft 服务器资源包支持的 ArchiveContentRepository。
 *
 * <p>Repository 只编排资源读取、metadata 校验和已注入 Loader；Markdown 语法仍由
 * ArchiveContentLoader 负责。加载完成后缓存不可变，不向 Service 暴露内部 Map。</p>
 */
public final class ResourceArchiveContentRepository implements ArchiveContentRepository {
    private final Map<String, ArchiveContent> contentsByDocumentId;
    private final List<String> documentIds;
    private final ContentLoadReport loadReport;

    public ResourceArchiveContentRepository(ResourceManager resourceManager, ArchiveContentLoader contentLoader) {
        this(new MinecraftArchiveResourceProvider(resourceManager), contentLoader);
    }

    ResourceArchiveContentRepository(
            ArchiveResourceProvider resourceProvider,
            ArchiveContentLoader contentLoader
    ) {
        Objects.requireNonNull(resourceProvider, "resourceProvider 不能为 null");
        Objects.requireNonNull(contentLoader, "contentLoader 不能为 null");

        Map<String, ArchiveContent> loadedContents = new LinkedHashMap<>();
        List<String> loadedIds = new ArrayList<>();
        List<ContentLoadError> errors = new ArrayList<>();
        List<ArchiveResourceKey> resourceKeys;
        try {
            resourceKeys = new ArrayList<>(resourceProvider.findArchives());
            resourceKeys.sort(Comparator.comparing(ArchiveResourceKey::resourceId));
        } catch (Exception exception) {
            resourceKeys = List.of();
            errors.add(new ContentLoadError("archives", errorMessage(exception)));
        }

        for (ArchiveResourceKey resourceKey : resourceKeys) {
            try {
                ArchiveContentMetadata metadata = new ArchiveContentMetadataParser().parse(
                        resourceProvider.readMetadata(resourceKey)
                );
                if (loadedContents.containsKey(metadata.documentId())) {
                    throw new IllegalArgumentException("documentId 重复: " + metadata.documentId());
                }
                String contentId = resourceKey.namespace()
                        + ":archives/" + resourceKey.directory()
                        + "/" + metadata.version();
                ArchiveContent content = contentLoader.load(new StringArchiveContentSource(
                        contentId,
                        metadata.documentId(),
                        metadata.version(),
                        resourceProvider.readMarkdown(resourceKey)
                ));
                if (!contentId.equals(content.contentId())
                        || !metadata.documentId().equals(content.documentId())
                        || !metadata.version().equals(content.version())) {
                    throw new IllegalArgumentException("ContentLoader 返回了与 metadata 不一致的 ArchiveContent");
                }
                loadedContents.put(metadata.documentId(), content);
                loadedIds.add(metadata.documentId());
            } catch (Exception exception) {
                errors.add(new ContentLoadError(resourceKey.resourceId(), errorMessage(exception)));
            }
        }

        contentsByDocumentId = Map.copyOf(loadedContents);
        loadedIds.sort(String::compareTo);
        documentIds = List.copyOf(loadedIds);
        loadReport = new ContentLoadReport(
                documentIds.size(),
                errors.size(),
                documentIds,
                errors
        );
    }

    @Override
    public Optional<ArchiveContent> findContent(String documentId) {
        return Optional.ofNullable(contentsByDocumentId.get(ArchiveDocumentIdRules.requireValid(documentId)));
    }

    @Override
    public List<String> findDocumentIds() {
        return documentIds;
    }

    public ContentLoadReport loadReport() {
        return loadReport;
    }

    private static String errorMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}
