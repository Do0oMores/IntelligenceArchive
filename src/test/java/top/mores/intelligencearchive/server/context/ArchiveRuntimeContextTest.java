package top.mores.intelligencearchive.server.context;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.application.result.DiscoverIntelResult;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;
import top.mores.intelligencearchive.server.event.SimpleDomainEventPublisher;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 2-E Runtime Context 装配与生命周期测试。 */
class ArchiveRuntimeContextTest {
    @Test
    void defaultContextBuildsAllRuntimeComponents() {
        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder().build();

        assertNotNull(context.getIntelService());
        assertNotNull(context.getInvestigationService());
        assertNotNull(context.getArchiveContentService());
        assertNotNull(context.getDomainEventPublisher());
        assertNotNull(context.getDiscoverIntelUseCase());
        assertNotNull(context.getReadArchiveUseCase());
        assertNotNull(context.getVerifyIntelUseCase());
        assertNotNull(context.getLinkIntelUseCase());
        assertNotNull(context.getResolveArchiveContentUseCase());
    }

    @Test
    void contextKeepsExactlyTheInjectedServiceInstances() {
        IntelService intelService = new SimpleIntelService();
        InvestigationService investigationService = new SimpleInvestigationService();

        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder(
                intelService,
                investigationService,
                new SimpleDomainEventPublisher(),
                fixedClock()
        ).build();

        assertSame(intelService, context.getIntelService());
        assertSame(investigationService, context.getInvestigationService());
    }

    @Test
    void useCaseObtainedFromContextUsesTheSharedPlayerStateService() {
        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder(
                new SimpleIntelService(),
                new SimpleInvestigationService(),
                new SimpleDomainEventPublisher(),
                fixedClock()
        ).build();
        UUID playerId = UUID.randomUUID();

        DiscoverIntelResult result = context.getDiscoverIntelUseCase().execute(
                playerId,
                SimpleIntelService.TEST_DOCUMENT_ID
        );

        assertTrue(result.success());
        assertTrue(context.getInvestigationService().hasDiscovered(
                playerId,
                SimpleIntelService.TEST_DOCUMENT_ID
        ));
    }

    @Test
    void runtimeHolderReleasesContextAtSessionEnd() {
        ArchiveRuntimeContexts.clear();
        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder().build();
        try {
            ArchiveRuntimeContexts.install(context);
            assertSame(context, ArchiveRuntimeContexts.current().orElseThrow());
        } finally {
            ArchiveRuntimeContexts.clear();
        }

        assertFalse(ArchiveRuntimeContexts.current().isPresent());
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-08-16T03:00:00Z"), ZoneOffset.UTC);
    }
}
