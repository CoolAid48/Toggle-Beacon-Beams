package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {

    @Unique
    private static final int beamToggle$buttonSize = 22;
    @Unique
    private static final int beamToggle$buttonOffsetX = 156;
    @Unique
    private static final int beamToggle$buttonOffsetY = 72;
    @Unique
    private static final int beamToggle$iconInset = 2;
    @Unique
    private static final Identifier beamToggle$buttonTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/beacon/button.png");
    @Unique
    private static final Identifier beamToggle$buttonHighlightedTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/beacon/button_highlighted.png");
    @Unique
    private static final Identifier beamToggle$buttonDisabledTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/beacon/button_disabled.png");
    @Unique
    private static final Identifier beamToggle$hideBeamTexture = Identifier.fromNamespaceAndPath("tbb", "textures/gui/sprites/beacon/hide_beam.png");
    @Unique
    private static final Identifier beamToggle$showBeamTexture = Identifier.fromNamespaceAndPath("tbb", "textures/gui/sprites/beacon/show_beam.png");

    @Unique
    private static final Component beamToggle$hideText = Component.translatable("component.beamtoggle.hide");
    @Unique
    private static final Component beamToggle$showText = Component.translatable("component.beamtoggle.show");

    @Unique
    private static Method beamToggle$setSelectedMethod;

    @Unique
    private AbstractWidget beamToggle$button;
    @Unique
    private BlockPos beamToggle$beaconPos;

    public BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void beamToggle$initBeamToggleWidget(CallbackInfo ci) {
        if (!ConfigManager.get().modEnabled) return;

        this.beamToggle$captureBeaconPosFromCrosshair();

        int buttonX = this.leftPos + beamToggle$buttonOffsetX;
        int buttonY = this.topPos + beamToggle$buttonOffsetY;

        this.beamToggle$button = this.beamToggle$addToggleButton(buttonX, buttonY);
        this.beamToggle$updateButtonPresentation();
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void beamToggle$refreshWidgetState(CallbackInfo ci) {
        this.beamToggle$updateButtonPresentation();
    }

    @Inject(method = "updateButtons", at = @At("TAIL"))
    private void beamToggle$keepEffectStyleUnpressed(CallbackInfo ci) {
        if (this.beamToggle$button != null) {
            // Active when beacon has loaded (levels > 0 means it's powered/loaded)
            this.beamToggle$button.active = this.menu.getLevels() > 0;
            this.beamToggle$forceUnpressedState();
        }
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void beamToggle$renderCustomSprite(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (this.beamToggle$button == null) return;

        boolean active = this.beamToggle$button.active;
        boolean hovered = this.beamToggle$button.isHoveredOrFocused();

        // Choose button background based on state
        Identifier buttonTexture;
        if (!active) {
            buttonTexture = beamToggle$buttonDisabledTexture; // Gray when inactive
        } else if (hovered) {
            buttonTexture = beamToggle$buttonHighlightedTexture;
        } else {
            buttonTexture = beamToggle$buttonTexture;
        }

        Identifier texture = this.beamToggle$isCurrentBeaconHidden() ? beamToggle$showBeamTexture : beamToggle$hideBeamTexture;
        int x = this.beamToggle$button.getX();
        int y = this.beamToggle$button.getY();

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, buttonTexture, x, y, 0.0F, 0.0F, beamToggle$buttonSize, beamToggle$buttonSize, beamToggle$buttonSize, beamToggle$buttonSize);

        int iconX = x + beamToggle$iconInset;
        int iconY = y + beamToggle$iconInset;
        int iconSize = beamToggle$buttonSize - (beamToggle$iconInset * 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
    }

    @Unique
    private Button beamToggle$addToggleButton(int x, int y) {
        Button button = Button.builder(Component.empty(), press -> this.beamToggle$onPressed())
                .bounds(x, y, beamToggle$buttonSize, beamToggle$buttonSize)
                .build();
        button.setAlpha(0.0F);
        return this.addRenderableWidget(button);
    }

    @Unique
    private void beamToggle$onPressed() {
        if (this.minecraft.gameMode == null) return;

        int buttonId;
        if (ToggleBeaconBeams.canUseClientConfigScreen()) {
            buttonId = ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID;
        } else {
            buttonId = this.beamToggle$isCurrentBeaconHidden()
                    ? ToggleBeaconBeams.SHOW_BEAM_BUTTON_ID
                    : ToggleBeaconBeams.HIDE_BEAM_BUTTON_ID;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        this.beamToggle$updateButtonPresentation();
    }

    @Unique
    private void beamToggle$updateButtonPresentation() {
        if (this.beamToggle$button == null) return;

        boolean isHidden = this.beamToggle$isCurrentBeaconHidden();
        this.beamToggle$button.setTooltip(Tooltip.create(isHidden ? beamToggle$showText : beamToggle$hideText));
    }

    @Unique
    private void beamToggle$captureBeaconPosFromCrosshair() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult instanceof BlockHitResult blockHitResult && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            this.beamToggle$beaconPos = blockHitResult.getBlockPos().immutable();
        }
    }

    @Unique
    private boolean beamToggle$isCurrentBeaconHidden() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        if (this.beamToggle$beaconPos != null && mc.level.getBlockEntity(this.beamToggle$beaconPos) instanceof BeaconBlockEntity beacon) {
            return ((BeamToggleAccess) beacon).beamToggle$isHidden();
        }

        if (mc.hitResult instanceof BlockHitResult blockHitResult
                && mc.hitResult.getType() == HitResult.Type.BLOCK
                && mc.level.getBlockEntity(blockHitResult.getBlockPos()) instanceof BeaconBlockEntity beaconFromHit) {
            this.beamToggle$beaconPos = blockHitResult.getBlockPos().immutable();
            return ((BeamToggleAccess) beaconFromHit).beamToggle$isHidden();
        }

        return false;
    }

    @Unique
    private void beamToggle$forceUnpressedState() {
        if (this.beamToggle$button == null) return;

        try {
            if (beamToggle$setSelectedMethod == null) {
                beamToggle$setSelectedMethod = this.beamToggle$button.getClass().getMethod("setSelected", boolean.class);
            }
            beamToggle$setSelectedMethod.invoke(this.beamToggle$button, false);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}