package top.mores.intelligencearchive.common;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.ArchiveDocumentType;
import top.mores.intelligencearchive.common.model.ArchiveMetadata;
import top.mores.intelligencearchive.common.model.ArchiveSecurityLevel;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelRelationType;
import top.mores.intelligencearchive.server.service.SimpleIntelService;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Archive Domain Core 的最小纯 Java 回归测试。 */
class ArchiveDomainCoreTest {
    @Test
    void createsImmutableArchiveDocument() {
        ArchiveDocument document = new ArchiveDocument(
                "case.red-well.001",
                "红井事件调查报告",
                ArchiveDocumentType.REPORT,
                "红井事件的初步调查摘要。",
                "archive/red-well/report.md",
                new ArchiveMetadata(
                        Instant.parse("2026-01-01T00:00:00Z"),
                        "研究部门",
                        ArchiveSecurityLevel.RESTRICTED
                ),
                Set.of("case.red-well.transcript")
        );

        assertEquals("case.red-well.001", document.id());
        assertEquals(ArchiveDocumentType.REPORT, document.type());
        assertThrows(UnsupportedOperationException.class,
                () -> document.links().add("case.illegal-mutation"));
    }

    @Test
    void simpleServiceFindsPreloadedTestDocument() {
        SimpleIntelService service = new SimpleIntelService();

        ArchiveDocument document = service.findDocumentById(SimpleIntelService.TEST_DOCUMENT_ID)
                .orElseThrow();

        assertEquals("测试档案", document.title());
        assertTrue(service.existsDocument(SimpleIntelService.TEST_DOCUMENT_ID));
    }

    @Test
    void missingDocumentReturnsEmpty() {
        SimpleIntelService service = new SimpleIntelService();

        assertTrue(service.findDocumentById("case.missing.404").isEmpty());
        assertFalse(service.existsDocument("case.missing.404"));
    }

    @Test
    void edgeExpressesDirectedRelationByNodeIds() {
        IntelEdge edge = new IntelEdge(
                "node.red-well-event",
                "node.s-04-experiment",
                IntelRelationType.CAUSED_BY
        );

        assertEquals("node.red-well-event", edge.sourceNodeId());
        assertEquals("node.s-04-experiment", edge.targetNodeId());
        assertEquals(IntelRelationType.CAUSED_BY, edge.relationType());
    }
}
