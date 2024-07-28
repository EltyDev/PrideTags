package fr.elty.pridetags;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PronounsAPI {

    public static final String API_URL = "https://en.pronouns.page/api/";
    public static final String FLAGS_URL = "https://en.pronouns.page/flags/";

    public static final Thread THREAD = new Thread(PronounsAPI::asyncLoop);
    public static Queue<Consumer<Void>> queues = new ConcurrentLinkedQueue<>();
    public static AtomicBoolean finished = new AtomicBoolean(false);

    private static void asyncLoop() {
        long lastTime = System.currentTimeMillis();
        while (!finished.get()) {
            while (!queues.isEmpty()) {
                Consumer<Void> consumer = queues.poll();
                consumer.accept(null);
            }
            if (lastTime + 600000 > System.currentTimeMillis()) continue;
            refreshFlags();
            lastTime = System.currentTimeMillis();
        }
    }

    public static NativeImage downloadImage(URI uri, File file) throws IOException {
        NativeImage image = null;
        URL url = uri.toURL();
        file.getParentFile().mkdirs();
        try (InputStream imageStream = url.openStream()) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] bytes = imageStream.readAllBytes();
                outputStream.write(bytes);
                image = NativeImage.read(bytes);
            }
        }
        return image;
    }

    public static ResourceLocation getFlagTexture(String flag) throws IOException {
        File file = Pridetags.ConfigPath.resolve("flags/" + flag + ".png").toFile();
        NativeImage image;
        boolean toRegister = true;
        if (!file.exists()) {
            try {
                String path = FLAGS_URL + flag + ".png";
                image = downloadImage(new URI(path.replaceAll(" ", "%20")), file);
            } catch (URISyntaxException error) {
                System.err.println("Failed to load flag texture: " + error);
                return null;
            }
        } else {
            image = NativeImage.read(new FileInputStream(file));
        }
        String lowerCaseFlag = flag.toLowerCase().replaceAll(" ", "_");
        for (ResourceLocation[] flags : Pridetags.flagsDatabase.values()) {
            if (Arrays.stream(flags).anyMatch(flagRes -> flagRes.getPath().equals(lowerCaseFlag + "_1"))) {
                toRegister = false;
                break;
            }
        }
        if (toRegister) {
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            return Minecraft.getInstance().getTextureManager().register(lowerCaseFlag, dynamicTexture);
        }
        return ResourceLocation.withDefaultNamespace("dynamic/" + lowerCaseFlag + "_1");
    }

    private static void asyncGetFlagsOf(String name) throws URISyntaxException, IOException {
        URL url = new URI(API_URL + "profile/get/" + name + "?version=2").toURL();
        JsonObject json = null;
        try (InputStream stream = url.openStream()) {
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            json = element.getAsJsonObject();
        }
        JsonArray flagsArray = json.getAsJsonObject("profiles").getAsJsonObject("en").getAsJsonArray("flags");
        ResourceLocation[] flags = new ResourceLocation[flagsArray.size()];
        for (int i = 0; i < flagsArray.size(); i++) {
            JsonElement flag = flagsArray.get(i);
            flags[i] = getFlagTexture(flag.getAsString());
        }
        Pridetags.flagsDatabase.put(name, flags);
    }

    public static ResourceLocation[] getFlagsOf(String name) {
        if (Pridetags.flagsDatabase.containsKey(name))
            return Pridetags.flagsDatabase.get(name);
        queues.add(v -> {
            try {
                if (!Pridetags.flagsDatabase.containsKey(name))
                    asyncGetFlagsOf(name);
            } catch (URISyntaxException | IOException error) {
                error.printStackTrace();
            }
        });
        return null;
    }

    public static void refreshFlags() {
        Pridetags.flagsDatabase.clear();
    }

}
