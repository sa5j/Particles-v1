package com.example.particlecustomizer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ParticleCustomizerScreen extends Screen {
    private static final int BG_DARK = 0xFF121212;
    private static final int PANEL_BG = 0xFF1E1E1E;
    private static final int BORDER_GRAY = 0xFF7A7A7A;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GRAY = 0xFFB8B8B8;

    private final Screen parent;
    private EditBox sourceBox;
    private EditBox targetBox;
    private EditBox multiplierBox;
    private String statusMessage = "Edit a particle ID (e.g. minecraft:crit) and save.";

    public ParticleCustomizerScreen(Screen parent) {
        super(Component.literal("PARTICLE CUSTOMIZER"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(340, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int top = 36;

        ParticleConfig config = ParticleCustomizerClient.getConfig();
        ParticleConfig.Rule defaultRule = config.getOrCreateRule("minecraft:crit");

        this.sourceBox = new EditBox(this.font, left + 12, top + 24, panelWidth - 24, 18, Component.literal("Source Particle"));
        this.sourceBox.setMaxLength(128);
        this.sourceBox.setValue("minecraft:crit");
        this.sourceBox.setResponder(val -> loadRuleIntoFields(val.trim()));
        addRenderableWidget(this.sourceBox);

        this.targetBox = new EditBox(this.font, left + 12, top + 66, panelWidth - 24, 18, Component.literal("Replacement Particle"));
        this.targetBox.setMaxLength(128);
        this.targetBox.setValue(defaultRule.replacementId);
        addRenderableWidget(this.targetBox);

        this.multiplierBox = new EditBox(this.font, left + 12, top + 108, panelWidth - 24, 18, Component.literal("Count Multiplier"));
        this.multiplierBox.setMaxLength(16);
        this.multiplierBox.setValue(String.valueOf(defaultRule.multiplier));
        addRenderableWidget(this.multiplierBox);

        int btnWidth = (panelWidth - 32) / 2;
        addRenderableWidget(Button.builder(Component.literal("SAVE RULE"), btn -> saveCurrentRule())
                .bounds(left + 12, top + 138, btnWidth, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("DONE"), btn -> onClose())
                .bounds(left + 20 + btnWidth, top + 138, btnWidth, 20)
                .build());
    }

    private void loadRuleIntoFields(String sourceId) {
        if (sourceId.isEmpty()) return;
        ParticleConfig.Rule rule = ParticleCustomizerClient.getConfig().rules.get(sourceId);
        if (rule != null) {
            this.targetBox.setValue(rule.replacementId != null ? rule.replacementId : "");
            this.multiplierBox.setValue(String.valueOf(rule.multiplier));
        } else {
            this.targetBox.setValue("");
            this.multiplierBox.setValue("1.0");
        }
    }

    private void saveCurrentRule() {
        String source = this.sourceBox.getValue().trim();
        if (source.isEmpty()) {
            this.statusMessage = "Source particle ID cannot be empty.";
            return;
        }

        float mult;
        try {
            mult = Float.parseFloat(this.multiplierBox.getValue().trim());
            if (mult < 0.0f || Float.isNaN(mult) || Float.isInfinite(mult)) {
                this.statusMessage = "Multiplier must be 0.0 or higher.";
                return;
            }
        } catch (NumberFormatException e) {
            this.statusMessage = "Invalid number for multiplier.";
            return;
        }

        ParticleConfig config = ParticleCustomizerClient.getConfig();
        ParticleConfig.Rule rule = config.getOrCreateRule(source);
        rule.replacementId = this.targetBox.getValue().trim();
        rule.multiplier = mult;
        config.save();

        this.statusMessage = "Saved " + source + " -> " + mult + "x";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, BG_DARK);

        int panelWidth = Math.min(340, this.width - 24);
        int panelHeight = 196;
        int left = (this.width - panelWidth) / 2;
        int top = 36;

        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, BORDER_GRAY);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, PANEL_BG);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 6, TEXT_WHITE);
        graphics.drawString(this.font, "SOURCE PARTICLE ID", left + 12, top + 14, TEXT_GRAY);
        graphics.drawString(this.font, "REPLACEMENT ID (BLANK = KEEP)", left + 12, top + 56, TEXT_GRAY);
        graphics.drawString(this.font, "COUNT MULTIPLIER (E.G. 0.5, 2.0)", left + 12, top + 98, TEXT_GRAY);
        graphics.drawString(this.font, this.statusMessage, left + 12, top + 168, TEXT_WHITE);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
