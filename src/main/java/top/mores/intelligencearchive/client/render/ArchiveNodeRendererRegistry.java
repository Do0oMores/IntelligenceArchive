package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * ViewNodeType 到独立 Renderer 的集中注册表。
 * 新节点只需新增 ViewNode、Renderer 和一条注册，不需要向 Screen 堆叠类型分支。
 */
public final class ArchiveNodeRendererRegistry {
    private final Map<ArchiveViewNodeType, ArchiveNodeRenderer> renderers;

    public ArchiveNodeRendererRegistry(ArchiveNodeRenderer... renderers) {
        Objects.requireNonNull(renderers, "renderers 不能为 null");
        EnumMap<ArchiveViewNodeType, ArchiveNodeRenderer> registry = new EnumMap<>(ArchiveViewNodeType.class);
        for (ArchiveNodeRenderer renderer : renderers) {
            ArchiveNodeRenderer validRenderer = Objects.requireNonNull(renderer, "renderer 不能为 null");
            if (registry.put(validRenderer.type(), validRenderer) != null) {
                throw new IllegalArgumentException("重复注册 Renderer: " + validRenderer.type());
            }
        }
        for (ArchiveViewNodeType type : ArchiveViewNodeType.values()) {
            if (!registry.containsKey(type)) {
                throw new IllegalArgumentException("缺少 Renderer: " + type);
            }
        }
        this.renderers = Map.copyOf(registry);
    }

    public static ArchiveNodeRendererRegistry createDefault() {
        return new ArchiveNodeRendererRegistry(
                new TextArchiveNodeRenderer(),
                new ImageArchiveNodeRenderer(),
                new AudioArchiveNodeRenderer(),
                new IntelLinkArchiveNodeRenderer(),
                new RedactedArchiveNodeRenderer()
        );
    }

    public ArchiveNodeRenderer rendererFor(ArchiveViewNode node) {
        ArchiveViewNode validNode = Objects.requireNonNull(node, "node 不能为 null");
        return renderers.get(validNode.type());
    }

    public int measure(ArchiveNodeRenderContext context, ArchiveViewNode node, int width) {
        return rendererFor(node).measure(context, node, width);
    }

    public void render(ArchiveNodeRenderContext context, ArchiveViewNode node, int x, int y, int width) {
        rendererFor(node).render(context, node, x, y, width);
    }

    public boolean click(ArchiveViewNode node, ArchiveLinkClickHandler linkClickHandler) {
        return rendererFor(node).click(node, linkClickHandler);
    }
}
