package top.mores.intelligencearchive.common.content.loader;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.ast.ArchiveAst;
import top.mores.intelligencearchive.common.content.converter.ArchiveAstConverter;
import top.mores.intelligencearchive.common.content.parser.MarkdownParser;

import java.util.Objects;

/** 按 Source → Parser → AST → Converter 顺序编排完整 Markdown 内容生产链。 */
public final class MarkdownContentLoader implements ArchiveContentLoader {
    private final MarkdownParser parser;
    private final ArchiveAstConverter converter;

    public MarkdownContentLoader() {
        this(new MarkdownParser(), new ArchiveAstConverter());
    }

    public MarkdownContentLoader(MarkdownParser parser, ArchiveAstConverter converter) {
        this.parser = Objects.requireNonNull(parser, "parser 不能为 null");
        this.converter = Objects.requireNonNull(converter, "converter 不能为 null");
    }

    @Override
    public ArchiveContent load(ArchiveContentSource source) {
        ArchiveContentSource validSource = Objects.requireNonNull(source, "source 不能为 null");
        ArchiveAst ast = parser.parse(validSource.readMarkdown());
        return converter.convert(
                validSource.contentId(),
                validSource.documentId(),
                validSource.version(),
                ast
        );
    }
}
