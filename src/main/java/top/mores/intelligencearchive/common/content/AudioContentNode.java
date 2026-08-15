package top.mores.intelligencearchive.common.content;

/**
 * 录音资源的逻辑引用。
 *
 * <p>模型只描述录音位于何处，不负责读取、解码或播放声音。</p>
 */
public record AudioContentNode(String audioReference) implements ContentNode {
    public AudioContentNode {
        audioReference = ContentValidation.requireNonBlank(audioReference, "audioReference");
    }

    @Override
    public ContentNodeType type() {
        return ContentNodeType.AUDIO;
    }
}
