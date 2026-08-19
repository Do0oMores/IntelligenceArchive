package top.mores.intelligencearchive.server.investigation.view;

import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.EvidenceDefinition;
import top.mores.intelligencearchive.common.casefile.model.HypothesisDefinition;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;
import top.mores.intelligencearchive.common.casefile.state.PlayerInvestigationEdge;
import top.mores.intelligencearchive.common.investigation.service.InvestigationViewService;
import top.mores.intelligencearchive.common.investigation.view.InvestigationClueView;
import top.mores.intelligencearchive.common.investigation.view.InvestigationEvidenceView;
import top.mores.intelligencearchive.common.investigation.view.InvestigationHypothesisView;
import top.mores.intelligencearchive.common.investigation.view.InvestigationNodeView;
import top.mores.intelligencearchive.common.investigation.view.InvestigationRelationView;
import top.mores.intelligencearchive.common.investigation.view.InvestigationViewStatus;
import top.mores.intelligencearchive.common.investigation.view.PlayerInvestigationView;
import top.mores.intelligencearchive.common.model.IntelNode;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 将世界定义与玩家状态组合成最小、安全的调查视图。
 *
 * <p>该实现只读取 IntelNode 定义，绝不调用 IntelService.findRelations；关系唯一来源是
 * PlayerInvestigationEdge，因此服务器世界图中的真实关系不会泄漏给玩家。</p>
 */
public final class DefaultInvestigationViewService implements InvestigationViewService {
    private static final String ACTIVE_VIEW_STATUS = "INVESTIGATING";
    private static final String UNSPECIFIED = "UNSPECIFIED";
    private final IntelService intelService;
    private final CaseDefinitionService caseDefinitionService;
    private final CaseInvestigationService caseInvestigationService;
    private final InvestigationService investigationService;
    private final Clock clock;

    public DefaultInvestigationViewService(
            IntelService intelService,
            CaseDefinitionService caseDefinitionService,
            CaseInvestigationService caseInvestigationService,
            InvestigationService investigationService
    ) {
        this(intelService, caseDefinitionService, caseInvestigationService,
                investigationService, Clock.systemUTC());
    }

    /** 可注入 Clock 以保证投影测试和未来请求追踪的时间可重复。 */
    public DefaultInvestigationViewService(
            IntelService intelService,
            CaseDefinitionService caseDefinitionService,
            CaseInvestigationService caseInvestigationService,
            InvestigationService investigationService,
            Clock clock
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.caseDefinitionService = Objects.requireNonNull(
                caseDefinitionService,
                "caseDefinitionService 不能为 null"
        );
        this.caseInvestigationService = Objects.requireNonNull(
                caseInvestigationService,
                "caseInvestigationService 不能为 null"
        );
        this.investigationService = Objects.requireNonNull(
                investigationService,
                "investigationService 不能为 null"
        );
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
    }

    @Override
    public Optional<PlayerInvestigationView> buildView(UUID playerId, String caseId) {
        if (playerId == null || caseId == null || caseId.isBlank()) {
            return Optional.empty();
        }
        CaseDefinition definition = caseDefinitionService.findCase(caseId).orElse(null);
        if (definition == null) {
            return Optional.empty();
        }

        PlayerInvestigationState intelState = investigationService.getPlayerState(playerId);
        PlayerCaseInvestigationState caseState = caseInvestigationService.getState(playerId, caseId);
        List<InvestigationNodeView> nodes = projectNodes(intelState, definition.relatedIntelNodeIds());
        Set<String> visibleNodeIds = nodes.stream()
                .map(InvestigationNodeView::intelId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return Optional.of(new PlayerInvestigationView(
                playerId,
                caseId,
                definition.title(),
                // 当前 Case 模型没有任务生命周期；该值只表示玩家正在查看调查上下文。
                ACTIVE_VIEW_STATUS,
                nodes,
                projectRelations(caseState.investigationEdges(), visibleNodeIds),
                projectEvidence(definition, caseState.discoveredEvidenceIds()),
                projectClues(definition, caseState.discoveredClueIds()),
                projectHypotheses(definition, caseState.hypothesisStatuses()),
                clock.instant()
        ));
    }

    private List<InvestigationNodeView> projectNodes(
            PlayerInvestigationState state,
            Set<String> caseNodeIds
    ) {
        return state.discoveredIntels().stream()
                .filter(record -> caseNodeIds.contains(record.intelId()))
                .map(record -> intelService.findNodeById(record.intelId())
                        .map(node -> toNodeView(node, record)))
                .flatMap(Optional::stream)
                .toList();
    }

    private static InvestigationNodeView toNodeView(IntelNode node, IntelDiscoveryRecord record) {
        return new InvestigationNodeView(
                node.id(),
                node.name(),
                node.type(),
                mapStatus(record.status()),
                // IntelNode 尚无重要度字段，服务端明确发送未知值，客户端不得自行推断。
                UNSPECIFIED
        );
    }

    private static InvestigationViewStatus mapStatus(IntelDiscoveryStatus status) {
        return switch (status) {
            case DISCOVERED -> InvestigationViewStatus.DISCOVERED;
            case READ -> InvestigationViewStatus.READ;
            case VERIFIED -> InvestigationViewStatus.VERIFIED;
            case ARCHIVED -> InvestigationViewStatus.ARCHIVED;
            case UNKNOWN -> throw new IllegalArgumentException("UNKNOWN Intel 不应拥有发现记录");
        };
    }

    private static List<InvestigationRelationView> projectRelations(
            List<PlayerInvestigationEdge> edges,
            Set<String> visibleNodeIds
    ) {
        return edges.stream()
                // 关系虽然属于玩家，但两个端点仍必须已经可见，避免通过 ID 泄漏未知节点。
                .filter(edge -> visibleNodeIds.contains(edge.sourceIntelNodeId())
                        && visibleNodeIds.contains(edge.targetIntelNodeId()))
                .map(edge -> new InvestigationRelationView(
                        edge.sourceIntelNodeId(),
                        edge.targetIntelNodeId(),
                        edge.relationType(),
                        edge.confidence(),
                        edge.createdAt()
                ))
                .toList();
    }

    private static List<InvestigationEvidenceView> projectEvidence(
            CaseDefinition definition,
            Set<String> discoveredEvidenceIds
    ) {
        return definition.evidence().stream()
                .filter(evidence -> discoveredEvidenceIds.contains(evidence.id()))
                .map(DefaultInvestigationViewService::toEvidenceView)
                .toList();
    }

    private static InvestigationEvidenceView toEvidenceView(EvidenceDefinition evidence) {
        return new InvestigationEvidenceView(
                evidence.id(),
                evidence.title(),
                evidence.sourceType().name(),
                // EvidenceDefinition 当前没有重要度字段，保持真实数据边界。
                UNSPECIFIED,
                true
        );
    }

    private static List<InvestigationClueView> projectClues(
            CaseDefinition definition,
            Set<String> discoveredClueIds
    ) {
        return definition.clues().stream()
                .filter(clue -> discoveredClueIds.contains(clue.id()))
                .map(DefaultInvestigationViewService::toClueView)
                .toList();
    }

    private static InvestigationClueView toClueView(ClueDefinition clue) {
        return new InvestigationClueView(clue.id(), clue.title(), clue.importance(), clue.reliability());
    }

    private static List<InvestigationHypothesisView> projectHypotheses(
            CaseDefinition definition,
            Map<String, HypothesisStatus> statuses
    ) {
        return definition.hypotheses().stream()
                .filter(hypothesis -> statuses.containsKey(hypothesis.id()))
                .map(hypothesis -> toHypothesisView(hypothesis, statuses))
                .toList();
    }

    private static InvestigationHypothesisView toHypothesisView(
            HypothesisDefinition hypothesis,
            Map<String, HypothesisStatus> statuses
    ) {
        return new InvestigationHypothesisView(
                hypothesis.id(),
                hypothesis.title(),
                statuses.get(hypothesis.id()),
                // HypothesisProgress 不保存可信度，不能根据状态自动推理出分值。
                UNSPECIFIED
        );
    }
}
