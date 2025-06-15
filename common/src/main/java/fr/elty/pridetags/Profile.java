package fr.elty.pridetags;

import net.minecraft.resources.ResourceLocation;

public class Profile {

    private final String username;
    private final String pronoun;

    private ResourceLocation[] flags;

    public Profile(String username, String pronoun, ResourceLocation[] flags) {
        this.username = username;
        this.pronoun = pronoun;
        this.flags = flags;
    }

    public String getUsername() { return username; }
    public String getPronoun() { return pronoun; }
    public ResourceLocation[] getFlags() { return flags; }

}
