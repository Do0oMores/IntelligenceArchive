package top.mores.intelligencearchive.common.investigation.view;

/** 已被当前玩家发现的 Evidence 摘要，不包含发现条件、关联目标或隐藏信息。 */
public record InvestigationEvidenceView(
        String evidenceId,
        String title,
        String sourceType,
        String importance,
        boolean discovered
) {
    public InvestigationEvidenceView {
        evidenceId = InvestigationViewValidation.requireText(evidenceId, "evidenceId");
        title = InvestigationViewValidation.requireText(title, "title");
        sourceType = InvestigationViewValidation.requireText(sourceType, "sourceType");
        importance = InvestigationViewValidation.requireText(importance, "importance");
        if (!discovered) {
            throw new IllegalArgumentException("未发现 Evidence 不应进入玩家调查视图");
        }
    }
}
