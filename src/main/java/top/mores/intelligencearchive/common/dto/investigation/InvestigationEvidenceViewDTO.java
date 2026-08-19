package top.mores.intelligencearchive.common.dto.investigation;

/** 已发现 Evidence 的最小网络摘要，不包含 Requirement、Condition 或 SourceArchive。 */
public record InvestigationEvidenceViewDTO(String id, String title, String sourceType, String importance) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_ENUM_NAME_LENGTH = 64;

    public InvestigationEvidenceViewDTO {
        id = InvestigationViewDtoValidation.requireText(id, "id", MAX_ID_LENGTH);
        title = InvestigationViewDtoValidation.requireText(title, "title", MAX_TITLE_LENGTH);
        sourceType = InvestigationViewDtoValidation.requireText(
                sourceType,
                "sourceType",
                MAX_ENUM_NAME_LENGTH
        );
        importance = InvestigationViewDtoValidation.requireText(
                importance,
                "importance",
                MAX_ENUM_NAME_LENGTH
        );
    }
}
