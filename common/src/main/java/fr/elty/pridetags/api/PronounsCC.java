package fr.elty.pridetags.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.elty.pridetags.Pridetags;
import fr.elty.pridetags.Profile;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class PronounsCC extends BaseAPI {

    public PronounsCC() {
        super("pronouns_cc", "https://pronouns.cc/api/v1/", "https://cdn.pronouns.cc/flags/");
    }

    public String getPronoun(JsonObject profile) {
        if (profile == null) return null;
        JsonArray pronounsArray = profile.getAsJsonArray("pronouns");
        if (pronounsArray == null || pronounsArray.isEmpty()) return null;
        String pronoun = pronounsArray.get(0).getAsJsonObject().get("pronouns").getAsString();
        int end =  StringUtils.ordinalIndexOf(pronoun, "/", 2);
        if (end == -1) return pronoun;
        return pronoun.substring(0, end);
    }

    public boolean getAsyncProfile(String name) {
        try {
            URL url = new URI(this.apiUrl + "users/" + name).toURL();
            JsonObject profile = null;
            try (InputStream stream = url.openStream()) {
                JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
                profile = element.getAsJsonObject();
            }
            String pronoun = getPronoun(profile);
            ResourceLocation[] flags = getFlags(profile);
            Pridetags.profiles.add(new Profile(name, pronoun, flags));
            return true;
        } catch (URISyntaxException | IOException error) {
            return false;
        }
    }

    public ResourceLocation[] getFlags(JsonObject profile) throws IOException {
        if (profile == null) return null;
        JsonArray flagsArray = profile.getAsJsonArray("flags");
        if (flagsArray == null || flagsArray.isEmpty()) return null;
        ResourceLocation[] flags = new ResourceLocation[flagsArray.size()];
        for (int i = 0; i < flagsArray.size(); i++) {
            JsonObject flag = flagsArray.get(i).getAsJsonObject();
            String flagName = flag.get("name").getAsString();
            flags[i] = this.getFlagTexture(flag.get("hash").getAsString() + ".webp", flagName);
        }
        return flags;
    }


}
