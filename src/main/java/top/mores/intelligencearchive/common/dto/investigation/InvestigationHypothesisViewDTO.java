package top.mores.intelligencearchive.common.dto.investigation;

/** 玩家 Hypothesis 进度的最小网络表示，不包含验证规则、反驳规则或真相。 */
public record InvestigationHypothesisViewDTO(String id, String title, String status, String confidence) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_STATUS_LENGTH = 64;
    public static final int MAX_TITLE_LENGTH = 256;

    public InvestigationHypothesisViewDTO {
        id = InvestigationViewDtoValidation.requireText(id, "id", MAX_ID_LENGTH);
        title = InvestigationViewDtoValidation.requireText(title, "title", MAX_TITLE_LENGTH);
        status = InvestigationViewDtoValidation.requireText(status, "status", MAX_STATUS_LENGTH);
        confidence = InvestigationViewDtoValidation.requireText(
                confidence,
                "confidence",
                MAX_STATUS_LENGTH
        );
    }
}
