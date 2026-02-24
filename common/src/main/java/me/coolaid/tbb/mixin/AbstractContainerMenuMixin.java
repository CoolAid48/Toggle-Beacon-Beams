package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void tbb$handleBeaconToggleButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id != ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID &&
                id != ToggleBeaconBeams.HIDE_BEAM_BUTTON_ID &&
                id != ToggleBeaconBeams.SHOW_BEAM_BUTTON_ID) return;
        if (player.level().isClientSide()) {
            cir.setReturnValue(true);
            return;
        }

        if ((Object) this instanceof BeaconMenu beaconMenu) {
            ((BeaconMenuAccessor) beaconMenu).beamToggle$getAccess().execute((level, pos) -> {
                if (level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon) {
                    BeamToggleAccess toggleAccess = (BeamToggleAccess) beacon;
                    if (id == ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID) {
                        toggleAccess.beamToggle$setHidden(!toggleAccess.beamToggle$isHidden());
                    } else if (id == ToggleBeaconBeams.HIDE_BEAM_BUTTON_ID) {
                        toggleAccess.beamToggle$setHidden(true);
                    } else if (id == ToggleBeaconBeams.SHOW_BEAM_BUTTON_ID) {
                        toggleAccess.beamToggle$setHidden(false);
                    }
                    level.sendBlockUpdated(pos, beacon.getBlockState(), beacon.getBlockState(), 3);
                }
            });
            cir.setReturnValue(true);
        }
    }
}