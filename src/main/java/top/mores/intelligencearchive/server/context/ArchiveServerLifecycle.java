package top.mores.intelligencearchive.server.context;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import top.mores.intelligencearchive.Intelligencearchive;

/** Forge 生命周期到纯业务 Runtime Context 的薄适配器。 */
public final class ArchiveServerLifecycle {
    private ArchiveServerLifecycle() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ArchiveServerLifecycle::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(ArchiveServerLifecycle::onServerStopped);
    }

    private static void onServerStarting(ServerStartingEvent event) {
        ArchiveRuntimeContexts.install(new ArchiveRuntimeContextBuilder().build());
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Runtime context initialized");
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        ArchiveRuntimeContexts.clear();
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Runtime context cleared");
    }
}
