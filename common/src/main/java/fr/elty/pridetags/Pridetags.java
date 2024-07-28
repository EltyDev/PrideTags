package fr.elty.pridetags;

import dev.architectury.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public final class Pridetags {

    public static final String MOD_ID = "pridetags";
    public static Path ConfigPath;

    public static ConcurrentHashMap<String, ResourceLocation[]> flagsDatabase = new ConcurrentHashMap<>();

    public static void init() {
        if (Platform.isFabric())
            ConfigPath = FabricLoader.getInstance().getConfigDir();
        else {
            //TODO To change later
            ConfigPath = FabricLoader.getInstance().getGameDir();
        }
        PronounsAPI.THREAD.start();
    }
}
