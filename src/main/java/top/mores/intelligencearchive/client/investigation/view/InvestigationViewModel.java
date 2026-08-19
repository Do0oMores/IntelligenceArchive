package top.mores.intelligencearchive.client.investigation.view;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Investigation Screen 唯一消费的只读数据模型。
 *
 * <p>此处再次复制集合，避免 UI 组件持有网络 DTO 的集合引用。</p>
 */
public record InvestigationViewModel(
        CaseSummaryViewModel caseSummary,
        List<IntelCardViewModel> intelCards,
        List<InvestigationRelationViewModel> relations,
        List<EvidenceCardViewModel> evidenceCards,
        List<ClueCardViewModel> clueCards,
        List<HypothesisCardViewModel> hypothesisCards,
        Instant timestamp
) {
    public InvestigationViewModel {
        caseSummary = Objects.requireNonNull(caseSummary, "caseSummary 不能为 null");
        intelCards = List.copyOf(Objects.requireNonNull(intelCards, "intelCards 不能为 null"));
        relations = List.copyOf(Objects.requireNonNull(relations, "relations 不能为 null"));
        evidenceCards = List.copyOf(Objects.requireNonNull(evidenceCards, "evidenceCards 不能为 null"));
        clueCards = List.copyOf(Objects.requireNonNull(clueCards, "clueCards 不能为 null"));
        hypothesisCards = List.copyOf(Objects.requireNonNull(hypothesisCards, "hypothesisCards 不能为 null"));
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }

    public boolean isEmpty() {
        return intelCards.isEmpty() && relations.isEmpty() && evidenceCards.isEmpty()
                && clueCards.isEmpty() && hypothesisCards.isEmpty();
    }

    public String caseId() {
        return caseSummary.caseId();
    }
}
