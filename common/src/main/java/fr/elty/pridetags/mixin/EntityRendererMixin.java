package fr.elty.pridetags.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.elty.pridetags.PronounsAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow public abstract Font getFont();
    @Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", shift = At.Shift.AFTER, ordinal = 0))
    private void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, CallbackInfo ci, @Local Matrix4f matrix4f, @Local Font font) {
        if (!(entity instanceof Player player) || entity.isDiscrete()) return;
        String username = player.getName().getString();
        ResourceLocation[] flags = null;
        try {
            flags = PronounsAPI.getFlagsOf(username);
        } catch (Exception error) {
            error.printStackTrace();
        }
        if (flags == null) return;
        int width = font.width(component)/2 + 4;
        float offsetX = 21.3f / 1.75f;
        float offsetY = 12.8f / 1.75f;
        float padding = 0;
        for (ResourceLocation flag : flags) {
            if (flag == null) continue;
            RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(flag).getId());
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(flag));
            vertexConsumer.addVertex(matrix4f, width + padding, 0, 0).setColor(255, 255, 255, 255).setUv(0, 1).setLight(i);
            vertexConsumer.addVertex(matrix4f, width + padding, offsetY, 0).setColor(255, 255, 255, 255).setUv(0, 0).setLight(i);
            vertexConsumer.addVertex(matrix4f,offsetX + width + padding, offsetY, 0).setColor(255, 255, 255, 255).setUv(1, 0).setLight(i);
            vertexConsumer.addVertex(matrix4f, offsetX + width + padding, 0, 0).setColor(255, 255, 255, 255).setUv(1, 1).setLight(i);
            padding += offsetX + 2;
        }
    }
}
