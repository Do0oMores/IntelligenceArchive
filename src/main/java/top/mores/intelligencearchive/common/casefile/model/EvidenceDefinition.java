package top.mores.intelligencearchive.common.casefile.model;

import java.util.Objects;
import java.util.Set;

/**
 * 玩家可发现的原始材料定义。
 *
 * <p>Evidence 不携带真假或正确结论；调查结论由 Clue 与 Hypothesis 表达。</p>
 */
public record EvidenceDefinition(
        String id,
        String caseId,
        String threadId,
        String title,
        String description,
        EvidenceSourceType sourceType,
        String sourceReference,
        Set<String> relatedArchiveIds,
        Set<String> relatedIntelNodeIds
) {
    public EvidenceDefinition {
        id = CaseModelValidation.requireId(id, "id");
        caseId = CaseModelValidation.requireId(caseId, "caseId");
        threadId = CaseModelValidation.requireId(threadId, "threadId");
        title = CaseModelValidation.requireId(title, "title");
        description = CaseModelValidation.requireText(description, "description");
        sourceType = Objects.requireNonNull(sourceType, "sourceType 不能为 null");
        sourceReference = CaseModelValidation.requireId(sourceReference, "sourceReference");
        relatedArchiveIds = CaseModelValidation.immutableIds(relatedArchiveIds, "relatedArchiveIds");
        relatedIntelNodeIds = CaseModelValidation.immutableIds(relatedIntelNodeIds, "relatedIntelNodeIds");
    }
}
