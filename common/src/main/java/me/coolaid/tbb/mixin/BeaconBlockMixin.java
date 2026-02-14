package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlock.class)
public class BeaconBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BeaconBlockEntity beacon) {
                if (!level.isClientSide()) {
                    BeamToggleAccess access = (BeamToggleAccess) beacon;
                    if (ToggleBeaconBeams.isHideAllBeamsEnabled()) {
                        access.beamToggle$setForceVisible(!access.beamToggle$isForceVisible());
                    } else {
                        access.beamToggle$setHidden(!access.beamToggle$isHidden());
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}