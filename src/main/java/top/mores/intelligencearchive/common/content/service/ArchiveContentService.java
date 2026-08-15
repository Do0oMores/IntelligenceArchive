package top.mores.intelligencearchive.common.content.service;

import top.mores.intelligencearchive.common.content.ArchiveContent;

import java.util.Optional;

/** Application 查询当前档案内容版本的只读端口。 */
public interface ArchiveContentService {
    Optional<ArchiveContent> findByDocumentId(String documentId);
}
