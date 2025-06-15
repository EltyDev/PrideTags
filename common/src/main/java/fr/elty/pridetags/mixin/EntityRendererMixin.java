package fr.elty.pridetags.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import fr.elty.pridetags.Profile;
import fr.elty.pridetags.PronounsAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Unique
    private static final int X_GAP = 1;
    @Unique
    private static final int Y_GAP = 3;

    @Unique
    public boolean isRenderingScore(Player player, Component component) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(Scoreboard.DISPLAY_SLOT_BELOW_NAME);
        if (objective == null) return false;
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
        MutableComponent scoreComponent = Component.literal(String.valueOf(score.getScore())).append(Component.literal(" ")).append(objective.getDisplayName());
        if (component.getString().equals(scoreComponent.getString())) return true;
        return false;
    }

    @Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    private void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci, @Local Matrix4f matrix4f, @Local Font font, @Local boolean bl, @Local(ordinal = 1) int j, @Local(ordinal = 2) int k) {
        if (!(entity instanceof Player player)) return;
        if (isRenderingScore(player, component)) return;
        String username = player.getName().getString();
        Profile profile = null;
        try {
            profile = PronounsAPI.getProfile(username);
        } catch (Exception error) {
            error.printStackTrace();
        }
        if (profile == null) return;
        ResourceLocation[] flags = profile.getFlags();
        int height = font.lineHeight + Y_GAP;
        float offsetX = 21.3f / 1.75f;
        float offsetY = 12.8f / 1.75f;
        float totalWidth = (flags.length) * offsetX + X_GAP * (flags.length - 1);
        float padding = 0;
        for (ResourceLocation flag : flags) {
            if (!bl) break;
            if (flag == null) continue;
            RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(flag).getId());
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(flag));
            vertexConsumer.vertex(matrix4f, padding - totalWidth/2 , -height - j, 0).color(255, 255, 255, 255).uv(0, 1).uv2(i & '\uffff', i >> 16 & '\uffff').endVertex();
            vertexConsumer.vertex(matrix4f, padding - totalWidth/2, offsetY - height - j, 0).color(255, 255, 255, 255).uv(0, 0).uv2(i & '\uffff', i >> 16 & '\uffff').endVertex();
            vertexConsumer.vertex(matrix4f,offsetX + padding - totalWidth/2, offsetY - height - j, 0).color(255, 255, 255, 255).uv(1, 0).uv2(i & '\uffff', i >> 16 & '\uffff').endVertex();
            vertexConsumer.vertex(matrix4f, offsetX + padding- totalWidth/2, -height - j, 0).color(255, 255, 255, 255).uv(1, 1).uv2(i & '\uffff', i >> 16 & '\uffff').endVertex();
            padding += offsetX + X_GAP;
        }
        if (profile.getPronoun() == null) return;
        MutableComponent pronoun = Component.empty().append(profile.getPronoun());
        float halfWidth = (float)(-font.width(pronoun) / 2);
        poseStack.scale(0.5F, 0.5F, 1);
        if (!bl) return;
        font.drawInBatch(pronoun, halfWidth, -height * 3 - j, 553648127, false, matrix4f, multiBufferSource, true, k, i);
        font.drawInBatch(pronoun, halfWidth, -height * 3 - j, -1, false, matrix4f, multiBufferSource, false, 0, i);
    }
}
