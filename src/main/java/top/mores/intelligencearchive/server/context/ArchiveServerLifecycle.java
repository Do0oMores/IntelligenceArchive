package top.mores.intelligencearchive.server.context;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.server.content.repository.ContentLoadError;
import top.mores.intelligencearchive.server.content.repository.ContentLoadReport;

/** Forge 生命周期到纯业务 Runtime Context 的薄适配器。 */
public final class ArchiveServerLifecycle {
    private ArchiveServerLifecycle() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ArchiveServerLifecycle::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(ArchiveServerLifecycle::onServerStopped);
    }

    private static void onServerStarting(ServerStartingEvent event) {
        ArchiveRuntimeContextBuilder builder = new ArchiveRuntimeContextBuilder(
                event.getServer().getResourceManager()
        );
        logContentLoadReport(builder.getContentLoadReport());
        ArchiveRuntimeContexts.install(builder.build());
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Runtime context initialized");
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        ArchiveRuntimeContexts.clear();
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Runtime context cleared");
    }

    private static void logContentLoadReport(ContentLoadReport report) {
        for (String documentId : report.loadedDocumentIds()) {
            Intelligencearchive.LOGGER.info(
                    "[IntelligenceArchive] Loaded archive content: {}",
                    documentId
            );
        }
        for (ContentLoadError error : report.errors()) {
            Intelligencearchive.LOGGER.warn(
                    "[IntelligenceArchive] Failed to load archive content {}: {}",
                    error.resourceId(),
                    error.message()
            );
        }
        Intelligencearchive.LOGGER.info(
                "[IntelligenceArchive] Archive content loading finished: {} loaded, {} failed",
                report.loaded(),
                report.failed()
        );
    }
}
