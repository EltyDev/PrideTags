package fr.elty.pridetags.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.elty.pridetags.Pridetags;
import fr.elty.pridetags.Profile;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.net.*;

public class PronounsAPI extends BaseAPI {

    public PronounsAPI() {
        super("pronouns_page", "https://en.pronouns.page/api/", "https://en.pronouns.page/flags/");
    }

    public String getPronoun(JsonObject profile) {
        JsonArray pronounsArray = profile.getAsJsonObject("profiles").getAsJsonObject("en").getAsJsonArray("pronouns");
        if (pronounsArray == null || pronounsArray.isEmpty()) return null;
        return pronounsArray.get(0).getAsJsonObject().get("value").getAsString();
    }

    public void getAsyncProfile(String name) throws URISyntaxException, IOException {
        URL url = new URI(this.apiUrl + "profile/get/" + name + "?version=2").toURL();
        JsonObject profile = null;
        try (InputStream stream = url.openStream()) {
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            profile = element.getAsJsonObject();
        }
        String pronoun = getPronoun(profile);
        ResourceLocation[] flags = getFlags(profile);
        Pridetags.profiles.add(new Profile(name, pronoun, flags));
    }

    public ResourceLocation[] getFlags(JsonObject profile) throws IOException {
        JsonArray flagsArray = profile.getAsJsonObject("profiles").getAsJsonObject("en").getAsJsonArray("flags");
        ResourceLocation[] flags = new ResourceLocation[flagsArray.size()];
        for (int i = 0; i < flagsArray.size(); i++) {
            JsonElement flag = flagsArray.get(i);
            String flagName = flag.getAsString();
            flags[i] = this.getFlagTexture(flagName + ".png", flagName);
        }
        return flags;
    }

}
