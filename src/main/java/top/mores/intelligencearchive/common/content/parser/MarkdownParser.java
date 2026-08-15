package top.mores.intelligencearchive.common.content.parser;

import top.mores.intelligencearchive.common.content.ast.ArchiveAst;
import top.mores.intelligencearchive.common.content.ast.ArchiveAstNode;
import top.mores.intelligencearchive.common.content.ast.AstAudioNode;
import top.mores.intelligencearchive.common.content.ast.AstHeadingNode;
import top.mores.intelligencearchive.common.content.ast.AstImageNode;
import top.mores.intelligencearchive.common.content.ast.AstIntelLinkNode;
import top.mores.intelligencearchive.common.content.ast.AstRedactedNode;
import top.mores.intelligencearchive.common.content.ast.AstTextNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 调查档案使用的轻量、行级 Markdown 子集解析器。
 *
 * <p>Parser 只输出 AST，不创建 ContentNode，更不引用 UI。Markdown 应在服务端受控内容源上解析，
 * 客户端未来只消费服务端生成的可见 ContentNode，避免自行改写源文本或绕过隐藏规则。</p>
 */
public final class MarkdownParser {
    public static final String DEFAULT_REDACTED_PLACEHOLDER = "██████";
    public static final String UNSPECIFIED_CONDITION = "condition.unspecified";

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern IMAGE = Pattern.compile("^!\\[([^\\]]*)\\]\\(([^)]+)\\)$");
    private static final Pattern AUDIO = Pattern.compile("^\\[audio:([^\\]]+)\\]$");
    private static final Pattern INTEL_LINK = Pattern.compile("^\\[intel:([^\\]]+)\\]$");
    private static final Pattern REDACTED_OPEN = Pattern.compile(
            "^\\[redacted(?:\\s+condition=\"([^\"]+)\")?\\]$"
    );

    public ArchiveAst parse(String markdown) {
        Objects.requireNonNull(markdown, "markdown 不能为 null");
        String[] lines = markdown.split("\\R", -1);
        List<ArchiveAstNode> nodes = new ArrayList<>();
        boolean insideRedacted = false;

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            String trimmed = line.strip();
            int lineNumber = index + 1;

            if (insideRedacted) {
                if (trimmed.equals("[/redacted]")) {
                    insideRedacted = false;
                } else if (trimmed.startsWith("[redacted")) {
                    throw parseError(lineNumber, "不支持嵌套 redacted 块");
                }
                // 安全边界：块内原文在 Parser 阶段直接丢弃，绝不进入 AST。
                continue;
            }

            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.equals("[/redacted]")) {
                throw parseError(lineNumber, "存在没有开始标签的 [/redacted]");
            }

            Matcher redactedMatcher = REDACTED_OPEN.matcher(trimmed);
            if (redactedMatcher.matches()) {
                String condition = redactedMatcher.group(1);
                nodes.add(new AstRedactedNode(
                        DEFAULT_REDACTED_PLACEHOLDER,
                        condition == null ? UNSPECIFIED_CONDITION : condition
                ));
                insideRedacted = true;
                continue;
            }
            if (trimmed.startsWith("[redacted")) {
                throw parseError(lineNumber, "redacted 开始标签格式无效");
            }

            Matcher headingMatcher = HEADING.matcher(trimmed);
            if (headingMatcher.matches()) {
                nodes.add(new AstHeadingNode(
                        headingMatcher.group(1).length(),
                        headingMatcher.group(2).strip()
                ));
                continue;
            }

            Matcher imageMatcher = IMAGE.matcher(trimmed);
            if (imageMatcher.matches()) {
                nodes.add(new AstImageNode(
                        imageMatcher.group(1),
                        imageMatcher.group(2).strip()
                ));
                continue;
            }

            Matcher audioMatcher = AUDIO.matcher(trimmed);
            if (audioMatcher.matches()) {
                nodes.add(new AstAudioNode(audioMatcher.group(1).strip()));
                continue;
            }

            Matcher intelLinkMatcher = INTEL_LINK.matcher(trimmed);
            if (intelLinkMatcher.matches()) {
                nodes.add(new AstIntelLinkNode(intelLinkMatcher.group(1).strip()));
                continue;
            }

            nodes.add(new AstTextNode(trimmed));
        }

        if (insideRedacted) {
            throw parseError(lines.length, "redacted 块缺少结束标签");
        }
        return new ArchiveAst(nodes);
    }

    private static MarkdownParseException parseError(int lineNumber, String reason) {
        // 错误消息不包含源行，避免日志意外记录隐藏原文。
        return new MarkdownParseException("Markdown 第 " + lineNumber + " 行解析失败：" + reason);
    }
}
