package top.mores.intelligencearchive.server.mapper;

import top.mores.intelligencearchive.common.dto.ArchiveDocumentDTO;
import top.mores.intelligencearchive.common.model.ArchiveDocument;

import java.util.Objects;

/**
 * 将服务端领域档案转换为网络 DTO。
 *
 * <p>转换集中在 Mapper，Packet 不需要理解领域模型，Screen 也不会依赖服务端对象。</p>
 */
public final class ArchiveDocumentMapper {
    private ArchiveDocumentMapper() {
    }

    public static ArchiveDocumentDTO toDto(ArchiveDocument document) {
        ArchiveDocument source = Objects.requireNonNull(document, "document 不能为 null");
        return new ArchiveDocumentDTO(
                source.id(),
                source.title(),
                source.type().name(),
                source.summary(),
                source.contentReference(),
                source.metadata().createdTime().toEpochMilli(),
                source.metadata().author(),
                source.metadata().securityLevel().name()
        );
    }
}
