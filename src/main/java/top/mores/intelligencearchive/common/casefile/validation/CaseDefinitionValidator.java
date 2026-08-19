package top.mores.intelligencearchive.common.casefile.validation;

import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.EvidenceDefinition;
import top.mores.intelligencearchive.common.casefile.model.HypothesisDefinition;
import top.mores.intelligencearchive.common.casefile.model.InvestigationRequirementSet;
import top.mores.intelligencearchive.common.casefile.model.InvestigationThreadDefinition;
import top.mores.intelligencearchive.common.service.IntelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * 面向内容制作的轻量静态校验器。
 *
 * <p>它只检查 ID、引用和明显规则冲突，不依赖 Minecraft 或 Repository；世界节点是否存在
 * 留给使用 IntelService 的运行时用例检查。</p>
 */
public final class CaseDefinitionValidator {
    public CaseValidationResult validate(CaseDefinition definition) {
        Objects.requireNonNull(definition, "definition 不能为 null");
        List<CaseValidationIssue> issues = new ArrayList<>();

        Set<String> threadIds = collectIds(definition.threads(), InvestigationThreadDefinition::id,
                "Thread", issues);
        Set<String> evidenceIds = collectIds(definition.evidence(), EvidenceDefinition::id,
                "Evidence", issues);
        Set<String> clueIds = collectIds(definition.clues(), ClueDefinition::id,
                "Clue", issues);
        Set<String> hypothesisIds = collectIds(definition.hypotheses(), HypothesisDefinition::id,
                "Hypothesis", issues);

        for (EvidenceDefinition evidence : definition.evidence()) {
            validateOwnerAndThread(definition.id(), evidence.id(), evidence.caseId(), evidence.threadId(), threadIds, issues);
        }
        for (ClueDefinition clue : definition.clues()) {
            validateOwnerAndThread(definition.id(), clue.id(), clue.caseId(), clue.threadId(), threadIds, issues);
            validateRequirements(clue.id(), clue.derivationRules(), evidenceIds, clueIds, issues);
            validateReferences(clue.id(), clue.supportsHypothesisIds(), hypothesisIds, "Hypothesis", issues);
            validateReferences(clue.id(), clue.contradictsHypothesisIds(), hypothesisIds, "Hypothesis", issues);
        }
        for (HypothesisDefinition hypothesis : definition.hypotheses()) {
            validateOwnerAndThread(definition.id(), hypothesis.id(), hypothesis.caseId(), hypothesis.threadId(), threadIds, issues);
            validateRequirements(hypothesis.id(), hypothesis.availabilityRequirements(), evidenceIds, clueIds, issues);
            validateRequirements(hypothesis.id(), hypothesis.supportRequirements(), evidenceIds, clueIds, issues);
            validateRequirements(hypothesis.id(), hypothesis.confirmationRequirements(), evidenceIds, clueIds, issues);
            validateRequirements(hypothesis.id(), hypothesis.refutationRequirements(), evidenceIds, clueIds, issues);
            if (!hypothesis.confirmationRequirements().isEmpty()
                    && equivalentAlternatives(hypothesis.confirmationRequirements(), hypothesis.refutationRequirements())) {
                issues.add(new CaseValidationIssue(
                        CaseValidationCode.EQUIVALENT_FINAL_REQUIREMENTS,
                        hypothesis.id(),
                        "Hypothesis 的确认条件与推翻条件完全等价"
                ));
            }
        }

        detectClueCycles(definition.clues(), clueIds, issues);
        return new CaseValidationResult(issues);
    }

    /**
     * 在静态校验之外，通过世界查询服务检查 Case 内容引用的 IntelNode。
     *
     * <p>依赖的是只读 IntelService，而不是 Repository，因此不会把持久化实现带入领域校验。</p>
     */
    public CaseValidationResult validate(CaseDefinition definition, IntelService intelService) {
        Objects.requireNonNull(intelService, "intelService 不能为 null");
        List<CaseValidationIssue> issues = new ArrayList<>(validate(definition).issues());
        Set<String> nodeIds = new LinkedHashSet<>(definition.relatedIntelNodeIds());
        for (EvidenceDefinition evidence : definition.evidence()) {
            nodeIds.addAll(evidence.relatedIntelNodeIds());
        }
        for (ClueDefinition clue : definition.clues()) {
            nodeIds.addAll(clue.relatedIntelNodeIds());
        }
        for (String nodeId : nodeIds) {
            if (intelService.findNodeById(nodeId).isEmpty()) {
                issues.add(new CaseValidationIssue(
                        CaseValidationCode.INTEL_NODE_NOT_FOUND,
                        nodeId,
                        "引用的世界 IntelNode 不存在: " + nodeId
                ));
            }
        }
        return new CaseValidationResult(issues);
    }

    private static <T> Set<String> collectIds(
            List<T> values,
            Function<T, String> idExtractor,
            String kind,
            List<CaseValidationIssue> issues
    ) {
        Set<String> ids = new LinkedHashSet<>();
        for (T value : values) {
            String id = idExtractor.apply(value);
            if (!ids.add(id)) {
                issues.add(new CaseValidationIssue(
                        CaseValidationCode.DUPLICATE_ID,
                        id,
                        kind + " ID 重复: " + id
                ));
            }
        }
        return ids;
    }

    private static void validateOwnerAndThread(
            String expectedCaseId,
            String subjectId,
            String actualCaseId,
            String threadId,
            Set<String> threadIds,
            List<CaseValidationIssue> issues
    ) {
        if (!expectedCaseId.equals(actualCaseId)) {
            issues.add(new CaseValidationIssue(
                    CaseValidationCode.CASE_ID_MISMATCH,
                    subjectId,
                    "定义的 caseId 不属于当前 Case"
            ));
        }
        if (!threadIds.contains(threadId)) {
            issues.add(new CaseValidationIssue(
                    CaseValidationCode.THREAD_NOT_FOUND,
                    subjectId,
                    "引用的 Thread 不存在: " + threadId
            ));
        }
    }

    private static void validateRequirements(
            String subjectId,
            List<InvestigationRequirementSet> rules,
            Set<String> evidenceIds,
            Set<String> clueIds,
            List<CaseValidationIssue> issues
    ) {
        for (InvestigationRequirementSet rule : rules) {
            validateReferences(subjectId, rule.requiredEvidenceIds(), evidenceIds, "Evidence", issues);
            validateReferences(subjectId, rule.requiredClueIds(), clueIds, "Clue", issues);
        }
    }

    private static void validateReferences(
            String subjectId,
            Set<String> references,
            Set<String> validIds,
            String kind,
            List<CaseValidationIssue> issues
    ) {
        for (String reference : references) {
            if (!validIds.contains(reference)) {
                issues.add(new CaseValidationIssue(
                        CaseValidationCode.INVALID_REFERENCE,
                        subjectId,
                        "引用的 " + kind + " 不存在: " + reference
                ));
            }
        }
    }

    private static boolean equivalentAlternatives(
            List<InvestigationRequirementSet> first,
            List<InvestigationRequirementSet> second
    ) {
        return new HashSet<>(first).equals(new HashSet<>(second));
    }

    private static void detectClueCycles(
            List<ClueDefinition> clues,
            Set<String> validClueIds,
            List<CaseValidationIssue> issues
    ) {
        Map<String, Set<String>> dependencies = new LinkedHashMap<>();
        for (ClueDefinition clue : clues) {
            Set<String> referencedClues = new LinkedHashSet<>();
            for (InvestigationRequirementSet rule : clue.derivationRules()) {
                for (String clueId : rule.requiredClueIds()) {
                    if (validClueIds.contains(clueId)) {
                        referencedClues.add(clueId);
                    }
                }
            }
            dependencies.putIfAbsent(clue.id(), referencedClues);
        }

        Map<String, VisitState> states = new HashMap<>();
        Set<String> reported = new HashSet<>();
        for (String clueId : dependencies.keySet()) {
            visit(clueId, dependencies, states, new ArrayList<>(), reported, issues);
        }
    }

    private static void visit(
            String clueId,
            Map<String, Set<String>> dependencies,
            Map<String, VisitState> states,
            List<String> path,
            Set<String> reported,
            List<CaseValidationIssue> issues
    ) {
        VisitState state = states.get(clueId);
        if (state == VisitState.VISITING) {
            if (reported.add(clueId)) {
                issues.add(new CaseValidationIssue(
                        CaseValidationCode.CLUE_DEPENDENCY_CYCLE,
                        clueId,
                        "Clue 派生依赖存在循环: " + String.join(" -> ", path) + " -> " + clueId
                ));
            }
            return;
        }
        if (state == VisitState.VISITED) {
            return;
        }

        states.put(clueId, VisitState.VISITING);
        path.add(clueId);
        for (String dependency : dependencies.getOrDefault(clueId, Set.of())) {
            visit(dependency, dependencies, states, path, reported, issues);
        }
        path.remove(path.size() - 1);
        states.put(clueId, VisitState.VISITED);
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}
