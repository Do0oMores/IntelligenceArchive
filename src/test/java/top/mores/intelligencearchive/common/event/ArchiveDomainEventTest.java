package top.mores.intelligencearchive.common.event;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.application.result.DiscoverIntelResult;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContextBuilder;
import top.mores.intelligencearchive.server.event.SimpleDomainEventPublisher;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.lang.reflect.RecordComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 2-E 纯 Java 领域事件边界测试。 */
class ArchiveDomainEventTest {
    private static final Instant EVENT_TIME = Instant.parse("2026-08-16T04:00:00Z");
    private static final String DOCUMENT_ID = SimpleIntelService.TEST_DOCUMENT_ID;

    @Test
    void successfulDiscoveryPublishesPastTenseFact() {
        EventFixture fixture = createFixture();
        UUID playerId = UUID.randomUUID();

        fixture.context().getDiscoverIntelUseCase().execute(playerId, DOCUMENT_ID);

        IntelDiscoveredEvent event = assertInstanceOf(
                IntelDiscoveredEvent.class,
                fixture.events().get(0)
        );
        assertEquals(playerId, event.playerId());
        assertEquals(DOCUMENT_ID, event.intelId());
        assertEquals(EVENT_TIME, event.timestamp());
    }

    @Test
    void successfulReadPublishesArchiveReadEvent() {
        EventFixture fixture = createFixture();
        UUID playerId = UUID.randomUUID();
        fixture.context().getDiscoverIntelUseCase().execute(playerId, DOCUMENT_ID);
        fixture.events().clear();

        fixture.context().getReadArchiveUseCase().execute(playerId, DOCUMENT_ID);

        ArchiveReadEvent event = assertInstanceOf(ArchiveReadEvent.class, fixture.events().get(0));
        assertEquals(playerId, event.playerId());
        assertEquals(DOCUMENT_ID, event.documentId());
        assertEquals(EVENT_TIME, event.timestamp());
    }

    @Test
    void successfulVerificationPublishesIntelVerifiedEvent() {
        EventFixture fixture = createFixture();
        UUID playerId = UUID.randomUUID();
        fixture.context().getDiscoverIntelUseCase().execute(playerId, DOCUMENT_ID);
        fixture.context().getReadArchiveUseCase().execute(playerId, DOCUMENT_ID);
        fixture.events().clear();

        fixture.context().getVerifyIntelUseCase().execute(playerId, DOCUMENT_ID);

        IntelVerifiedEvent event = assertInstanceOf(IntelVerifiedEvent.class, fixture.events().get(0));
        assertEquals(playerId, event.playerId());
        assertEquals(DOCUMENT_ID, event.intelId());
    }

    @Test
    void listenerFailureDoesNotBlockOtherListeners() {
        SimpleDomainEventPublisher publisher = new SimpleDomainEventPublisher();
        List<ArchiveEvent> received = new ArrayList<>();
        publisher.registerListener(event -> {
            throw new IllegalStateException("listener failure");
        });
        publisher.registerListener(received::add);
        IntelDiscoveredEvent event = new IntelDiscoveredEvent(
                UUID.randomUUID(),
                DOCUMENT_ID,
                EVENT_TIME
        );

        assertDoesNotThrow(() -> publisher.publish(event));
        assertEquals(List.of(event), received);
    }

    @Test
    void publisherFailureDoesNotChangeSuccessfulCoreResult() {
        DomainEventPublisher failingPublisher = new DomainEventPublisher() {
            @Override
            public void publish(ArchiveEvent event) {
                throw new IllegalStateException("publisher unavailable");
            }

            @Override
            public void registerListener(DomainEventListener listener) {
            }

            @Override
            public void unregisterListener(DomainEventListener listener) {
            }
        };
        SimpleInvestigationService investigationService = new SimpleInvestigationService(fixedClock());
        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder(
                new SimpleIntelService(),
                investigationService,
                failingPublisher,
                fixedClock()
        ).build();
        UUID playerId = UUID.randomUUID();

        DiscoverIntelResult result = context.getDiscoverIntelUseCase().execute(playerId, DOCUMENT_ID);

        assertTrue(result.success());
        assertEquals(
                IntelDiscoveryStatus.DISCOVERED,
                investigationService.getPlayerState(playerId).statusOf(DOCUMENT_ID)
        );
    }

    @Test
    void eventRecordsContainOnlyPureJavaIdentifiersAndTimestamp() {
        List<Class<? extends ArchiveEvent>> eventTypes = List.of(
                IntelDiscoveredEvent.class,
                ArchiveReadEvent.class,
                IntelVerifiedEvent.class
        );

        for (Class<? extends ArchiveEvent> eventType : eventTypes) {
            assertTrue(eventType.isRecord());
            for (RecordComponent component : eventType.getRecordComponents()) {
                String typeName = component.getType().getName();
                assertFalse(typeName.startsWith("net.minecraft"));
                assertFalse(typeName.startsWith("net.minecraftforge"));
            }
        }
    }

    private static EventFixture createFixture() {
        SimpleDomainEventPublisher publisher = new SimpleDomainEventPublisher();
        List<ArchiveEvent> events = new ArrayList<>();
        publisher.registerListener(events::add);
        ArchiveRuntimeContext context = new ArchiveRuntimeContextBuilder(
                new SimpleIntelService(),
                new SimpleInvestigationService(fixedClock()),
                publisher,
                fixedClock()
        ).build();
        return new EventFixture(context, events);
    }

    private static Clock fixedClock() {
        return Clock.fixed(EVENT_TIME, ZoneOffset.UTC);
    }

    private record EventFixture(ArchiveRuntimeContext context, List<ArchiveEvent> events) {
    }
}
