package com.miskatonicmysteries.mixin.client;

import com.miskatonicmysteries.api.banner.impl.LoomPatternContainer;
import com.miskatonicmysteries.api.banner.impl.LoomPatternData;
import com.miskatonicmysteries.api.banner.impl.LoomPatternRenderContext;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BannerBlockEntityRenderer.class)
public abstract class BannerBlockEntityRendererMixin {

	@Unique
	private static List<LoomPatternData> mmLoomPatterns;

	@Unique
	private static int mmNextLoomPatternIndex;

    /**
     * Saves Banner++ loom pattens in a field for rendering.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void mm$prePatternRender(
    BannerBlockEntity banner,
    float f1,
    MatrixStack stack,
    VertexConsumerProvider provider,
    int i,
    int j,
    CallbackInfo info) {
        LoomPatternRenderContext.setLoomPatterns(((LoomPatternContainer) banner).bannermm_getLoomPatterns());
    }

    @Inject(
    method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLjava/util/List;Z)V",
    at = @At("HEAD")
    )
    private static void mm$resetLocalCtx(CallbackInfo info) {
        mmNextLoomPatternIndex = 0;
        mmLoomPatterns = LoomPatternRenderContext.getLoomPatterns();
    }

    /**
     * Renders Banner++ loom patterns in line with vanilla banner patterns.
     */
    @Inject(
    method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLjava/util/List;Z)V",
    at = @At(
    value = "INVOKE",
    target = "Ljava/util/List;get(I)Ljava/lang/Object;",
    ordinal = 0,
    remap = false
    ),
    locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void mm$patternRenderInline(
    MatrixStack stack,
    VertexConsumerProvider provider,
    int light,
    int overlay,
    ModelPart canvas,
    SpriteIdentifier baseSprite,
    boolean isBanner,
    List<Pair<BannerPattern, DyeColor>> patterns,
    boolean glint,
    CallbackInfo info,
    int idx) {
        while (mmNextLoomPatternIndex < mmLoomPatterns.size()) {
            LoomPatternData data = mmLoomPatterns.get(mmNextLoomPatternIndex);

            if (data.index() == idx - 1) {
                mm$renderLoomPattern(data, stack, provider, canvas, light, overlay, isBanner);
                mmNextLoomPatternIndex++;
            } else {
                break;
            }
        }
    }

    /**
     * Renders Banner++ loom patterns that occur after all vanilla banner patterns.
     */
    @Inject(
    method = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLjava/util/List;Z)V",
    at = @At("RETURN")
    )
    private static void mm$patternRenderPost(
    MatrixStack stack,
    VertexConsumerProvider provider,
    int light,
    int overlay,
    ModelPart canvas,
    SpriteIdentifier baseSprite,
    boolean isBanner,
    List<Pair<BannerPattern, DyeColor>> patterns,
    boolean glint,
    CallbackInfo info) {
        for (int i = mmNextLoomPatternIndex; i < mmLoomPatterns.size(); i++) {
            mm$renderLoomPattern(mmLoomPatterns.get(i), stack, provider, canvas, light, overlay, isBanner);
        }

        mmLoomPatterns = Collections.emptyList();
    }

    @Unique
    private static void mm$renderLoomPattern(
    LoomPatternData data,
    MatrixStack stack,
    VertexConsumerProvider provider,
    ModelPart canvas,
    int light,
    int overlay,
    boolean notShield) {
        Identifier spriteId = data.pattern().getSpriteId(notShield ? "banner" : "shield");
        SpriteIdentifier realSpriteId = new SpriteIdentifier(notShield ? TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE : TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, spriteId);
        float[] color = data.color().getColorComponents();
        canvas.render(stack, realSpriteId.getVertexConsumer(provider, RenderLayer::getEntityNoOutline), light, overlay, color[0], color[1], color[2], 1.0f);
    }
}