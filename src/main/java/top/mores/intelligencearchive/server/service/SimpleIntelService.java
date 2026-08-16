package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.IntelEdge;
import top.mores.intelligencearchive.common.model.IntelNode;
import top.mores.intelligencearchive.common.service.IntelRepository;
import top.mores.intelligencearchive.common.service.IntelService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * IntelRepository 的最小查询服务实现。
 *
 * <p>默认构造器提供空的内存存储，保证尚未接入世界数据源时服务端仍可安全启动。
 * 正式数据源通过注入构造器提供。</p>
 */
public final class SimpleIntelService implements IntelService {
    private final IntelRepository repository;

    /** 创建不包含任何固定内容的内存服务。 */
    public SimpleIntelService() {
        this(new InMemoryIntelRepository());
    }

    /** 创建使用指定 Repository 的服务，不隐式写入固定内容。 */
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

    private static String requireQueryId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    /**
     * 只服务于默认安全启动路径的空内存适配器。
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
