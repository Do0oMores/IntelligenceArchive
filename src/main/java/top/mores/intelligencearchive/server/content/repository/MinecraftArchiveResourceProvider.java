package top.mores.intelligencearchive.server.content.repository;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Minecraft ResourceManager 适配器。
 *
 * <p>ResourceManager 依赖被限制在 server infrastructure；common 内容模型不会知道资源包路径。</p>
 */
final class MinecraftArchiveResourceProvider implements ArchiveResourceProvider {
    private static final String ROOT_PATH = "archives";
    private static final String METADATA_FILE = "metadata.yml";
    private static final String CONTENT_FILE = "content.md";

    private final ResourceManager resourceManager;

    MinecraftArchiveResourceProvider(ResourceManager resourceManager) {
        this.resourceManager = Objects.requireNonNull(resourceManager, "resourceManager 不能为 null");
    }

    @Override
    public List<ArchiveResourceKey> findArchives() {
        Map<ResourceLocation, Resource> metadataResources = resourceManager.listResources(
                ROOT_PATH,
                location -> location.getPath().endsWith("/" + METADATA_FILE)
        );
        List<ArchiveResourceKey> keys = new ArrayList<>(metadataResources.size());
        for (ResourceLocation location : metadataResources.keySet()) {
            String path = location.getPath();
            String suffix = "/" + METADATA_FILE;
            String directoryWithRoot = path.substring(0, path.length() - suffix.length());
            String directory = directoryWithRoot.substring((ROOT_PATH + "/").length());
            keys.add(new ArchiveResourceKey(location.getNamespace(), directory));
        }
        keys.sort(Comparator.comparing(ArchiveResourceKey::resourceId));
        return List.copyOf(keys);
    }

    @Override
    public String readMetadata(ArchiveResourceKey key) throws IOException {
        return read(key, METADATA_FILE);
    }

    @Override
    public String readMarkdown(ArchiveResourceKey key) throws IOException {
        return read(key, CONTENT_FILE);
    }

    private String read(ArchiveResourceKey key, String fileName) throws IOException {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                key.namespace(),
                ROOT_PATH + "/" + key.directory() + "/" + fileName
        );
        Resource resource = resourceManager.getResource(location).orElseThrow(
                () -> new IOException("缺少资源: " + location)
        );
        try (var inputStream = resource.open()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
