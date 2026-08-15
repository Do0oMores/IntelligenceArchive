package top.mores.intelligencearchive.common.dto;

/** 网络传输的情报链接 ID。 */
public record ResolvedIntelLinkNodeDTO(String targetIntelId) implements ResolvedContentNodeDTO {
    public ResolvedIntelLinkNodeDTO {
        targetIntelId = ResolvedContentDtoValidation.requireText(
                targetIntelId,
                "targetIntelId",
                ResolvedArchiveContentDTO.MAX_ID_LENGTH,
                false
        );
    }

    @Override
    public ResolvedContentNodeDTOType type() {
        return ResolvedContentNodeDTOType.INTEL_LINK;
    }
}
