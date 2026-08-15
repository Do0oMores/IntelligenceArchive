package top.mores.intelligencearchive.application;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.application.result.DiscoverIntelResult;
import top.mores.intelligencearchive.application.result.LinkIntelResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.result.ReadArchiveResult;
import top.mores.intelligencearchive.application.result.VerifyIntelResult;
import top.mores.intelligencearchive.application.usecase.DiscoverIntelUseCase;
import top.mores.intelligencearchive.application.usecase.LinkIntelUseCase;
import top.mores.intelligencearchive.application.usecase.ReadArchiveUseCase;
import top.mores.intelligencearchive.application.usecase.VerifyIntelUseCase;
import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelNode;
import top.mores.intelligencearchive.common.model.IntelNodeType;
import top.mores.intelligencearchive.common.model.IntelRelationType;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 2-D 应用层业务编排测试。 */
class ArchiveApplicationLayerTest {
    private static final String DOCUMENT_ID = SimpleIntelService.TEST_DOCUMENT_ID;

    @Test
    void discoversExistingWorldIntel() {
        SimpleInvestigationService investigation = investigationService();
        DiscoverIntelUseCase useCase = new DiscoverIntelUseCase(new SimpleIntelService(), investigation);
        UUID playerId = UUID.randomUUID();

        DiscoverIntelResult result = useCase.execute(playerId, DOCUMENT_ID);

        assertTrue(result.success());
        assertEquals(OperationStatus.SUCCESS, result.status());
        assertEquals(IntelDiscoveryStatus.UNKNOWN, result.oldStatus());
        assertEquals(IntelDiscoveryStatus.DISCOVERED, result.newStatus());
        assertTrue(investigation.hasDiscovered(playerId, DOCUMENT_ID));
    }

    @Test
    void repeatedDiscoveryReturnsAlreadyDiscovered() {
        SimpleInvestigationService investigation = investigationService();
        DiscoverIntelUseCase useCase = new DiscoverIntelUseCase(new SimpleIntelService(), investigation);
        UUID playerId = UUID.randomUUID();
        useCase.execute(playerId, DOCUMENT_ID);

        DiscoverIntelResult result = useCase.execute(playerId, DOCUMENT_ID);

        assertFalse(result.success());
        assertEquals(OperationStatus.ALREADY_DISCOVERED, result.status());
        assertEquals(IntelDiscoveryStatus.DISCOVERED, result.oldStatus());
        assertEquals(1, investigation.getPlayerState(playerId).discoveredIntels().size());
    }

    @Test
    void missingWorldIntelCannotBeDiscovered() {
        SimpleInvestigationService investigation = investigationService();
        DiscoverIntelUseCase useCase = new DiscoverIntelUseCase(new SimpleIntelService(), investigation);
        UUID playerId = UUID.randomUUID();

        DiscoverIntelResult result = useCase.execute(playerId, "document.invalid.xxx");

        assertEquals(OperationStatus.INTEL_NOT_FOUND, result.status());
        assertTrue(investigation.getPlayerState(playerId).discoveredIntels().isEmpty());
    }

    @Test
    void readsDiscoveredArchive() {
        SimpleIntelService intelService = new SimpleIntelService();
        SimpleInvestigationService investigation = investigationService();
        UUID playerId = UUID.randomUUID();
        new DiscoverIntelUseCase(intelService, investigation).execute(playerId, DOCUMENT_ID);

        ReadArchiveResult result = new ReadArchiveUseCase(intelService, investigation)
                .execute(playerId, DOCUMENT_ID);

        assertTrue(result.success());
        assertEquals(IntelDiscoveryStatus.DISCOVERED, result.oldStatus());
        assertEquals(IntelDiscoveryStatus.READ, result.newStatus());
        assertEquals(IntelDiscoveryStatus.READ,
                investigation.getPlayerState(playerId).statusOf(DOCUMENT_ID));
    }

    @Test
    void unknownArchiveCannotJumpDirectlyToRead() {
        SimpleInvestigationService investigation = investigationService();
        UUID playerId = UUID.randomUUID();

        ReadArchiveResult result = new ReadArchiveUseCase(new SimpleIntelService(), investigation)
                .execute(playerId, DOCUMENT_ID);

        assertFalse(result.success());
        assertEquals(OperationStatus.INVALID_STATE, result.status());
        assertEquals(IntelDiscoveryStatus.UNKNOWN, result.oldStatus());
        assertFalse(investigation.hasDiscovered(playerId, DOCUMENT_ID));
    }

    @Test
    void verifiesReadIntel() {
        SimpleIntelService intelService = new SimpleIntelService();
        SimpleInvestigationService investigation = investigationService();
        UUID playerId = UUID.randomUUID();
        new DiscoverIntelUseCase(intelService, investigation).execute(playerId, DOCUMENT_ID);
        new ReadArchiveUseCase(intelService, investigation).execute(playerId, DOCUMENT_ID);

        VerifyIntelResult result = new VerifyIntelUseCase(intelService, investigation)
                .execute(playerId, DOCUMENT_ID);

        assertTrue(result.success());
        assertEquals(IntelDiscoveryStatus.READ, result.oldStatus());
        assertEquals(IntelDiscoveryStatus.VERIFIED, result.newStatus());
    }

    @Test
    void applicationOperationsKeepPlayersIsolated() {
        SimpleInvestigationService investigation = investigationService();
        DiscoverIntelUseCase useCase = new DiscoverIntelUseCase(new SimpleIntelService(), investigation);
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        useCase.execute(playerA, DOCUMENT_ID);

        assertTrue(investigation.hasDiscovered(playerA, DOCUMENT_ID));
        assertFalse(investigation.hasDiscovered(playerB, DOCUMENT_ID));
    }

    @Test
    void linkUseCaseValidatesNodesWithoutPersistingRelation() {
        IntelNode source = new IntelNode("node.location.lab", "废弃实验室", IntelNodeType.LOCATION, "");
        IntelNode target = new IntelNode("node.person.researcher", "研究员", IntelNodeType.PERSON, "");
        TestNodeIntelService intelService = new TestNodeIntelService(Map.of(
                source.id(), source,
                target.id(), target
        ));

        LinkIntelResult result = new LinkIntelUseCase(intelService).execute(
                UUID.randomUUID(),
                source.id(),
                target.id(),
                IntelRelationType.RELATED_TO
        );

        assertTrue(result.success());
        assertEquals(OperationStatus.SUCCESS, result.status());
        assertTrue(intelService.findRelations(source.id()).isEmpty());
    }

    @Test
    void invalidDiscoveryInputReturnsExplicitResult() {
        DiscoverIntelResult result = new DiscoverIntelUseCase(
                new SimpleIntelService(),
                investigationService()
        ).execute(UUID.randomUUID(), " ");

        assertFalse(result.success());
        assertEquals(OperationStatus.INVALID_INPUT, result.status());
    }

    private static SimpleInvestigationService investigationService() {
        return new SimpleInvestigationService(Clock.fixed(
                Instant.parse("2026-08-16T02:00:00Z"),
                ZoneOffset.UTC
        ));
    }

    private static final class TestNodeIntelService implements IntelService {
        private final Map<String, IntelNode> nodes;

        private TestNodeIntelService(Map<String, IntelNode> nodes) {
            this.nodes = Map.copyOf(nodes);
        }

        @Override
        public Optional<ArchiveDocument> findDocumentById(String documentId) {
            return Optional.empty();
        }

        @Override
        public Optional<IntelNode> findNodeById(String nodeId) {
            return Optional.ofNullable(nodes.get(nodeId));
        }

        @Override
        public List<IntelEdge> findRelations(String nodeId) {
            return List.of();
        }

        @Override
        public boolean existsDocument(String documentId) {
            return false;
        }
    }
}
