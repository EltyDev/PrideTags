package fr.elty.pridetags.fabric.client;

import fr.elty.pridetags.Pridetags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class PridetagsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Pridetags.ConfigPath = FabricLoader.getInstance().getGameDir().resolve("resources/pridetags_flags/");
        Pridetags.init();
    }
}
