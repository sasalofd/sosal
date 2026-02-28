package salo2b.beer.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawStructure.class)
public class StructurePlacementMixin {
    @Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void beer$perfectPlacementCheck(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        Structure structure = (Structure) (Object) this;
        var registry = context.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        var structureKey = registry.getResourceKey(structure);

        if (structureKey.isPresent() && structureKey.get().location().getPath().equals("beer_bar")) {
            ChunkGenerator generator = context.chunkGenerator();
            BlockPos center = context.chunkPos().getMiddleBlockPosition(0);
            LevelHeightAccessor heightAccessor = context.heightAccessor();
            int centerX = center.getX();
            int centerZ = center.getZ();

            // Определяем высоту центра
            int baseHeight = generator.getFirstFreeHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, context.randomState());

            // ПРОВЕРКА ПЛОЩАДКИ (Радиус 10 блоков для полного покрытия структуры)
            for (int x = -10; x <= 10; x += 2) {
                for (int z = -10; z <= 10; z += 2) {
                    int h = generator.getFirstFreeHeight(centerX + x, centerZ + z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, context.randomState());
                    
                    // 1. ПРОВЕРКА НА ПУСТОТУ (Чтобы не висел над обрывом)
                    var groundState = generator.getBaseColumn(centerX + x, centerZ + z, heightAccessor, context.randomState()).getBlock(h - 1);
                    if (groundState.isAir() || groundState.is(Blocks.WATER) || groundState.is(Blocks.LAVA)) {
                        cir.setReturnValue(Optional.empty());
                        return;
                    }

                    // 2. ПРОВЕРКА НА ГОРУ (Чтобы не врезался в скалы рядом)
                    // Если блок рядом выше центра более чем на 1 — это склон горы, отменяем.
                    if (h > baseHeight + 1) {
                        cir.setReturnValue(Optional.empty());
                        return;
                    }

                    // 3. ПРОВЕРКА НА ЯМУ (Чтобы не было дырок под фундаментом)
                    if (h < baseHeight - 1) {
                        cir.setReturnValue(Optional.empty());
                        return;
                    }
                    
                    // 4. ПРОВЕРКА НА ДЕРЕВЬЯ
                    for (int y = 1; y <= 4; y++) {
                        var state = generator.getBaseColumn(centerX + x, centerZ + z, heightAccessor, context.randomState()).getBlock(h + y);
                        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                            cir.setReturnValue(Optional.empty());
                            return;
                        }
                    }
                }
            }
        }
    }
}
