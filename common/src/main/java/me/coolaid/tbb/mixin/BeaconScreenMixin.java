package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.effect.MobEffect;
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
    private AbstractWidget beamToggle$button;
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

    public BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void beamToggle$initBeamToggleWidget(CallbackInfo ci) {
        int beamToggle$buttonX = this.leftPos + 156;
        int beamToggle$buttonY = this.topPos + 72;

        Button beamToggle$clickTarget = this.addRenderableWidget(
                Button.builder(Component.empty(), button -> {
                            if (this.minecraft == null || this.minecraft.gameMode == null) return;
                            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID);
                            this.beamToggle$updateButtonPresentation();
                        })
                        .bounds(beamToggle$buttonX, beamToggle$buttonY, beamToggle$buttonSize, beamToggle$buttonSize)
                        .build()
        );
        beamToggle$clickTarget.setAlpha(0.0F);

        this.beamToggle$button = this.beamToggle$addEffectStyleButton(beamToggle$buttonX, beamToggle$buttonY);

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

    @Unique
    private void beamToggle$updateButtonPresentation() {
        if (this.beamToggle$button == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        if (mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos()) instanceof BeaconBlockEntity beacon) {
            boolean isHidden = ((BeamToggleAccess) beacon).beamToggle$isHidden();
            this.beamToggle$button.setTooltip(Tooltip.create(isHidden ? beamToggle$showText : beamToggle$hideText));
            this.beamToggle$forceUnpressedState();
        }
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
                throw new IllegalStateException("Constructed BeaconPowerButton is not an AbstractWidget");
            }

            if (beamToggle$addBeaconButtonMethod == null) {
                beamToggle$addBeaconButtonMethod = BeaconScreen.class.getDeclaredMethod("addBeaconButton", AbstractWidget.class);
                beamToggle$addBeaconButtonMethod.setAccessible(true);
            }

            beamToggle$addBeaconButtonMethod.invoke(this, widget);
            return widget;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to create and register Beacon effect-style toggle button", e);
        }
    }

}