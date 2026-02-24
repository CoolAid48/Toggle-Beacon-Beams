package me.coolaid.tbb.mixin;

import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void beamToggle$load(ValueInput input, CallbackInfo ci) {
        this.beamToggle$isHidden = input.getBooleanOr("ltbl_hidden", false);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void beamToggle$save(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("ltbl_hidden", this.beamToggle$isHidden);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void beamToggle$addToUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag != null) {
            tag.putBoolean("ltbl_hidden", this.beamToggle$isHidden);
        }
    }

    @Inject(method = "getUpdatePacket*", at = @At("RETURN"), cancellable = true)
    private void beamToggle$createUpdatePacket(CallbackInfoReturnable<ClientboundBlockEntityDataPacket> cir) {
        cir.setReturnValue(ClientboundBlockEntityDataPacket.create((BeaconBlockEntity)(Object)this));
    }

    @Inject(method = "getBeamSections", at = @At("HEAD"), cancellable = true)
    private void beamToggle$hideBeam(CallbackInfoReturnable<List<BeaconBeamOwner.Section>> cir) {
        if (ConfigManager.get().modEnabled && this.beamToggle$isHidden) {
            cir.setReturnValue(List.of());
        }
    }
}