package top.mores.intelligencearchive.common.content.loader;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.AudioContentNode;
import top.mores.intelligencearchive.common.content.ContentNode;
import top.mores.intelligencearchive.common.content.ImageContentNode;
import top.mores.intelligencearchive.common.content.IntelLinkContentNode;
import top.mores.intelligencearchive.common.content.RedactedContentNode;
import top.mores.intelligencearchive.common.content.TextContentNode;
import top.mores.intelligencearchive.common.content.ast.ArchiveAst;
import top.mores.intelligencearchive.common.content.ast.AstAudioNode;
import top.mores.intelligencearchive.common.content.ast.AstHeadingNode;
import top.mores.intelligencearchive.common.content.ast.AstImageNode;
import top.mores.intelligencearchive.common.content.ast.AstIntelLinkNode;
import top.mores.intelligencearchive.common.content.ast.AstRedactedNode;
import top.mores.intelligencearchive.common.content.ast.AstTextNode;
import top.mores.intelligencearchive.common.content.converter.ArchiveAstConverter;
import top.mores.intelligencearchive.common.content.parser.MarkdownParseException;
import top.mores.intelligencearchive.common.content.parser.MarkdownParser;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 3-B Markdown → AST → ArchiveContent 完整链路测试。 */
class ArchiveContentLoaderTest {
    private final MarkdownParser parser = new MarkdownParser();
    private final ArchiveAstConverter converter = new ArchiveAstConverter();

    @Test
    void parsesPlainText() {
        ArchiveAst ast = parser.parse("事故发生于03:17。");

        AstTextNode node = assertInstanceOf(AstTextNode.class, ast.nodes().get(0));
        assertEquals("事故发生于03:17。", node.text());
    }

    @Test
    void parsesImageSyntax() {
        ArchiveAst ast = parser.parse("![photo](archive/image/lab001.png)");

        AstImageNode node = assertInstanceOf(AstImageNode.class, ast.nodes().get(0));
        assertEquals("photo", node.altText());
        assertEquals("archive/image/lab001.png", node.imageReference());
    }

    @Test
    void parsesAudioExtensionSyntax() {
        ArchiveAst ast = parser.parse("[audio:archive/audio/radio001.ogg]");

        AstAudioNode node = assertInstanceOf(AstAudioNode.class, ast.nodes().get(0));
        assertEquals("archive/audio/radio001.ogg", node.audioReference());
    }

    @Test
    void parsesIntelLinkExtensionSyntax() {
        ArchiveAst ast = parser.parse("[intel:node.location.lab]");

        AstIntelLinkNode node = assertInstanceOf(AstIntelLinkNode.class, ast.nodes().get(0));
        assertEquals("node.location.lab", node.targetIntelId());
    }

    @Test
    void redactedBlockDiscardsHiddenSourceText() {
        String hiddenText = "项目负责人为 张三。";
        ArchiveAst ast = parser.parse("""
                [redacted condition="security.level3"]
                项目负责人为 张三。
                [/redacted]
                """);

        assertEquals(1, ast.nodes().size());
        AstRedactedNode node = assertInstanceOf(AstRedactedNode.class, ast.nodes().get(0));
        assertEquals("security.level3", node.conditionReference());
        assertFalse(node.toString().contains(hiddenText));

        ArchiveContent content = converter.convert("content.secret.v1", "document.secret", "v1", ast);
        RedactedContentNode contentNode = assertInstanceOf(
                RedactedContentNode.class,
                content.nodes().get(0)
        );
        assertFalse(contentNode.toString().contains(hiddenText));
    }

    @Test
    void parsesComplexMixedArchiveMarkdownInOrder() {
        ArchiveAst ast = parser.parse("""
                # 实验报告

                事故发生于03:17。
                ![现场照片](archive/image/lab001.png)
                [audio:archive/audio/radio001.ogg]
                [intel:node.location.lab]
                [redacted condition="security.level3"]
                不应进入AST的秘密内容
                [/redacted]
                """);

        assertEquals(6, ast.nodes().size());
        assertInstanceOf(AstHeadingNode.class, ast.nodes().get(0));
        assertInstanceOf(AstTextNode.class, ast.nodes().get(1));
        assertInstanceOf(AstImageNode.class, ast.nodes().get(2));
        assertInstanceOf(AstAudioNode.class, ast.nodes().get(3));
        assertInstanceOf(AstIntelLinkNode.class, ast.nodes().get(4));
        assertInstanceOf(AstRedactedNode.class, ast.nodes().get(5));
        assertTrue(ast.nodes().stream().noneMatch(node -> node.toString().contains("秘密内容")));
    }

    @Test
    void converterProducesCorrectContentNodeTypes() {
        ArchiveAst ast = parser.parse("""
                ## 标题
                正文
                ![image](archive/image/test.png)
                [audio:archive/audio/test.ogg]
                [intel:node.test]
                [redacted]
                hidden
                [/redacted]
                """);

        ArchiveContent content = converter.convert("content.test.v1", "document.test", "v1", ast);

        List<Class<? extends ContentNode>> expectedTypes = List.of(
                TextContentNode.class,
                TextContentNode.class,
                ImageContentNode.class,
                AudioContentNode.class,
                IntelLinkContentNode.class,
                RedactedContentNode.class
        );
        assertEquals(expectedTypes.size(), content.nodes().size());
        for (int index = 0; index < expectedTypes.size(); index++) {
            assertTrue(expectedTypes.get(index).isInstance(content.nodes().get(index)));
        }
    }

    @Test
    void markdownLoaderRunsFullPipelineAndPreservesMetadata() {
        ArchiveContentSource source = new StringArchiveContentSource(
                "content.case.lab.v2",
                "document.case.lab",
                "v2",
                "# 实验报告\n正文"
        );

        ArchiveContent content = new MarkdownContentLoader().load(source);

        assertEquals("content.case.lab.v2", content.contentId());
        assertEquals("document.case.lab", content.documentId());
        assertEquals("v2", content.version());
        assertEquals(2, content.nodes().size());
    }

    @Test
    void parserAndAstHaveNoMinecraftOrForgeFieldTypes() {
        List<Class<?>> pureTypes = List.of(
                MarkdownParser.class,
                ArchiveAst.class,
                AstTextNode.class,
                AstHeadingNode.class,
                AstImageNode.class,
                AstAudioNode.class,
                AstIntelLinkNode.class,
                AstRedactedNode.class
        );

        for (Class<?> pureType : pureTypes) {
            for (Field field : pureType.getDeclaredFields()) {
                String fieldType = field.getType().getName();
                assertFalse(fieldType.startsWith("net.minecraft"));
                assertFalse(fieldType.startsWith("net.minecraftforge"));
            }
        }
    }

    @Test
    void malformedOrUnclosedRedactedBlocksFailWithoutEchoingSecret() {
        String secret = "NEVER_LOG_THIS_SECRET";

        MarkdownParseException malformed = assertThrows(
                MarkdownParseException.class,
                () -> parser.parse("[redacted condition=level3]\n" + secret + "\n[/redacted]")
        );
        MarkdownParseException unclosed = assertThrows(
                MarkdownParseException.class,
                () -> parser.parse("[redacted condition=\"level3\"]\n" + secret)
        );

        assertFalse(malformed.getMessage().contains(secret));
        assertFalse(unclosed.getMessage().contains(secret));
    }

    @Test
    void astNodeCollectionIsImmutable() {
        ArchiveAst ast = parser.parse("正文");

        assertThrows(UnsupportedOperationException.class,
                () -> ast.nodes().add(new AstTextNode("非法修改")));
    }
}
