package me.coolaid.tbb.config;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private Button beamToggle$toggleAllButton;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("text.configScreen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (ToggleBeaconBeams.canUseClientConfigScreen()) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
                if (this.minecraft.player != null) {
                    this.minecraft.player.displayClientMessage(Component.translatable("text.configScreen.onServer"), true);
                }
            }
            return;
        }

        int centerX = this.width / 2;
        int y = this.height / 2 - 42;

        int textWidth = this.font.width(this.title) + 26; // add 26p width offset to fit entire title in bold
        Component title = Component.translatable("text.configScreen.title").withStyle(ChatFormatting.BOLD);
        StringWidget titleWidget = new StringWidget((this.width - textWidth) / 2, 10, textWidth, 9, title, this.font);
        this.addRenderableWidget(titleWidget);

        this.beamToggle$syncConfigToLoadedBeacons();

        int buttonWidth = 190;
        int buttonHeight = 20;
        int buttonSpacing = 8;
        int startX = centerX - (buttonWidth / 2);


        Button beamToggle$enableButton = Button.builder(
                this.beamToggle$getModEnabledButtonText(),
                btn -> {
                    ConfigManager.get().modEnabled = !ConfigManager.get().modEnabled;
                    if (!ConfigManager.get().modEnabled) {
                        ConfigManager.get().hideAllBeaconBeams = false;
                        ToggleBeaconBeams.setAllLoadedBeaconsHidden(false);
                    }

                    btn.setMessage(this.beamToggle$getModEnabledButtonText());
                    this.beamToggle$toggleAllButton.active = ConfigManager.get().modEnabled;
                    this.beamToggle$toggleAllButton.setMessage(this.beamToggle$getToggleAllButtonText());
                    ConfigManager.save();
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(beamToggle$enableButton);

        this.beamToggle$toggleAllButton = Button.builder(
                this.beamToggle$getToggleAllButtonText(),
                btn -> {
                    ConfigManager.get().hideAllBeaconBeams = !ConfigManager.get().hideAllBeaconBeams;
                    ToggleBeaconBeams.setAllLoadedBeaconsHidden(ConfigManager.get().hideAllBeaconBeams);
                    btn.setMessage(this.beamToggle$getToggleAllButtonText());
                    ConfigManager.save();
                }
        ).bounds(startX, y + buttonHeight + buttonSpacing, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(this.beamToggle$toggleAllButton);
        this.beamToggle$toggleAllButton.active = ConfigManager.get().modEnabled;

        // Done button (centered)
        this.addRenderableWidget(Button.builder(
                Component.translatable("text.configButton.done"),
                btn -> {
                    minecraft.setScreen(parent);
                }
        ).bounds(startX, y + (buttonHeight + buttonSpacing) * 2, buttonWidth, buttonHeight).build());

    }

    private Component beamToggle$getModEnabledButtonText() {
        return Component.translatable(ConfigManager.get().modEnabled ? "text.configButton.disableMod" : "text.configButton.enableMod");
    }

    private Component beamToggle$getToggleAllButtonText() {
        if (!ConfigManager.get().modEnabled) {
            return Component.translatable("text.configButton.modDisabled");
        }
        return Component.translatable(ConfigManager.get().hideAllBeaconBeams ? "text.configButton.showAll" : "text.configButton.hideAll");
    }

    private void beamToggle$syncConfigToLoadedBeacons() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        boolean foundBeacon = false;
        for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
            if (be instanceof BeaconBlockEntity beacon) {
                foundBeacon = true;
                if (!((BeamToggleAccess) beacon).beamToggle$isHidden()) {
                    ConfigManager.get().hideAllBeaconBeams = false;
                    return;
                }
            }
        }

        if (foundBeacon) {
            ConfigManager.get().hideAllBeaconBeams = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draws background and widgets
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(graphics, mouseX, mouseY, delta);

    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}