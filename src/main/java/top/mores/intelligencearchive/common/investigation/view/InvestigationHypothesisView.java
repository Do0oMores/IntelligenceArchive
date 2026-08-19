package top.mores.intelligencearchive.common.investigation.view;

import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;

import java.util.Objects;

/** 玩家已经形成进度记录的假设；不包含验证条件、反驳条件或世界真相。 */
public record InvestigationHypothesisView(
        String hypothesisId,
        String title,
        HypothesisStatus status,
        String confidence
) {
    public InvestigationHypothesisView {
        hypothesisId = InvestigationViewValidation.requireText(hypothesisId, "hypothesisId");
        title = InvestigationViewValidation.requireText(title, "title");
        status = Objects.requireNonNull(status, "status 不能为 null");
        confidence = InvestigationViewValidation.requireText(confidence, "confidence");
    }
}
