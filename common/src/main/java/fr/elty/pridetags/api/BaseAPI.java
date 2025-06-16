package fr.elty.pridetags.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import fr.elty.pridetags.Pridetags;
import fr.elty.pridetags.Profile;
import fr.elty.pridetags.WebPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public abstract class BaseAPI {

    protected final String apiName;
    protected final String apiUrl;
    protected final String flagsUrl;

    public BaseAPI(String name, String apiUrl, String flagsUrl) {
        this.apiName = name;
        this.apiUrl = apiUrl;
        this.flagsUrl = flagsUrl;
    }

    public NativeImage downloadImage(URI uri, File file) throws IOException {
        NativeImage image = null;
        URL url = uri.toURL();
        file.getParentFile().mkdirs();
        System.out.println(url);
        try (InputStream imageStream = url.openStream()) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] bytes = imageStream.readAllBytes();
                outputStream.write(bytes);
                if (WebPUtils.isWebP(new ByteArrayInputStream(bytes))) {
                    image = WebPUtils.loadWebP(new ByteArrayInputStream(bytes));
                } else
                    image = NativeImage.read(bytes);
            }
        }
        return image;
    }

    public ResourceLocation getFlagTexture(String path, String name) throws IOException {
        String extension = path.substring(path.lastIndexOf(".") + 1);
        File file = Pridetags.ConfigPath.resolve(this.apiName + "/" + name + "." + extension).toFile();
        NativeImage image;
        boolean toRegister = true;
        if (!file.exists() || file.length() == 0) {
            try {
                String absolutePath = flagsUrl + path;
                image = downloadImage(new URI(absolutePath.replaceAll(" ", "%20")), file);
            } catch (URISyntaxException | IOException error) {
                System.err.println("Failed to load flag texture: " + error);
                return null;
            }
        } else {
            if (WebPUtils.isWebP(new FileInputStream(file)))
                image = WebPUtils.loadWebP(new FileInputStream(file));
            else
                image = NativeImage.read(new FileInputStream(file));
        }
        String lowerCaseFlag = name.toLowerCase().replaceAll(" ", "_");
        for (Profile profile : Pridetags.profiles) {
            if (Arrays.stream(profile.getFlags()).anyMatch(flag -> flag.getPath().equals(lowerCaseFlag + "_1"))) {
                toRegister = false;
                break;
            }
        }
        ResourceLocation location =  ResourceLocation.fromNamespaceAndPath(Pridetags.MOD_ID,  apiName + "_flag_" + lowerCaseFlag);
        if (toRegister) {
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
        }
        return location;
    }

    public abstract boolean getAsyncProfile(String name);
    public abstract String getPronoun(JsonObject profile);
    public abstract ResourceLocation[] getFlags(JsonObject profile) throws IOException;

}
