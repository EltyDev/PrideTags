package fr.elty.pridetags.neoforge;

import fr.elty.pridetags.Pridetags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(Pridetags.MOD_ID)
public final class PridetagsNeoForge {

    public PridetagsNeoForge(IEventBus bus) {
        bus.addListener(PridetagsNeoForge::onClientSetup);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        Pridetags.ConfigPath = FMLPaths.getOrCreateGameRelativePath(Path.of("resources/pridetags_flags/"));
        Pridetags.init();
    }

}