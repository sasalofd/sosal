package salo2b.beer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BeerMugItem extends BlockItem {

    public BeerMugItem(Block block, Properties properties) {
        super(block, properties);
    }

    // Этот метод отвечает за клик по блоку (попытка поставить)
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        // Если игрок НЕ крадется (не нажат Shift)
        if (player != null && !player.isCrouching()) {
            // Мы говорим игре: "Ничего не делай с блоком, давай пить"
            return InteractionResult.PASS;
        }

        // Если Shift нажат -> ставим блок как обычно
        return super.useOn(context);
    }

    // Этот метод отвечает за использование предмета (питье)
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        // Начинаем процесс питья/еды
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    // Указываем анимацию питья
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}
