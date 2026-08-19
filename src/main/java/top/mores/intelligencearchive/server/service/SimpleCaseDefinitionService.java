package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.EvidenceDefinition;
import top.mores.intelligencearchive.common.casefile.model.HypothesisDefinition;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.validation.CaseDefinitionValidator;
import top.mores.intelligencearchive.common.casefile.validation.CaseValidationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Phase 4-B 的内存案件定义服务。
 *
 * <p>构造时拒绝静态校验失败的内容；资源包或 YAML 加载属于后续阶段。</p>
 */
public final class SimpleCaseDefinitionService implements CaseDefinitionService {
    private final Map<String, CaseDefinition> definitions;

    public SimpleCaseDefinitionService(List<CaseDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions 不能为 null");
        CaseDefinitionValidator validator = new CaseDefinitionValidator();
        LinkedHashMap<String, CaseDefinition> copy = new LinkedHashMap<>();
        for (CaseDefinition definition : definitions) {
            CaseDefinition validDefinition = Objects.requireNonNull(definition, "definitions 不能包含 null");
            CaseValidationResult validation = validator.validate(validDefinition);
            if (!validation.valid()) {
                throw new IllegalArgumentException(
                        "CaseDefinition 校验失败 " + validDefinition.id() + ": " + validation.issues()
                );
            }
            if (copy.putIfAbsent(validDefinition.id(), validDefinition) != null) {
                throw new IllegalArgumentException("Case ID 重复: " + validDefinition.id());
            }
        }
        this.definitions = Map.copyOf(copy);
    }

    @Override
    public Optional<CaseDefinition> findCase(String caseId) {
        return Optional.ofNullable(definitions.get(caseId));
    }

    @Override
    public Optional<EvidenceDefinition> findEvidence(String caseId, String evidenceId) {
        return findCase(caseId).flatMap(definition -> definition.findEvidence(evidenceId));
    }

    @Override
    public Optional<ClueDefinition> findClue(String caseId, String clueId) {
        return findCase(caseId).flatMap(definition -> definition.findClue(clueId));
    }

    @Override
    public Optional<HypothesisDefinition> findHypothesis(String caseId, String hypothesisId) {
        return findCase(caseId).flatMap(definition -> definition.findHypothesis(hypothesisId));
    }
}
