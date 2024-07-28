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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
            Set<Profile> newProfiles = new HashSet<>(Pridetags.profiles);
            Pridetags.profiles.clear();
            for (Profile profile : newProfiles) {
                try {
                    getAsyncProfile(profile.getUsername());
                } catch (URISyntaxException | IOException error) {
                    error.printStackTrace();
                }
            }
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
        File file = Pridetags.ConfigPath.resolve(flag + ".png").toFile();
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
        for (Profile profile : Pridetags.profiles) {
            if (Arrays.stream(profile.getFlags()).anyMatch(flagRes -> flagRes.getPath().equals(lowerCaseFlag + "_1"))) {
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

    public static String getPronoun(JsonObject profile) {
        JsonArray pronounsArray = profile.getAsJsonObject("profiles").getAsJsonObject("en").getAsJsonArray("pronouns");
        if (pronounsArray == null || pronounsArray.isEmpty()) return null;
        return pronounsArray.get(0).getAsJsonObject().get("value").getAsString();
    }

    private static void getAsyncProfile(String name) throws URISyntaxException, IOException {
        URL url = new URI(API_URL + "profile/get/" + name + "?version=2").toURL();
        JsonObject profile = null;
        try (InputStream stream = url.openStream()) {
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            profile = element.getAsJsonObject();
        }
        String pronoun = getPronoun(profile);
        ResourceLocation[] flags = getFlags(profile);
        Pridetags.profiles.add(new Profile(name, pronoun, flags));
    }


    public static ResourceLocation[] getFlags(JsonObject profile) throws IOException {
        JsonArray flagsArray = profile.getAsJsonObject("profiles").getAsJsonObject("en").getAsJsonArray("flags");
        ResourceLocation[] flags = new ResourceLocation[flagsArray.size()];
        for (int i = 0; i < flagsArray.size(); i++) {
            JsonElement flag = flagsArray.get(i);
            flags[i] = getFlagTexture(flag.getAsString());
        }
        return flags;
    }

    public static Profile getProfile(String name) {
        Optional<Profile> maybeProfile = Pridetags.profiles.stream().filter(profile -> profile.getUsername().equals(name)).findFirst();
        if (maybeProfile.isPresent()) return maybeProfile.get();
        queues.add(v -> {
            try {
                if (Pridetags.profiles.stream().noneMatch(profile -> profile.getUsername().equals(name)))
                    getAsyncProfile(name);
            } catch (URISyntaxException | IOException error) {
                error.printStackTrace();
            }
        });
        return null;
    }

}
