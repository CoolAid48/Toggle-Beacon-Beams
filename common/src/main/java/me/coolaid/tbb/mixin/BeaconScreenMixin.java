package me.coolaid.tbb.mixin;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.ToggleBeaconBeamsClient;
import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.config.LocalToggleStore;
import me.coolaid.tbb.network.ServerPresenceTracker;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.Level;
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
    @Unique
    private boolean beamToggle$cachedHidden;
    @Unique
    private boolean beamToggle$hasCachedHidden;

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

        Identifier texture = this.beamToggle$cachedHidden ? beamToggle$showBeamTexture : beamToggle$hideBeamTexture;
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

        var level = this.minecraft.level;
        BlockPos pos = this.beamToggle$resolveBeaconPos();
        if (level == null || pos == null) return;

        boolean serverPresent = ServerPresenceTracker.isServerPresent();
        boolean serverHidden = this.beamToggle$getServerHidden(serverPresent, level, pos);
        boolean hideAll = ConfigManager.get().hideAllBeaconBeams;
        boolean defaultHidden = serverHidden || hideAll;
        boolean useLocalOverride = hideAll || !serverPresent;
        ResourceKey<Level> dimension = level.dimension();
        String worldIdentifier = null;
        boolean currentHidden = serverHidden;
        if (useLocalOverride) {
            worldIdentifier = ToggleBeaconBeamsClient.getWorldUniqueIdentifier(this.minecraft);
            currentHidden = LocalToggleStore.isHidden(worldIdentifier, dimension, pos, defaultHidden);
        }
        boolean targetHidden = !currentHidden;

        if (serverPresent && targetHidden != serverHidden) {
            int buttonId = ToggleBeaconBeamsClient.canUseClientConfigScreen()
                ? ToggleBeaconBeams.TOGGLE_BEAM_BUTTON_ID
                : (targetHidden
                    ? ToggleBeaconBeams.HIDE_BEAM_BUTTON_ID
                    : ToggleBeaconBeams.SHOW_BEAM_BUTTON_ID);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }

        if (useLocalOverride) {
            LocalToggleStore.setHidden(worldIdentifier, dimension, pos, targetHidden, defaultHidden);
        }

        var state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        this.beamToggle$updateButtonPresentation();
    }

    @Unique
    private void beamToggle$updateButtonPresentation() {
        if (this.beamToggle$button == null) return;

        boolean isHidden = this.beamToggle$isCurrentBeaconHidden();
        if (this.beamToggle$hasCachedHidden && this.beamToggle$cachedHidden == isHidden) return;

        this.beamToggle$cachedHidden = isHidden;
        this.beamToggle$hasCachedHidden = true;
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
        var level = mc.level;
        if (level == null) return false;

        BlockPos pos = this.beamToggle$resolveBeaconPos();
        if (pos == null) return false;

        boolean serverPresent = ServerPresenceTracker.isServerPresent();
        boolean serverHidden = this.beamToggle$getServerHidden(serverPresent, level, pos);
        boolean hideAll = ConfigManager.get().hideAllBeaconBeams;
        boolean defaultHidden = serverHidden || hideAll;
        if (!hideAll && serverPresent) {
            return serverHidden;
        }

        String worldIdentifier = ToggleBeaconBeamsClient.getWorldUniqueIdentifier(mc);
        return LocalToggleStore.isHidden(worldIdentifier, level.dimension(), pos, defaultHidden);
    }

    @Unique
    private boolean beamToggle$getServerHidden(boolean serverPresent, Level level, BlockPos pos) {
        if (serverPresent && level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon) {
            return ((BeamToggleAccess) beacon).beamToggle$isHidden();
        }
        return false;
    }

    @Unique
    private BlockPos beamToggle$resolveBeaconPos() {
        Minecraft mc = Minecraft.getInstance();
        if (this.beamToggle$beaconPos != null) return this.beamToggle$beaconPos;
        if (mc.hitResult instanceof BlockHitResult bhr && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            this.beamToggle$beaconPos = bhr.getBlockPos().immutable();
            return this.beamToggle$beaconPos;
        }
        return null;
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
