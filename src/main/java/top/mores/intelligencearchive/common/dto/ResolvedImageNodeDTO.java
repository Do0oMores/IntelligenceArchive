package top.mores.intelligencearchive.common.dto;

/** 网络传输的图片引用节点，不包含二进制。 */
public record ResolvedImageNodeDTO(String imageReference) implements ResolvedContentNodeDTO {
    public ResolvedImageNodeDTO {
        imageReference = ResolvedContentDtoValidation.requireText(
                imageReference,
                "imageReference",
                ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH,
                false
        );
    }

    @Override
    public ResolvedContentNodeDTOType type() {
        return ResolvedContentNodeDTOType.IMAGE;
    }
}
