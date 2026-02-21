package salo2b.beer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WetBarleyItem extends Item {
    public WetBarleyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        long currentTime = level.getGameTime();

        // Если у предмета ещё нет метки времени, ставим текущую
        if (!stack.has(ModDataComponents.START_TIME.get())) {
            stack.set(ModDataComponents.START_TIME.get(), currentTime);
            return;
        }

        long startTime = stack.get(ModDataComponents.START_TIME.get());

        // 200 тиков = 10 секунд
        if (currentTime - startTime >= 200) {
            ItemStack maltStack = new ItemStack(ModItems.MALT.get(), stack.getCount());
            player.getInventory().setItem(slotId, maltStack);
        }
    }
}
