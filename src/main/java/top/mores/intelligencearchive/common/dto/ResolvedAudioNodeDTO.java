package top.mores.intelligencearchive.common.dto;

/** 网络传输的音频引用节点，不包含声音数据。 */
public record ResolvedAudioNodeDTO(String audioReference) implements ResolvedContentNodeDTO {
    public ResolvedAudioNodeDTO {
        audioReference = ResolvedContentDtoValidation.requireText(
                audioReference,
                "audioReference",
                ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH,
                false
        );
    }

    @Override
    public ResolvedContentNodeDTOType type() {
        return ResolvedContentNodeDTOType.AUDIO;
    }
}
