package top.mores.intelligencearchive.common.content.repository;

import top.mores.intelligencearchive.common.content.ArchiveContent;

import java.util.List;
import java.util.Optional;

/**
 * ArchiveContent 的只读存储端口。
 *
 * <p>Repository 独立于 Markdown 与 Minecraft ResourceManager：Domain/Application 只依赖查询语义，
 * 资源包、数据库或远程来源等基础设施实现可以替换，而不需要修改内容模型。</p>
 */
public interface ArchiveContentRepository {
    Optional<ArchiveContent> findContent(String documentId);

    List<String> findDocumentIds();

    default boolean exists(String documentId) {
        return findContent(documentId).isPresent();
    }
}
