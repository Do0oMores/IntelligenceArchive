package top.mores.intelligencearchive.common.content.service;

import top.mores.intelligencearchive.common.content.ArchiveContent;

import java.util.Optional;
import java.util.List;

/** Application 查询当前档案内容版本的只读端口。 */
public interface ArchiveContentService {
    Optional<ArchiveContent> findByDocumentId(String documentId);

    /** 返回当前可查询内容的文档 ID；具体玩家可见性由索引服务另行裁决。 */
    List<String> findDocumentIds();
}
