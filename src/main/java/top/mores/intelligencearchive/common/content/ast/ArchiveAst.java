package top.mores.intelligencearchive.common.content.ast;

import java.util.List;

/** 一次 Markdown 解析产生的不可变、有序中间语法树。 */
public record ArchiveAst(List<ArchiveAstNode> nodes) {
    public ArchiveAst {
        nodes = AstValidation.immutableNodes(nodes);
    }
}
