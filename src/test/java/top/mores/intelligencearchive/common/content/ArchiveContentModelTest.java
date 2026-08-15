package top.mores.intelligencearchive.common.content;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 3-A Archive Content Model 的纯 Java 行为测试。 */
class ArchiveContentModelTest {
    @Test
    void createsTextNode() {
        TextContentNode node = new TextContentNode("事故发生于03:17。");

        assertEquals(ContentNodeType.TEXT, node.type());
        assertEquals("事故发生于03:17。", node.text());
    }

    @Test
    void createsImageReferenceNode() {
        ImageContentNode node = new ImageContentNode("archive/image/redwell_001.png");

        assertEquals(ContentNodeType.IMAGE, node.type());
        assertEquals("archive/image/redwell_001.png", node.imageReference());
    }

    @Test
    void createsAudioReferenceNode() {
        AudioContentNode node = new AudioContentNode("archive/audio/radio_001.ogg");

        assertEquals(ContentNodeType.AUDIO, node.type());
        assertEquals("archive/audio/radio_001.ogg", node.audioReference());
    }

    @Test
    void createsRedactedConditionReferenceNode() {
        RedactedContentNode node = new RedactedContentNode("██████", "condition.case.redwell.identity");

        assertEquals(ContentNodeType.REDACTED, node.type());
        assertEquals("██████", node.placeholder());
        assertEquals("condition.case.redwell.identity", node.conditionReference());
    }

    @Test
    void createsIntelLinkByIdentifier() {
        IntelLinkContentNode node = new IntelLinkContentNode("node.location.underground_lab");

        assertEquals(ContentNodeType.INTEL_LINK, node.type());
        assertEquals("node.location.underground_lab", node.targetIntelId());
    }

    @Test
    void archiveContentKeepsOrderedPolymorphicNodes() {
        ArchiveContent content = new ArchiveContent(
                "content.case.redwell.v1",
                "document.case.redwell_report",
                "v1",
                List.of(
                        new TextContentNode("调查摘要"),
                        new ImageContentNode("archive/image/redwell_001.png"),
                        new IntelLinkContentNode("node.location.underground_lab")
                )
        );

        assertEquals("document.case.redwell_report", content.documentId());
        assertEquals("v1", content.version());
        assertEquals(3, content.nodes().size());
        assertInstanceOf(TextContentNode.class, content.nodes().get(0));
        assertInstanceOf(ImageContentNode.class, content.nodes().get(1));
    }

    @Test
    void archiveContentDefensivelyCopiesAndProtectsNodeList() {
        List<ContentNode> mutableNodes = new ArrayList<>();
        mutableNodes.add(new TextContentNode("初始内容"));
        ArchiveContent content = new ArchiveContent(
                "content.case.test.v1",
                "document.case.test",
                mutableNodes
        );

        mutableNodes.clear();

        assertEquals(1, content.nodes().size());
        assertThrows(UnsupportedOperationException.class,
                () -> content.nodes().add(new TextContentNode("非法修改")));
    }

    @Test
    void contentNodesContainNoMinecraftOrForgeTypes() {
        List<Class<? extends ContentNode>> nodeTypes = List.of(
                TextContentNode.class,
                ImageContentNode.class,
                AudioContentNode.class,
                RedactedContentNode.class,
                IntelLinkContentNode.class
        );

        for (Class<? extends ContentNode> nodeType : nodeTypes) {
            assertTrue(nodeType.isRecord());
            for (RecordComponent component : nodeType.getRecordComponents()) {
                String typeName = component.getType().getName();
                assertFalse(typeName.startsWith("net.minecraft"));
                assertFalse(typeName.startsWith("net.minecraftforge"));
            }
        }
    }

    @Test
    void rejectsBlankContentReferencesAndIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new ImageContentNode(" "));
        assertThrows(IllegalArgumentException.class, () -> new AudioContentNode(""));
        assertThrows(IllegalArgumentException.class, () -> new IntelLinkContentNode(" "));
        assertThrows(IllegalArgumentException.class,
                () -> new ArchiveContent("content.test", " ", List.of()));
    }

    @Test
    void archiveDocumentModelDoesNotOwnContentNodes() {
        boolean documentContainsContentNode = Arrays.stream(
                        top.mores.intelligencearchive.common.model.ArchiveDocument.class.getRecordComponents()
                )
                .map(component -> component.getGenericType().getTypeName())
                .anyMatch(typeName -> typeName.contains(ContentNode.class.getName()));

        assertFalse(documentContainsContentNode);
    }
}
