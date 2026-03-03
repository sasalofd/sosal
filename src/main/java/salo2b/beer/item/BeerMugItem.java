package salo2b.beer.item;

import salo2b.beer.effect.DrunkEffect;
import salo2b.beer.registration.ModBlocks;
import salo2b.beer.registration.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BeerMugItem extends BlockItem {

    public BeerMugItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !player.isCrouching()) return InteractionResult.PASS;
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.canEat(true)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player player) {
            DrunkEffect.drink(player);
        }
        return super.finishUsingItem(stack, world, entity);
    }
}
