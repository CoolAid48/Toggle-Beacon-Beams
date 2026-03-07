package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    private static final int beamToggle$hoverOverlayColor = 0x339293B3;
    @Unique
    private static final int beamToggle$iconInset = 2;
    @Unique
    private static final int beamToggle$iconMaskColor = 0xFF343434;

    @Unique
    private static final Identifier beamToggle$hideBeamSprite = Identifier.fromNamespaceAndPath("tbb", "beacon/hide_beam");
    @Unique
    private static final Identifier beamToggle$showBeamSprite = Identifier.fromNamespaceAndPath("tbb", "beacon/show_beam");

    @Unique
    private static final Component beamToggle$hideText = Component.translatable("component.beamtoggle.hide");
    @Unique
    private static final Component beamToggle$showText = Component.translatable("component.beamtoggle.show");

    @Unique
    private static Method beamToggle$addBeaconButtonMethod;
    @Unique
    private static Constructor<?> beamToggle$powerButtonConstructor;
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

        this.beamToggle$addInvisibleClickTarget(buttonX, buttonY);
        this.beamToggle$button = this.beamToggle$addEffectStyleButton(buttonX, buttonY);
        this.beamToggle$updateButtonPresentation();
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void beamToggle$refreshWidgetState(CallbackInfo ci) {
        this.beamToggle$updateButtonPresentation();
    }

    @Inject(method = "updateButtons", at = @At("TAIL"))
    private void beamToggle$keepEffectStyleUnpressed(CallbackInfo ci) {
        this.beamToggle$forceUnpressedState();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void beamToggle$renderCustomSprite(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.beamToggle$button == null) return;

        Identifier sprite = this.beamToggle$isCurrentBeaconHidden() ? beamToggle$showBeamSprite : beamToggle$hideBeamSprite;
        int x = this.beamToggle$button.getX();
        int y = this.beamToggle$button.getY();

        int iconX1 = x + beamToggle$iconInset;
        int iconY1 = y + beamToggle$iconInset;
        int iconX2 = x + beamToggle$buttonSize - beamToggle$iconInset;
        int iconY2 = y + beamToggle$buttonSize - beamToggle$iconInset;

        guiGraphics.fill(iconX1, iconY1, iconX2, iconY2, beamToggle$iconMaskColor);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, beamToggle$buttonSize, beamToggle$buttonSize);

        if (this.beamToggle$button.isHoveredOrFocused()) {
            guiGraphics.fill(x, y, x + beamToggle$buttonSize, y + beamToggle$buttonSize, beamToggle$hoverOverlayColor);
        }
    }

    @Unique
    private void beamToggle$addInvisibleClickTarget(int x, int y) {
        Button clickTarget = this.addRenderableWidget(
                Button.builder(Component.empty(), button -> this.beamToggle$onPressed())
                        .bounds(x, y, beamToggle$buttonSize, beamToggle$buttonSize)
                        .build()
        );
        clickTarget.setAlpha(0.0F);
    }

    @Unique
    private void beamToggle$onPressed() {
        if (this.minecraft == null || this.minecraft.gameMode == null) return;

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
        this.beamToggle$forceUnpressedState();
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

    @Unique
    private AbstractWidget beamToggle$addEffectStyleButton(int x, int y) {
        try {
            if (beamToggle$powerButtonConstructor == null) {
                Class<?> powerButtonClass = Class.forName("net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton");
                beamToggle$powerButtonConstructor = powerButtonClass.getDeclaredConstructor(BeaconScreen.class, int.class, int.class, Holder.class, boolean.class, int.class);
                beamToggle$powerButtonConstructor.setAccessible(true);
            }

            @SuppressWarnings("unchecked")
            Holder<MobEffect> holder = (Holder<MobEffect>) BeaconBlockEntity.BEACON_EFFECTS.getFirst().getFirst();
            Object powerButton = beamToggle$powerButtonConstructor.newInstance(this, x, y, holder, true, 0);
            if (!(powerButton instanceof AbstractWidget widget)) {
                throw new IllegalStateException("BeaconPowerButton isn't an AbstractWidget");
            }

            if (beamToggle$addBeaconButtonMethod == null) {
                beamToggle$addBeaconButtonMethod = BeaconScreen.class.getDeclaredMethod("addBeaconButton", AbstractWidget.class);
                beamToggle$addBeaconButtonMethod.setAccessible(true);
            }

            beamToggle$addBeaconButtonMethod.invoke(this, widget);
            return widget;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Couldn't create Beacon toggle button", e);
        }
    }
}