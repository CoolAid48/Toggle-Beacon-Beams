package me.coolaid.tbb.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private Component statusText;
    private int statusColor;
    private StringWidget titleWidget;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("text.configScreen.title"));
        this.parent = parent;
        this.statusText = Component.empty();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 100;

        int buttonWidth = 130;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int startX = centerX - buttonWidth - (buttonSpacing / 2);

        Button workstationToggle = Button.builder(
                Component.translatable("text.configButton.hideAll",
                        Component.translatable(ConfigManager.get().hideAllBeaconBeams ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    ConfigManager.get().hideAllBeaconBeams = !ConfigManager.get().hideAllBeaconBeams;
                    btn.setMessage(Component.translatable("text.configButton.hideAll",
                            Component.translatable(ConfigManager.get().hideAllBeaconBeams ? "component.configButton.yes" : "component.configButton.no")));
                    ConfigManager.save();
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(workstationToggle);

        // Done button (centered)
        this.addRenderableWidget(Button.builder(
                Component.translatable("text.configButton.done"),
                btn -> {
                    minecraft.setScreen(parent);
                }
        ).bounds(centerX - 60, y + buttonHeight + 4, 120, 20).build());

        // Status text widget
        this.addRenderableWidget(new StringWidget(
                centerX - 100, y, 200, 20, statusText, this.font
        ) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (!statusText.getString().isEmpty()) {
                    graphics.drawCenteredString(font, statusText, getX() + getWidth() / 2, getY(), statusColor);
                }
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draws background and widgets
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(graphics, mouseX, mouseY, delta);

        // Screen title component
        int textWidth = this.font.width(title) + 25; // Include +25 to fit bold formatting
        Component title = Component.translatable("text.configScreen.title").withStyle(ChatFormatting.BOLD);
        titleWidget = new StringWidget(
                (this.width - textWidth) / 2, 10, textWidth, 9, title, this.font
        );
        this.addRenderableWidget(titleWidget);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}