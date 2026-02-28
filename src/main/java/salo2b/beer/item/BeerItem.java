package salo2b.beer.item;

import salo2b.beer.*;
import salo2b.beer.block.*;
import salo2b.beer.block.entity.*;
import salo2b.beer.item.*;
import salo2b.beer.menu.*;
import salo2b.beer.registration.*;
import salo2b.beer.villager.*;
import salo2b.beer.worldgen.*;
import salo2b.beer.client.renderer.*;
import salo2b.beer.client.screen.*;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class BeerItem extends Item {
    public BeerItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 1. Вызываем стандартный метод поедания (восстанавливает голод и тратит 1 предмет)
        // Мы сохраняем результат в переменную, чтобы вернуть её, если не нужно выдавать кружку
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        // 2. Накладываем эффекты
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0)); // Тошнота (10 сек)
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0)); // Слепота
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));  // Тьма
        }

        // 3. Логика возврата кружки
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            // ИСПРАВЛЕНО: Создаем переменную emptyMug
            ItemStack emptyMug = new ItemStack(ModBlocks.WOODEN_MUG.get());

            // Если стак, который мы только что выпили (resultStack), стал пустым (это было последнее пиво)
            if (resultStack.isEmpty()) {
                return emptyMug; // Просто превращаем его в кружку
            }

            // Если пиво еще осталось (было, например, 64 шт), то кладем кружку в инвентарь
            if (!player.getInventory().add(emptyMug)) {
                player.drop(emptyMug, false); // Если инвентарь полон, выбрасываем под ноги
            }
        }

        return resultStack;
    }
}
