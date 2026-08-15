package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelNode;

import java.util.List;
import java.util.Optional;

/**
 * 情报领域的统一查询入口。
 *
 * <p>UI、网络、GameCore 或未来 Bukkit Bridge 应依赖这个接口，而不是直接访问 Repository。
 * Service 边界可以集中承载权限、解锁和剧情规则，同时允许底层存储实现独立替换。</p>
 */
public interface IntelService {
    Optional<ArchiveDocument> findDocumentById(String documentId);

    Optional<IntelNode> findNodeById(String nodeId);

    /** 返回所有以该节点为起点或终点的关系。 */
    List<IntelEdge> findRelations(String nodeId);

    boolean existsDocument(String documentId);
}
