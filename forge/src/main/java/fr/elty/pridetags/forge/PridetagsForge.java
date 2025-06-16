package fr.elty.pridetags.forge;

import fr.elty.pridetags.Pridetags;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(Pridetags.MOD_ID)
public final class PridetagsForge {

    public PridetagsForge(FMLJavaModLoadingContext context) {
        EventBuses.registerModEventBus(Pridetags.MOD_ID, context.getModEventBus());
        context.getModEventBus().addListener(PridetagsForge::onClientSetup);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        Pridetags.ConfigPath = FMLPaths.getOrCreateGameRelativePath(Path.of("resources/"), "pridetags_flags");
        Pridetags.init();
    }
}
