package fr.elty.pridetags.forge;

import fr.elty.pridetags.Pridetags;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Pridetags.MOD_ID)
public final class PridetagsForge {
    public PridetagsForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Pridetags.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Pridetags.init();
    }
}
