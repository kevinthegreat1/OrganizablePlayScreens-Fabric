package com.kevinthegreat.organizableplayscreens.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class OrganizablePlayScreensOptionsScreen extends Screen {
    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public OrganizablePlayScreensOptionsScreen(Screen parent) {
        super(Text.translatable("organizableplayscreens:options.title"));
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget().setSpacing(8);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("organizableplayscreens:options.buttonDrag"), button -> client.setScreen(new OrganizablePlayScreensButtonDragScreen(this))).width(96).build());
        adder.add(ButtonWidget.builder(Text.translatable("organizableplayscreens:options.buttonOptions"), button -> client.setScreen(new OrganizablePlayScreensButtonOptionsScreen(this))).width(96).build());
        layout.addBody(gridWidget);

        layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE,button -> close()).width(200).build());

        layout.forEachChild(this::addDrawableChild);
        layout.refreshPositions();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
