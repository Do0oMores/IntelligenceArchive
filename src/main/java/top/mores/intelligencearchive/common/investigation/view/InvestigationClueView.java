package top.mores.intelligencearchive.common.investigation.view;

import top.mores.intelligencearchive.common.casefile.model.ClueImportance;
import top.mores.intelligencearchive.common.casefile.model.ClueReliability;

import java.util.Objects;

/** 已由当前玩家产生的 Clue 摘要，不暴露 derivation rules 或 sourceEvidenceIds。 */
public record InvestigationClueView(
        String clueId,
        String title,
        ClueImportance importance,
        ClueReliability reliability
) {
    public InvestigationClueView {
        clueId = InvestigationViewValidation.requireText(clueId, "clueId");
        title = InvestigationViewValidation.requireText(title, "title");
        importance = Objects.requireNonNull(importance, "importance 不能为 null");
        reliability = Objects.requireNonNull(reliability, "reliability 不能为 null");
    }
}
