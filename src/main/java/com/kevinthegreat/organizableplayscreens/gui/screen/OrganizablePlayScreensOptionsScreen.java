package com.kevinthegreat.organizableplayscreens.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OrganizablePlayScreensOptionsScreen extends Screen {
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public OrganizablePlayScreensOptionsScreen(Screen parent) {
        super(Component.translatable("organizableplayscreens:options.title"));
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
    }

    @Override
    protected void init() {
        GridLayout gridWidget = new GridLayout().spacing(8);
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        adder.addChild(Button.builder(Component.translatable("organizableplayscreens:options.buttonDrag"), button -> minecraft.setScreen(new OrganizablePlayScreensButtonDragScreen(this))).width(96).build());
        adder.addChild(Button.builder(Component.translatable("organizableplayscreens:options.buttonOptions"), button -> minecraft.setScreen(new OrganizablePlayScreensButtonOptionsScreen(this))).width(96).build());
        layout.addToContents(gridWidget);

        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
