package top.mores.intelligencearchive.common.dto;

/** 网络传输的可见文本节点。 */
public record ResolvedTextNodeDTO(String text) implements ResolvedContentNodeDTO {
    public ResolvedTextNodeDTO {
        text = ResolvedContentDtoValidation.requireText(
                text,
                "text",
                ResolvedArchiveContentDTO.MAX_TEXT_LENGTH,
                false
        );
    }

    @Override
    public ResolvedContentNodeDTOType type() {
        return ResolvedContentNodeDTOType.TEXT;
    }
}
