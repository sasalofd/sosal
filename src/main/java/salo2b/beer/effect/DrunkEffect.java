package salo2b.beer.effect;

import salo2b.beer.registration.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DrunkEffect extends BeerEffect {
    public DrunkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x000000); // Контроллер
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide) {
            CompoundTag data = player.getPersistentData();
            int mugs = data.getInt("beer.mug_count");
            
            // 1. ПРОТРЕЗВЛЕНИЕ (раз в 4 минуты снижаем уровень)
            int soberTicks = data.getInt("beer.sober_ticks") + 1;
            if (soberTicks >= 4800) { 
                if (mugs > 0) {
                    mugs--;
                    data.putInt("beer.mug_count", mugs);
                    data.putInt("beer.sober_ticks", 0);
                    // Устанавливаем флаг для PlayerTickEvent
                    data.putBoolean("beer.needs_update", true);
                    return true; 
                }
                soberTicks = 0;
            }
            data.putInt("beer.sober_ticks", soberTicks);

            // 2. ЛОГИКА АКТИВАЦИИ
            int state = data.getInt("beer.state");
            boolean hasMild = player.hasEffect(ModEffects.MILD_DRUNK);
            boolean hasStrong = player.hasEffect(ModEffects.STRONG_DRUNK);
            boolean hasHangover = player.hasEffect(ModEffects.HANGOVER);
            boolean isProcessing = state > 0 || hasMild || hasStrong || hasHangover;

            if (mugs >= 4 && (data.getBoolean("beer.trigger_pending") || isProcessing)) {
                updateDrunkStages(player, data);
            } else if (mugs < 4) {
                clearStages(player, data);
            }
        }
        return true;
    }

    private void updateDrunkStages(Player player, CompoundTag data) {
        boolean hasMild = player.hasEffect(ModEffects.MILD_DRUNK);
        boolean hasStrong = player.hasEffect(ModEffects.STRONG_DRUNK);
        boolean hasHangover = player.hasEffect(ModEffects.HANGOVER);
        
        int state = data.getInt("beer.state");
        int countdown = data.getInt("beer.countdown");

        if (hasMild) {
            tickMild(player, data);
            if (player.getEffect(ModEffects.MILD_DRUNK).getDuration() <= 2) {
                data.putBoolean("beer.allow_remove", true);
                player.removeEffect(ModEffects.MILD_DRUNK);
                data.putBoolean("beer.allow_remove", false);
                applyStrong(player, data, 1100 + player.getRandom().nextInt(300));
            }
            return;
        }
        if (hasStrong) {
            tickStrong(player, data);
            if (player.getEffect(ModEffects.STRONG_DRUNK).getDuration() <= 2) {
                data.putBoolean("beer.allow_remove", true);
                player.removeEffect(ModEffects.STRONG_DRUNK);
                data.putBoolean("beer.allow_remove", false);
                applyHangover(player, data);
            }
            return;
        }
        if (hasHangover) {
            tickHangover(player, data);
            if (player.getEffect(ModEffects.HANGOVER).getDuration() <= 2) {
                data.putBoolean("beer.allow_remove", true);
                player.removeEffect(ModEffects.HANGOVER);
                data.putBoolean("beer.allow_remove", false);
                data.putInt("beer.state", 0);
                data.putBoolean("beer.trigger_pending", false); 
            }
            return;
        }

        if (state == 0 && data.getBoolean("beer.trigger_pending")) { 
            data.putInt("beer.state", 1); 
            data.putInt("beer.countdown", 200 + player.getRandom().nextInt(1000)); 
        } else if (state == 1) { 
            countdown--;
            if (countdown <= 0) {
                if (data.getBoolean("beer.skip_to_strong")) {
                    data.remove("beer.skip_to_strong");
                    applyStrong(player, data, 1100 + player.getRandom().nextInt(300));
                } else {
                    applyMild(player, data);
                }
            } else {
                data.putInt("beer.countdown", countdown);
            }
        }
    }

    private void applyMild(Player player, CompoundTag data) {
        data.putInt("beer.state", 2); 
        data.putInt("beer.fades_left", 1);
        data.putInt("beer.drop_timer", 600 + player.getRandom().nextInt(200));
        player.addEffect(new MobEffectInstance(ModEffects.MILD_DRUNK, 1200, 0, false, true, true));
    }

    private void applyStrong(Player player, CompoundTag data, int duration) {
        data.putInt("beer.state", 3); 
        data.putInt("beer.fades_left", 4 + player.getRandom().nextInt(2));
        data.putInt("beer.drop_timer", 400 + player.getRandom().nextInt(200));
        player.addEffect(new MobEffectInstance(ModEffects.STRONG_DRUNK, duration, 0, false, true, true));
    }

    private void applyHangover(Player player, CompoundTag data) {
        data.putInt("beer.state", 4); 
        int duration = 800 + player.getRandom().nextInt(600);
        player.addEffect(new MobEffectInstance(ModEffects.HANGOVER, duration, 0, false, true, true));
    }

    private void tickMild(Player player, CompoundTag data) {
        handleDrop(player, data, 600 + player.getRandom().nextInt(200));
        handleBlackout(player, data, 0.005f);
    }

    private void tickStrong(Player player, CompoundTag data) {
        handleDrop(player, data, 400 + player.getRandom().nextInt(200));
        handleBlackout(player, data, 0.008f);
    }

    private void tickHangover(Player player, CompoundTag data) {
        player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 100, 0, false, false));
        player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false));
    }

    private void handleDrop(Player player, CompoundTag data, int nextTime) {
        int timer = data.getInt("beer.drop_timer") - 1;
        if (timer <= 0) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                player.drop(stack.copy(), true);
                stack.setCount(0);
            }
            data.putInt("beer.drop_timer", nextTime);
        } else {
            data.putInt("beer.drop_timer", timer);
        }
    }

    private void handleBlackout(Player player, CompoundTag data, float chance) {
        if (data.getInt("beer.fades_left") > 0 && player.getRandom().nextFloat() < chance) {
            if (!player.hasEffect(ModEffects.BLACKOUT)) {
                data.putInt("beer.fades_left", data.getInt("beer.fades_left") - 1);
                player.addEffect(new MobEffectInstance(ModEffects.BLACKOUT, 80, 0, false, false, false));
            }
        }
    }

    private void clearDrunkenness(Player player) {
        CompoundTag data = player.getPersistentData();
        data.putInt("beer.mug_count", 0);
        data.putInt("beer.state", 0);
        data.putInt("beer.sober_ticks", 0);
        data.putBoolean("beer.trigger_pending", false);
        data.putBoolean("beer.allow_remove", true);
        player.removeEffect(ModEffects.DRUNK);
        data.putBoolean("beer.allow_remove", false);
    }

    private void clearStages(Player player, CompoundTag data) {
        data.putBoolean("beer.allow_remove", true);
        if (player.hasEffect(ModEffects.MILD_DRUNK)) player.removeEffect(ModEffects.MILD_DRUNK);
        if (player.hasEffect(ModEffects.STRONG_DRUNK)) player.removeEffect(ModEffects.STRONG_DRUNK);
        if (player.hasEffect(ModEffects.HANGOVER)) player.removeEffect(ModEffects.HANGOVER);
        data.putBoolean("beer.allow_remove", false);
        data.putInt("beer.state", 0);
    }

    public static void drink(Player player) {
        CompoundTag data = player.getPersistentData();
        int mugs = data.getInt("beer.mug_count") + 1;
        if (mugs > 6) mugs = 6;
        data.putInt("beer.mug_count", mugs);
        data.putInt("beer.sober_ticks", 0); 
        data.putBoolean("beer.trigger_pending", true); 
        
        long lastDrink = data.getLong("beer.last_drink_time");
        long currentTime = player.level().getGameTime();
        data.putLong("beer.last_drink_time", currentTime);

        int state = data.getInt("beer.state");
        boolean drinkFast = (currentTime - lastDrink < 200);

        if (mugs >= 4) {
            if (state == 4) { 
                data.putInt("beer.state", 1); 
                data.putInt("beer.countdown", 240 + player.getRandom().nextInt(460));
                data.putBoolean("beer.skip_to_strong", true);
                data.putBoolean("beer.trigger_pending", false);
                data.putBoolean("beer.allow_remove", true);
                player.removeEffect(ModEffects.HANGOVER);
                data.putBoolean("beer.allow_remove", false);
            } else if (state > 0 && (drinkFast || state == 2)) { 
                if (state == 2) {
                    data.putBoolean("beer.allow_remove", true);
                    player.removeEffect(ModEffects.MILD_DRUNK);
                    data.putBoolean("beer.allow_remove", false);
                }
                ((DrunkEffect)ModEffects.DRUNK.get()).applyStrong(player, data, 1100 + player.getRandom().nextInt(300));
            }
        }

        data.putBoolean("beer.needs_update", true);
    }
}
