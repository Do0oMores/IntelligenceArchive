package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelNode;

import java.util.List;
import java.util.Optional;

/**
 * 情报持久化端口。
 *
 * <p>Repository 只描述存取能力，不包含数据库、Redis 或文件格式细节。业务调用方通过
 * {@link IntelService} 访问情报，未来替换存储实现时不会把持久化细节扩散到网络和 UI。</p>
 */
public interface IntelRepository {
    Optional<ArchiveDocument> findDocumentById(String documentId);

    Optional<IntelNode> findNodeById(String nodeId);

    List<IntelEdge> findRelations(String nodeId);

    void saveDocument(ArchiveDocument document);

    void saveNode(IntelNode node);

    void saveRelation(IntelEdge edge);
}
