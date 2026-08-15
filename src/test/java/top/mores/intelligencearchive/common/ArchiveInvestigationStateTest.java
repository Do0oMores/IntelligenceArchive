package top.mores.intelligencearchive.common;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.InvestigationLevel;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 2-C 玩家调查状态的纯 Java 行为测试。 */
class ArchiveInvestigationStateTest {
    private static final Instant TEST_TIME = Instant.parse("2026-08-16T01:00:00Z");
    private static final String TEST_INTEL_ID = "document.case.redwell_report";

    @Test
    void newPlayerStartsWithNoIntel() {
        SimpleInvestigationService service = createService();
        UUID playerId = UUID.randomUUID();

        PlayerInvestigationState state = service.getPlayerState(playerId);

        assertTrue(state.discoveredIntels().isEmpty());
        assertEquals(InvestigationLevel.NOVICE, state.investigationLevel());
        assertEquals(IntelDiscoveryStatus.UNKNOWN, state.statusOf(TEST_INTEL_ID));
        assertFalse(service.hasDiscovered(playerId, TEST_INTEL_ID));
    }

    @Test
    void discoverIntelAddsRecord() {
        SimpleInvestigationService service = createService();
        UUID playerId = UUID.randomUUID();

        IntelDiscoveryRecord record = service.discoverIntel(playerId, TEST_INTEL_ID);

        assertEquals(IntelDiscoveryStatus.DISCOVERED, record.status());
        assertEquals(TEST_TIME, record.discoveredTime());
        assertTrue(service.hasDiscovered(playerId, TEST_INTEL_ID));
        assertEquals(1, service.getPlayerState(playerId).discoveredIntels().size());
    }

    @Test
    void repeatedDiscoveryDoesNotCreateDuplicate() {
        SimpleInvestigationService service = createService();
        UUID playerId = UUID.randomUUID();

        IntelDiscoveryRecord first = service.discoverIntel(playerId, TEST_INTEL_ID);
        IntelDiscoveryRecord second = service.discoverIntel(playerId, TEST_INTEL_ID);

        assertEquals(first, second);
        assertEquals(1, service.getPlayerState(playerId).discoveredIntels().size());
    }

    @Test
    void statusAdvancesFromDiscoveredToRead() {
        SimpleInvestigationService service = createService();
        UUID playerId = UUID.randomUUID();
        service.discoverIntel(playerId, TEST_INTEL_ID);

        IntelDiscoveryRecord updated = service.updateStatus(
                playerId,
                TEST_INTEL_ID,
                IntelDiscoveryStatus.READ
        ).orElseThrow();

        assertEquals(IntelDiscoveryStatus.READ, updated.status());
        assertEquals(TEST_TIME, updated.discoveredTime());
        assertEquals(IntelDiscoveryStatus.READ,
                service.getPlayerState(playerId).statusOf(TEST_INTEL_ID));
    }

    @Test
    void differentPlayersHaveIsolatedStates() {
        SimpleInvestigationService service = createService();
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        service.discoverIntel(playerA, TEST_INTEL_ID);

        assertTrue(service.hasDiscovered(playerA, TEST_INTEL_ID));
        assertFalse(service.hasDiscovered(playerB, TEST_INTEL_ID));
        assertTrue(service.getPlayerState(playerB).discoveredIntels().isEmpty());
    }

    @Test
    void missingIntelCanBeQueriedAndIsNotImplicitlyGrantedByUpdate() {
        SimpleInvestigationService service = createService();
        UUID playerId = UUID.randomUUID();

        assertEquals(IntelDiscoveryStatus.UNKNOWN,
                service.getPlayerState(playerId).statusOf("document.invalid.xxx"));
        assertTrue(service.updateStatus(
                playerId,
                "document.invalid.xxx",
                IntelDiscoveryStatus.READ
        ).isEmpty());
        assertFalse(service.hasDiscovered(playerId, "document.invalid.xxx"));
    }

    @Test
    void stateDefensivelyCopiesDiscoveryRecords() {
        UUID playerId = UUID.randomUUID();
        List<IntelDiscoveryRecord> mutableRecords = new ArrayList<>();
        mutableRecords.add(new IntelDiscoveryRecord(
                TEST_INTEL_ID,
                IntelDiscoveryStatus.DISCOVERED,
                TEST_TIME,
                TEST_TIME
        ));

        PlayerInvestigationState state = new PlayerInvestigationState(
                playerId,
                mutableRecords,
                InvestigationLevel.NOVICE
        );
        mutableRecords.clear();

        assertEquals(1, state.discoveredIntels().size());
        assertThrows(UnsupportedOperationException.class,
                () -> state.discoveredIntels().add(state.discoveredIntels().get(0)));
    }

    private static SimpleInvestigationService createService() {
        return new SimpleInvestigationService(Clock.fixed(TEST_TIME, ZoneOffset.UTC));
    }
}
