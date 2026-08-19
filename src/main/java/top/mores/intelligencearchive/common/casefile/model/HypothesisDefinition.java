package top.mores.intelligencearchive.common.casefile.model;

import java.util.List;

/**
 * 内容作者预定义、可由玩家显式验证的解释。
 *
 * <p>定义中没有 isCorrect、isFalseLead 或 actualTruth 字段，避免内容数据向客户端泄漏答案。</p>
 */
public record HypothesisDefinition(
        String id,
        String caseId,
        String threadId,
        String title,
        String description,
        List<InvestigationRequirementSet> availabilityRequirements,
        List<InvestigationRequirementSet> supportRequirements,
        List<InvestigationRequirementSet> confirmationRequirements,
        List<InvestigationRequirementSet> refutationRequirements
) {
    public HypothesisDefinition {
        id = CaseModelValidation.requireId(id, "id");
        caseId = CaseModelValidation.requireId(caseId, "caseId");
        threadId = CaseModelValidation.requireId(threadId, "threadId");
        title = CaseModelValidation.requireId(title, "title");
        description = CaseModelValidation.requireText(description, "description");
        availabilityRequirements = CaseModelValidation.immutableList(availabilityRequirements, "availabilityRequirements");
        supportRequirements = CaseModelValidation.immutableList(supportRequirements, "supportRequirements");
        confirmationRequirements = CaseModelValidation.immutableList(confirmationRequirements, "confirmationRequirements");
        refutationRequirements = CaseModelValidation.immutableList(refutationRequirements, "refutationRequirements");
    }
}
