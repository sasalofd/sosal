package salo2b.beer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BeerMugItem extends BlockItem {

    public BeerMugItem(Block block, Properties properties) {
        super(block, properties);
    }

    // 1. Установка на Shift + ПКМ
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !player.isCrouching()) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    // 2. Исправляем питье: используем стандартную логику еды
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        // Проверяем, может ли игрок съесть/выпить это
        if (player.canEat(itemstack.getFoodProperties(player).canAlwaysEat())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    // Этот метод ВАЖЕН для применения эффектов еды в 1.21.1
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        // super.finishUsingItem сам применит FoodProperties (эффекты)
        return super.finishUsingItem(stack, world, entity);
    }
}
