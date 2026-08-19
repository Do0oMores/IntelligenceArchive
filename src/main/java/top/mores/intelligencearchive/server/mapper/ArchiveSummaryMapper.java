package top.mores.intelligencearchive.server.mapper;

import top.mores.intelligencearchive.common.dto.ArchiveSummaryDTO;
import top.mores.intelligencearchive.common.model.ArchiveSummary;

import java.util.Objects;

/** 将服务端可见摘要转换为稳定网络 DTO，Packet 不直接依赖领域对象。 */
public final class ArchiveSummaryMapper {
    private ArchiveSummaryMapper() {
    }

    public static ArchiveSummaryDTO toDto(ArchiveSummary summary) {
        ArchiveSummary source = Objects.requireNonNull(summary, "summary 不能为 null");
        return new ArchiveSummaryDTO(
                source.documentId(),
                source.title(),
                source.type().name(),
                source.securityLevel().name(),
                source.summary(),
                source.status(),
                source.version()
        );
    }
}
