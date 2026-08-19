package top.mores.intelligencearchive.client.view;

import top.mores.intelligencearchive.common.dto.ArchiveSummaryDTO;

import java.util.Objects;

/** 将网络摘要转换为 UI 模型，Screen 不直接依赖 Packet。 */
public final class ArchiveSummaryViewModelMapper {
    private ArchiveSummaryViewModelMapper() {
    }

    public static ArchiveSummaryViewModel fromDto(ArchiveSummaryDTO dto) {
        ArchiveSummaryDTO source = Objects.requireNonNull(dto, "dto 不能为 null");
        return new ArchiveSummaryViewModel(
                source.documentId(),
                source.title(),
                source.type(),
                source.securityLevel(),
                source.summary(),
                source.status(),
                source.version()
        );
    }
}
