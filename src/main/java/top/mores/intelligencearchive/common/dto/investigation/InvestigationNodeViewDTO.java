package top.mores.intelligencearchive.common.dto.investigation;

/** 已知节点的网络投影；不包含描述、metadata、隐藏信息或世界关系。 */
public record InvestigationNodeViewDTO(
        String intelId,
        String displayName,
        String type,
        String status,
        String importance
) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_DISPLAY_NAME_LENGTH = 256;
    public static final int MAX_ENUM_NAME_LENGTH = 64;

    public InvestigationNodeViewDTO {
        intelId = InvestigationViewDtoValidation.requireText(intelId, "intelId", MAX_ID_LENGTH);
        displayName = InvestigationViewDtoValidation.requireText(
                displayName,
                "displayName",
                MAX_DISPLAY_NAME_LENGTH
        );
        type = InvestigationViewDtoValidation.requireText(type, "type", MAX_ENUM_NAME_LENGTH);
        status = InvestigationViewDtoValidation.requireText(status, "status", MAX_ENUM_NAME_LENGTH);
        importance = InvestigationViewDtoValidation.requireText(
                importance,
                "importance",
                MAX_ENUM_NAME_LENGTH
        );
    }
}
