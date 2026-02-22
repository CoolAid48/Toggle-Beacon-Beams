package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {

    @Shadow
    protected abstract <T extends AbstractWidget> T addBeaconButton(T beaconButton);
    @Unique
    private static final int beamToggle$buttonSize = 22;
    @Unique
    private Button beamToggle$button;
    @Unique
    private static final Component beamToggle$hideText = Component.literal("Hide Beam");
    @Unique
    private static final Component beamToggle$showText = Component.literal("Show Beam");

    public BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void beamToggle$initBeamToggleWidget(CallbackInfo ci) {
        int beamToggle$buttonX = this.leftPos + 190;
        int beamToggle$buttonY = this.topPos + 107;

        this.beamToggle$button = this.addBeaconButton(
                Button.builder(Component.empty(), button -> {
                            if (this.minecraft.gameMode == null) return;
                            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID);
                            this.beamToggle$updateButtonPresentation();
                        })
                        .bounds(beamToggle$buttonX, beamToggle$buttonY, beamToggle$buttonSize, beamToggle$buttonSize)
                        .build()
        );

        this.beamToggle$updateButtonPresentation();
        this.beamToggle$hideVanillaCancelButton(beamToggle$buttonX, beamToggle$buttonY);
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void beamToggle$refreshWidgetState(CallbackInfo ci) {
        this.beamToggle$hideVanillaCancelButton(this.leftPos + 190, this.topPos + 107);
        this.beamToggle$updateButtonPresentation();
    }

    @Unique
    private void beamToggle$hideVanillaCancelButton(int buttonX, int buttonY) {
        for (Object child : this.children()) {
            if (child instanceof AbstractWidget widget && widget != this.beamToggle$button
                    && widget.getX() == buttonX && widget.getY() == buttonY
                    && widget.getWidth() == beamToggle$buttonSize && widget.getHeight() == beamToggle$buttonSize) {
                widget.visible = false;
                widget.active = false;
            }
        }
    }

    @Unique
    private void beamToggle$updateButtonPresentation() {
        if (this.beamToggle$button == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        if (mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos()) instanceof BeaconBlockEntity beacon) {
            boolean isHidden = ((BeamToggleAccess) beacon).beamToggle$isHidden();

            this.beamToggle$button.setMessage(Component.literal(isHidden ? "✔" : "✖"));
            this.beamToggle$button.setTooltip(Tooltip.create(isHidden ? beamToggle$showText : beamToggle$hideText));
        }
    }
}