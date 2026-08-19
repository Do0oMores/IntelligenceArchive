package top.mores.intelligencearchive.common.dto.investigation;

/** 已产生 Clue 的网络摘要，不包含来源 Evidence、派生规则或 Hypothesis 关系。 */
public record InvestigationClueViewDTO(
        String id,
        String title,
        String importance,
        String reliability
) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_ENUM_NAME_LENGTH = 64;

    public InvestigationClueViewDTO {
        id = InvestigationViewDtoValidation.requireText(id, "id", MAX_ID_LENGTH);
        title = InvestigationViewDtoValidation.requireText(title, "title", MAX_TITLE_LENGTH);
        importance = InvestigationViewDtoValidation.requireText(
                importance,
                "importance",
                MAX_ENUM_NAME_LENGTH
        );
        reliability = InvestigationViewDtoValidation.requireText(
                reliability,
                "reliability",
                MAX_ENUM_NAME_LENGTH
        );
    }
}
