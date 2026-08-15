package top.mores.intelligencearchive.common.content.parser;

/** 受限 Markdown 子集无法安全解析时抛出的明确异常。 */
public final class MarkdownParseException extends IllegalArgumentException {
    public MarkdownParseException(String message) {
        super(message);
    }
}
