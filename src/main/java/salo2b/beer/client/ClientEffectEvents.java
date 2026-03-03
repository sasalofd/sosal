package salo2b.beer.client;

import salo2b.beer.BeerMod;
import salo2b.beer.registration.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = BeerMod.MODID, value = Dist.CLIENT)
public class ClientEffectEvents {

    private static float autoForward = 0;
    private static float autoLeft = 0;
    private static int directionTimer = 0;
    
    private static float currentIntensity = 0;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean isMild = mc.player.hasEffect(ModEffects.MILD_DRUNK);
            boolean isStrong = mc.player.hasEffect(ModEffects.STRONG_DRUNK);
            boolean isHangover = mc.player.hasEffect(ModEffects.HANGOVER);
            
            float targetIntensity = (isMild || isStrong || isHangover) ? 1.0f : 0.0f;
            float stageMultiplier = isStrong ? 1.0f : (isMild ? 0.4f : (isHangover ? 0.2f : 0.0f));
            
            if (currentIntensity < targetIntensity) {
                currentIntensity += 0.005f; 
            } else if (currentIntensity > targetIntensity) {
                currentIntensity -= 0.002f; 
            }

            if (currentIntensity > 0) {
                float ticks = mc.level.getGameTime() + (float)event.getPartialTick();
                
                MobEffectInstance drunk = mc.player.getEffect(ModEffects.DRUNK);
                float ampFactor = 0.35f; 
                if (drunk != null) {
                    ampFactor += (drunk.getAmplifier() * 0.06f);
                }
                
                float power = currentIntensity * ampFactor * stageMultiplier;

                float yawSway = (float)Math.sin(ticks * 0.015f) * 18.0f * power;
                float pitchSway = (float)Math.cos(ticks * 0.01f) * 12.0f * power;
                float rollSway = (float)Math.sin(ticks * 0.02f) * 8.0f * power;

                event.setYaw(event.getYaw() + yawSway);
                event.setPitch(event.getPitch() + pitchSway);
                event.setRoll(event.getRoll() + rollSway);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (currentIntensity > 0.05f) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                boolean isStrong = mc.player.hasEffect(ModEffects.STRONG_DRUNK);
                boolean isMild = mc.player.hasEffect(ModEffects.MILD_DRUNK);
                boolean isHangover = mc.player.hasEffect(ModEffects.HANGOVER);
                
                float stageMultiplier = isStrong ? 1.0f : (isMild ? 0.4f : (isHangover ? 0.2f : 0.0f));
                float ticks = mc.level.getGameTime();
                float blur = (float)Math.sin(ticks * 0.012f) * 3.0f * currentIntensity * stageMultiplier;
                event.setFOV(event.getFOV() + blur);
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // АВТОПИЛОТ: Разные стороны и меньше длительность
            if (mc.player.hasEffect(ModEffects.STRONG_DRUNK) && mc.player.hasEffect(ModEffects.BLACKOUT)) {
                if (directionTimer <= 0) {
                    // Теперь может идти и назад (-0.3) и в стороны сильнее
                    autoForward = (mc.player.getRandom().nextFloat() - 0.3f) * 0.6f;
                    autoLeft = (mc.player.getRandom().nextFloat() - 0.5f) * 0.8f;
                    // Меньше длительность шага (0.5 - 1.2 сек)
                    directionTimer = 10 + mc.player.getRandom().nextInt(15); 
                }
                directionTimer--;
                event.getInput().forwardImpulse = autoForward;
                event.getInput().leftImpulse = autoLeft;
            } else {
                directionTimer = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            MobEffectInstance blackout = mc.player.getEffect(ModEffects.BLACKOUT);
            if (blackout != null) {
                float duration = blackout.getDuration();
                float maxDuration = 80.0f; 
                float alpha = 0;
                
                // БЫСТРОЕ ЗАТУХАНИЕ (за 15 тиков вместо 40)
                float transitionTicks = 15.0f;
                if (duration > maxDuration - transitionTicks) {
                    alpha = (maxDuration - duration) / transitionTicks;
                } else if (duration < transitionTicks) {
                    alpha = duration / transitionTicks;
                } else {
                    alpha = 1.0f; // Держим черным в середине долго
                }
                
                renderTotalBlackout(event.getGuiGraphics(), Math.min(1.0f, alpha));
            }
        }
    }

    private static void renderTotalBlackout(GuiGraphics graphics, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int color = ((int)(alpha * 255) << 24); 
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
