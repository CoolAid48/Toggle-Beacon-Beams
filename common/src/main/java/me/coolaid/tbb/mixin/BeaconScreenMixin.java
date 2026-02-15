package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<BeaconMenu> {

    @Unique
    private static final int beamToggle$widgetSize = 14;

    @Unique
    private int beamToggle$widgetX;

    @Unique
    private int beamToggle$widgetY;

    @Unique
    private boolean beamToggle$wasLeftDown;

    public BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void beamToggle$initBeamToggleWidget(CallbackInfo ci) {
        this.beamToggle$widgetX = this.leftPos + this.imageWidth - beamToggle$widgetSize - 6;
        this.beamToggle$widgetY = this.topPos + 6;
        this.beamToggle$wasLeftDown = false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void beamToggle$renderBeamToggleWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        boolean hovered = mouseX >= this.beamToggle$widgetX && mouseX < this.beamToggle$widgetX + beamToggle$widgetSize
                && mouseY >= this.beamToggle$widgetY && mouseY < this.beamToggle$widgetY + beamToggle$widgetSize;

        int background = hovered ? 0xE0666666 : 0xC0333333;
        guiGraphics.fill(this.beamToggle$widgetX, this.beamToggle$widgetY, this.beamToggle$widgetX + beamToggle$widgetSize, this.beamToggle$widgetY + beamToggle$widgetSize, background);
        guiGraphics.fill(this.beamToggle$widgetX + 1, this.beamToggle$widgetY + 1, this.beamToggle$widgetX + beamToggle$widgetSize - 1, this.beamToggle$widgetY + beamToggle$widgetSize - 1, 0xFF111111);

        Component icon = Component.literal("✦");
        int textWidth = this.font.width(icon);
        int textX = this.beamToggle$widgetX + (beamToggle$widgetSize - textWidth) / 2;
        int textY = this.beamToggle$widgetY + 3;
        guiGraphics.drawString(this.font, icon, textX, textY, hovered ? 0xFF55FF55 : 0xFFCCCCCC, false);

        if (this.minecraft.gameMode == null) {
            return;
        }

        boolean leftDown = this.minecraft.mouseHandler.isLeftPressed();
        if (leftDown && !this.beamToggle$wasLeftDown && hovered) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID);
        }
        this.beamToggle$wasLeftDown = leftDown;
    }
}