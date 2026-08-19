package top.mores.intelligencearchive.common.casefile.service;

import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.EvidenceDefinition;
import top.mores.intelligencearchive.common.casefile.model.HypothesisDefinition;

import java.util.Optional;

/** 静态案件定义的只读查询边界；本阶段不规定资源或数据库格式。 */
public interface CaseDefinitionService {
    Optional<CaseDefinition> findCase(String caseId);

    Optional<EvidenceDefinition> findEvidence(String caseId, String evidenceId);

    Optional<ClueDefinition> findClue(String caseId, String clueId);

    Optional<HypothesisDefinition> findHypothesis(String caseId, String hypothesisId);
}
