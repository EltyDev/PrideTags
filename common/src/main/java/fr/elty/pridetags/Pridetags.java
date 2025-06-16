package fr.elty.pridetags;

import dev.architectury.platform.Platform;
import io.netty.util.internal.ConcurrentSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Pridetags {

    public static final String MOD_ID = "pridetags";
    public static Path ConfigPath;

    public static Set<Profile> profiles = ConcurrentHashMap.newKeySet();

    public static void init() {
        PronounsAPI.THREAD.start();
    }
}
