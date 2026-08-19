package top.mores.intelligencearchive.common.casefile.model;

/**
 * 案件中的一条调查主题。
 *
 * <p>Thread 只组织内容，不保存阶段游标，也不强制玩家按线性顺序调查。</p>
 */
public record InvestigationThreadDefinition(
        String id,
        String title,
        String question,
        String description
) {
    public InvestigationThreadDefinition {
        id = CaseModelValidation.requireId(id, "id");
        title = CaseModelValidation.requireId(title, "title");
        question = CaseModelValidation.requireId(question, "question");
        description = CaseModelValidation.requireText(description, "description");
    }
}
