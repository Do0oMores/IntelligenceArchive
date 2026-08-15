package top.mores.intelligencearchive.server;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.client.state.ArchiveClientArchiveState;
import top.mores.intelligencearchive.common.dto.ArchiveDocumentDTO;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.server.service.SimpleIntelService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 2-B 档案查询业务链的纯逻辑与协议回归测试。 */
class ArchiveDocumentQueryPipelineTest {
    @Test
    void existingDocumentReturnsMappedDto() {
        ResponseArchiveDocumentPacket response = ArchiveDocumentServerHandler.createResponse(
                SimpleIntelService.TEST_DOCUMENT_ID,
                new SimpleIntelService()
        );

        assertTrue(response.success());
        assertEquals(SimpleIntelService.TEST_DOCUMENT_ID, response.documentId());
        assertEquals("测试档案", response.document().title());
        assertEquals("DOCUMENT", response.document().type());
        assertEquals("PUBLIC", response.document().securityLevel());
    }

    @Test
    void missingDocumentReturnsFailure() {
        ResponseArchiveDocumentPacket response = ArchiveDocumentServerHandler.createResponse(
                "document.invalid.xxx",
                new SimpleIntelService()
        );

        assertFalse(response.success());
        assertNull(response.document());
        assertEquals("Document not found.", response.errorMessage());
    }

    @Test
    void malformedDocumentIdIsRejectedBeforeQuery() {
        ResponseArchiveDocumentPacket response = ArchiveDocumentServerHandler.createResponse(
                "../INVALID DOCUMENT",
                new SimpleIntelService()
        );

        assertFalse(response.success());
        assertEquals("Invalid document id.", response.errorMessage());
    }

    @Test
    void clientArchiveStateAcceptsMatchingResponseData() {
        ResponseArchiveDocumentPacket response = ArchiveDocumentServerHandler.createResponse(
                SimpleIntelService.TEST_DOCUMENT_ID,
                new SimpleIntelService()
        );
        ArchiveClientArchiveState state = new ArchiveClientArchiveState();

        assertTrue(state.beginRequest(SimpleIntelService.TEST_DOCUMENT_ID, 100L));
        state.acceptDocument(response.documentId(), response.document());

        ArchiveClientArchiveState.View view = state.view();
        assertFalse(view.requesting());
        assertEquals("测试档案", view.currentDocument().title());
        assertTrue(view.lastError().isEmpty());
    }

    @Test
    void requestPacketRoundTripsDocumentId() {
        RequestArchiveDocumentPacket source = new RequestArchiveDocumentPacket(
                SimpleIntelService.TEST_DOCUMENT_ID
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RequestArchiveDocumentPacket.encode(source, buffer);
            assertEquals(source, RequestArchiveDocumentPacket.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void successfulResponsePacketRoundTripsDto() {
        ArchiveDocumentDTO document = ArchiveDocumentServerHandler.createResponse(
                SimpleIntelService.TEST_DOCUMENT_ID,
                new SimpleIntelService()
        ).document();
        ResponseArchiveDocumentPacket source = ResponseArchiveDocumentPacket.success(document);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ResponseArchiveDocumentPacket.encode(source, buffer);
            assertEquals(source, ResponseArchiveDocumentPacket.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void failedResponsePacketRoundTripsError() {
        ResponseArchiveDocumentPacket source = ResponseArchiveDocumentPacket.failure(
                "document.invalid.xxx",
                "Document not found."
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ResponseArchiveDocumentPacket.encode(source, buffer);
            assertEquals(source, ResponseArchiveDocumentPacket.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
