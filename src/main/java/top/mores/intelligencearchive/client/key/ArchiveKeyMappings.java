package top.mores.intelligencearchive.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.mores.intelligencearchive.Intelligencearchive;

/** 客户端按键定义与注册；该类不会在 Dedicated Server 上加载。 */
@Mod.EventBusSubscriber(modid = Intelligencearchive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ArchiveKeyMappings {
    public static final KeyMapping OPEN_ARCHIVE = new KeyMapping(
            "key.intelligencearchive.open_archive",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.intelligencearchive"
    );

    private ArchiveKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ARCHIVE);
    }
}
