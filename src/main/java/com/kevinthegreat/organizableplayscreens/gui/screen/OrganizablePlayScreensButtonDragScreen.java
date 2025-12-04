package com.kevinthegreat.organizableplayscreens.gui.screen;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.option.BothSuppliableIntSliderCallbacks;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizablePlayScreensButtonDragScreen extends Screen {
    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
    private static final int HEADER_HEIGHT = 33;
    private static final int FOOTER_HEIGHT = 60;

    private final Screen parent;
    private final OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, HEADER_HEIGHT, FOOTER_HEIGHT);
    private final List<ButtonWidget> draggableButtons = new ArrayList<>();

    private double mouseClickRelativeX;
    private double mouseClickRelativeY;

    public OrganizablePlayScreensButtonDragScreen(Screen parent) {
        super(Text.translatable("organizableplayscreens:options.buttonDrag"));
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
    }

    @Override
    protected void init() {
        layout.addHeader(title, textRenderer);

        draggableButtons.add(addDrawableChild(ButtonWidget.builder(Text.of("←"), button -> {}).dimensions(options.backButtonX.getValue(), options.backButtonY.getValue(), 20, 20).build()));
        draggableButtons.add(addDrawableChild(ButtonWidget.builder(Text.of("←+"), button -> {}).dimensions(options.moveEntryBackButtonX.getValue(), options.moveEntryBackButtonY.getValue(), 20, 20).tooltip(OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP).build()));
        draggableButtons.add(addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {}).dimensions(options.getValue(options.newFolderButtonX), options.newFolderButtonY.getValue(), 20, 20).build()));
        draggableButtons.add(addDrawableChild(new LegacyTexturedButtonWidget(options.getValue(options.optionsButtonX), options.optionsButtonY.getValue(), 20, 20, 0, 0, 20, OrganizablePlayScreens.OPTIONS_BUTTON_TEXTURE, 32, 64, button -> {}, Text.translatable("organizableplayscreens:options.optionsButton"))));

        layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).width(200).build());

        layout.forEachChild(this::addDrawableChild);
        layout.refreshPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        assert client != null;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE, 0, layout.getHeaderHeight(), width, height - layout.getFooterHeight(), width, height - layout.getHeaderHeight() - layout.getFooterHeight(), 32, 32);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 0, layout.getHeaderHeight() - 2, 0, 0, width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 0, height - layout.getFooterHeight(), 0, 0, width, 2, 32, 2);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Optional<Element> element = hoveredElement(click.x(), click.y());
        if (element.isPresent() && element.get() instanceof ButtonWidget button && draggableButtons.contains(button)) {
            mouseClickRelativeX = click.x() - button.getX();
            mouseClickRelativeY = click.y() - button.getY();
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (getFocused() instanceof ButtonWidget button && draggableButtons.contains(button)) {
            int x = (int) Math.clamp(click.x() - mouseClickRelativeX, 0, width - button.getWidth());
            int y = (int) Math.clamp(click.y() - mouseClickRelativeY, 0, height - button.getHeight());

            button.setPosition(x, y);
            if (button == draggableButtons.get(0)) {
                setOption(options.backButtonX, x);
                setOption(options.backButtonY, y);
            } else if (button == draggableButtons.get(1)) {
                setOption(options.moveEntryBackButtonX, x);
                setOption(options.moveEntryBackButtonY, y);
            } else if (button == draggableButtons.get(2)) {
                setOption(options.newFolderButtonX, x);
                setOption(options.newFolderButtonY, y);
            } else if (button == draggableButtons.get(3)) {
                setOption(options.optionsButtonX, x);
                setOption(options.optionsButtonY, y);
            }
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    private void setOption(SimpleOption<Integer> option, int value) {
        if (option.getCallbacks() instanceof BothSuppliableIntSliderCallbacks bothSuppliableCallbacks) {
            option.setValue(bothSuppliableCallbacks.displayValueParser().apply(String.valueOf(value)));
        } else {
            option.setValue(value);
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void removed() {
        options.save();
    }
}
