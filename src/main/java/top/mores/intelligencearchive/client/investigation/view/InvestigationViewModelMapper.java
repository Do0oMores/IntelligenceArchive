package top.mores.intelligencearchive.client.investigation.view;

import top.mores.intelligencearchive.common.dto.investigation.PlayerInvestigationViewDTO;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 单向 DTO → ViewModel 映射边界。
 *
 * <p>Screen 不接触 DTO；客户端也没有 ViewModel → DTO 的反向能力，因而不能借 UI
 * 构造或回传服务器调查状态。</p>
 */
public final class InvestigationViewModelMapper {
    private InvestigationViewModelMapper() {
    }

    public static InvestigationViewModel fromDto(PlayerInvestigationViewDTO dto) {
        Objects.requireNonNull(dto, "dto 不能为 null");
        Map<String, String> nodeNames = dto.nodes().stream().collect(Collectors.toUnmodifiableMap(
                node -> node.intelId(),
                node -> node.displayName(),
                (first, ignored) -> first
        ));
        return new InvestigationViewModel(
                new CaseSummaryViewModel(dto.caseId(), dto.caseTitle(), dto.caseStatus()),
                dto.nodes().stream().map(node -> new IntelCardViewModel(
                        node.intelId(), node.displayName(), node.type(), node.importance()
                )).toList(),
                dto.relations().stream().map(relation -> new InvestigationRelationViewModel(
                        relation.sourceId(),
                        nodeNames.getOrDefault(relation.sourceId(), relation.sourceId()),
                        relation.targetId(),
                        nodeNames.getOrDefault(relation.targetId(), relation.targetId()),
                        relation.relationType(),
                        relation.confidence(),
                        relation.createdTime()
                )).toList(),
                dto.evidence().stream().map(item -> new EvidenceCardViewModel(
                        item.id(), item.title(), item.sourceType(), item.importance()
                )).toList(),
                dto.clues().stream().map(item -> new ClueCardViewModel(
                        item.id(), item.title(), item.importance(), item.reliability()
                )).toList(),
                dto.hypotheses().stream().map(item -> new HypothesisCardViewModel(
                        item.id(), item.title(), item.status(), item.confidence()
                )).toList(),
                dto.timestamp()
        );
    }
}
