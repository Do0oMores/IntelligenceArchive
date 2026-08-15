package top.mores.intelligencearchive.common.content.resolution;

/** 已允许玩家看到的音频引用；本节点不加载、验证或播放资源。 */
public record ResolvedAudioNode(String audioReference) implements ResolvedContentNode {
    public ResolvedAudioNode {
        audioReference = ResolutionValidation.requireNonBlank(audioReference, "audioReference");
    }

    @Override
    public ResolvedContentNodeType type() {
        return ResolvedContentNodeType.AUDIO;
    }
}
