package top.mores.intelligencearchive.common.content.ast;

/**
 * Markdown 结构的多态中间节点。
 *
 * <p>AST 描述策划文本采用了什么语法；ContentNode 描述游戏最终可消费的内容。
 * 两者分离后，Parser 不需要知道游戏模型，Converter 也可以独立演进映射规则。</p>
 */
public interface ArchiveAstNode {
    AstNodeType type();
}
