package top.mores.intelligencearchive.common.content.ast;

/** {@code [audio:reference]} 扩展语法的中间节点。 */
public record AstAudioNode(String audioReference) implements ArchiveAstNode {
    public AstAudioNode {
        audioReference = AstValidation.requireNonBlank(audioReference, "audioReference");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.AUDIO;
    }
}
