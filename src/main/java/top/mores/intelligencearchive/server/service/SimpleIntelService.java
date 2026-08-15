package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.ArchiveDocumentType;
import top.mores.intelligencearchive.common.model.ArchiveMetadata;
import top.mores.intelligencearchive.common.model.ArchiveSecurityLevel;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelNode;
import top.mores.intelligencearchive.common.service.IntelRepository;
import top.mores.intelligencearchive.common.service.IntelService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Phase 2-A 使用的最小服务实现。
 *
 * <p>默认构造器使用私有内存 Repository 并预载测试档案；注入构造器允许未来在不改变
 * 调用方的情况下替换为数据库 Repository。本实现不保存玩家权限或解锁状态。</p>
 */
public final class SimpleIntelService implements IntelService {
    public static final String TEST_DOCUMENT_ID = "document.case.test_001";

    private final IntelRepository repository;

    /** 创建带有 CASE-TEST-001 测试数据的内存服务。 */
    public SimpleIntelService() {
        this(createTestRepository());
    }

    /** 创建使用指定 Repository 的服务，不隐式写入测试数据。 */
    public SimpleIntelService(IntelRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository 不能为 null");
    }

    @Override
    public Optional<ArchiveDocument> findDocumentById(String documentId) {
        return repository.findDocumentById(requireQueryId(documentId, "documentId"));
    }

    @Override
    public Optional<IntelNode> findNodeById(String nodeId) {
        return repository.findNodeById(requireQueryId(nodeId, "nodeId"));
    }

    @Override
    public List<IntelEdge> findRelations(String nodeId) {
        return List.copyOf(repository.findRelations(requireQueryId(nodeId, "nodeId")));
    }

    @Override
    public boolean existsDocument(String documentId) {
        return findDocumentById(documentId).isPresent();
    }

    private static IntelRepository createTestRepository() {
        InMemoryIntelRepository repository = new InMemoryIntelRepository();
        repository.saveDocument(new ArchiveDocument(
                TEST_DOCUMENT_ID,
                "测试档案",
                ArchiveDocumentType.DOCUMENT,
                "IntelligenceArchive Archive Domain Core 测试档案。",
                "archive/case/test001.md",
                new ArchiveMetadata(
                        Instant.parse("2026-08-16T00:00:00Z"),
                        "IntelligenceArchive",
                        ArchiveSecurityLevel.PUBLIC
                ),
                Set.of()
        ));
        return repository;
    }

    private static String requireQueryId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    /**
     * 只服务于 Phase 2-A 默认构造器的内存适配器。
     * Map 模拟持久化存储，但被 Repository 接口封装，不向 Service 调用方暴露。
     */
    private static final class InMemoryIntelRepository implements IntelRepository {
        private final Map<String, ArchiveDocument> documents = new LinkedHashMap<>();
        private final Map<String, IntelNode> nodes = new LinkedHashMap<>();
        private final List<IntelEdge> relations = new ArrayList<>();

        @Override
        public Optional<ArchiveDocument> findDocumentById(String documentId) {
            return Optional.ofNullable(documents.get(requireQueryId(documentId, "documentId")));
        }

        @Override
        public Optional<IntelNode> findNodeById(String nodeId) {
            return Optional.ofNullable(nodes.get(requireQueryId(nodeId, "nodeId")));
        }

        @Override
        public List<IntelEdge> findRelations(String nodeId) {
            String requestedNodeId = requireQueryId(nodeId, "nodeId");
            return relations.stream()
                    .filter(edge -> edge.sourceNodeId().equals(requestedNodeId)
                            || edge.targetNodeId().equals(requestedNodeId))
                    .toList();
        }

        @Override
        public void saveDocument(ArchiveDocument document) {
            ArchiveDocument validDocument = Objects.requireNonNull(document, "document 不能为 null");
            documents.put(validDocument.id(), validDocument);
        }

        @Override
        public void saveNode(IntelNode node) {
            IntelNode validNode = Objects.requireNonNull(node, "node 不能为 null");
            nodes.put(validNode.id(), validNode);
        }

        @Override
        public void saveRelation(IntelEdge edge) {
            relations.add(Objects.requireNonNull(edge, "edge 不能为 null"));
        }
    }
}
