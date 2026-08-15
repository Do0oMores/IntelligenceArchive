package top.mores.intelligencearchive;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.server.context.ArchiveServerLifecycle;

/**
 * MOD 的公共入口。
 *
 * <p>入口只组装公共模块，不引用 Minecraft、Screen 等客户端专属类型，
 * 从而保证同一份 MOD 可以安全地在 Dedicated Server 上初始化。</p>
 */
@Mod(Intelligencearchive.MOD_ID)
public final class Intelligencearchive {
    public static final String MOD_ID = "intelligencearchive";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Intelligencearchive() {
        LOGGER.info("[IntelligenceArchive] Initializing");
        ArchiveNetwork.initialize();
        ArchiveServerLifecycle.register();
    }
}
