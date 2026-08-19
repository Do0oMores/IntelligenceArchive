package top.mores.intelligencearchive.application.discovery;

import java.util.UUID;

/**
 * Discovery 到 Evidence 系统的外围端口。
 *
 * <p>端口只接受全局 evidenceId，不暴露 caseId；Case 定位和 Clue 派生由适配器负责。</p>
 */
@FunctionalInterface
public interface EvidenceDiscoveryGateway {
    EvidenceDiscoveryOutcome discover(UUID playerId, String evidenceId);

    static EvidenceDiscoveryGateway unavailable() {
        return (playerId, evidenceId) -> EvidenceDiscoveryOutcome.notFound();
    }
}
