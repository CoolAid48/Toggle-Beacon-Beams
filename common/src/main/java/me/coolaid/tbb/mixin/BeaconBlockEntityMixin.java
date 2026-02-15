package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity implements BeamToggleAccess {

    @Unique
    private boolean beamToggle$isHidden = false;
    @Unique
    private boolean beamToggle$isForceVisible = false;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean beamToggle$isHidden() {
        return this.beamToggle$isHidden;
    }

    @Override
    public void beamToggle$setHidden(boolean hidden) {
        this.beamToggle$isHidden = hidden;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public boolean beamToggle$isForceVisible() {
        return this.beamToggle$isForceVisible;
    }

    @Override
    public void beamToggle$setForceVisible(boolean forceVisible) {
        this.beamToggle$isForceVisible = forceVisible;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void loadHiddenState(ValueInput input, CallbackInfo ci) {
        this.beamToggle$isHidden = input.getBooleanOr("ltbl_hidden", false);
        this.beamToggle$isForceVisible = input.getBooleanOr("ltbl_force_visible", false);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void saveHiddenState(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("ltbl_hidden", this.beamToggle$isHidden);
        output.putBoolean("ltbl_force_visible", this.beamToggle$isForceVisible);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void addToUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("ltbl_hidden", this.beamToggle$isHidden);
        cir.getReturnValue().putBoolean("ltbl_force_visible", this.beamToggle$isForceVisible);
    }

    @Inject(method = "getUpdatePacket", at = @At("RETURN"), cancellable = true)
    private void createUpdatePacket(CallbackInfoReturnable<ClientboundBlockEntityDataPacket> cir) {
        cir.setReturnValue(ClientboundBlockEntityDataPacket.create((BeaconBlockEntity)(Object)this));
    }

    @Inject(method = "getBeamSections", at = @At("HEAD"), cancellable = true)
    private void hideBeamSectionsWhenHidden(CallbackInfoReturnable<List<BeaconBeamOwner.Section>> cir) {
        boolean globalHidden = ToggleBeaconBeams.isHideAllBeamsEnabled();
        if (!globalHidden && this.beamToggle$isForceVisible) {
            this.beamToggle$setForceVisible(false);
        }

        boolean hideBecauseGlobalMode = globalHidden && !this.beamToggle$isForceVisible;
        if (this.beamToggle$isHidden || hideBecauseGlobalMode) {
            cir.setReturnValue(List.of());
        }
    }
}