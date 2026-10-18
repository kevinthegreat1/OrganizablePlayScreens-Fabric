package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.gui.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.mixin.client.gametest.gui.ScreenAccessor;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.joml.Vector2d;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class OrganizablePlayScreensClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Singleplayer
        // Create a new world and navigate to the select world screen
        //noinspection EmptyTryBlock
        try (TestSingleplayerContext _ = context.worldBuilder().create()) {}
        context.waitForScreen(TitleScreen.class);
        context.clickScreenButton("menu.singleplayer");
        // Create a new folder and assert the select world screen root
        createNewFolder(context);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root").save());

        // Move the world into the folder
        clickListWidgetEntry(context, WorldSelectionList.WorldListEntry.class, 33);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        // Open the folder
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 24);
        // Create a new folder, section, and separator inside the folder and assert the select world screen folder
        createNewFolder(context);
        createNewEntry(context, "organizableplayscreens:entry.section");
        createNewEntry(context, "organizableplayscreens:entry.separator");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-folder").save());

        // Move the section above the folder and assert the select world screen folder reordered
        clickListWidgetEntry(context, SingleplayerSectionEntry.class, 0);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-folder-reordered").save());

        // Navigate back to the title screen and back to the folder
        context.clickScreenButton("gui.back");
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.singleplayer");
        context.waitTick();
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 33);
        context.clickScreenButton("organizableplayscreens:folder.openFolder");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-folder-reopened").save());

        // Move entries out of the current folder and assert the select world screen root again
        clickListWidgetEntry(context, SingleplayerSectionEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, SingleplayerSeparatorEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, WorldSelectionList.WorldListEntry.class, 33);
        clickScreenButton(context, "←+");
        context.clickScreenButton("gui.back");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root-move-entries-back").save());

        // Move entries back into the folder and assert the select world screen folder reopened again
        clickListWidgetEntry(context, SingleplayerSectionEntry.class, 33);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 1, 33);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        clickListWidgetEntry(context, SingleplayerSeparatorEntry.class, 33);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        clickListWidgetEntry(context, WorldSelectionList.WorldListEntry.class, 33);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 24);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-folder-reopened").save());

        // Delete the folder and assert the select world screen root again
        context.clickScreenButton("gui.back");
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 33);
        context.clickScreenButton("selectWorld.delete");
        context.clickScreenButton("selectWorld.deleteButton");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root-delete-folder").save());

        testSingleplayerCustomIcon(context);

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);

        // Multiplayer
        // Navigate to the multiplayer screen, skip the multiplayer warning screen, and add a new server
        context.clickScreenButton("menu.multiplayer");
        context.clickScreenButton("gui.proceed");
        context.clickScreenButton("selectServer.add");
        context.clickScreenButton("gui.done");
        // Create a new folder and assert the multiplayer screen root
        createNewFolder(context);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root").save());

        // Move the server into the folder
        clickListWidgetEntry(context, ServerSelectionList.OnlineServerEntry.class, 33);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        // Open the folder
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 24);
        // Create a new folder, section, and separator inside the folder and assert the multiplayer screen folder
        createNewFolder(context);
        createNewEntry(context, "organizableplayscreens:entry.section");
        createNewEntry(context, "organizableplayscreens:entry.separator");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-folder").save());

        // Move the section above the folder and assert the multiplayer screen folder reordered
        clickListWidgetEntry(context, MultiplayerSectionEntry.class, 0);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-folder-reordered").save());

        // Navigate back to the title screen and back to the folder
        context.clickScreenButton("gui.back");
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.multiplayer");
        context.clickScreenButton("gui.proceed");
        context.waitTick();
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 33);
        context.clickScreenButton("organizableplayscreens:folder.openFolder");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-folder-reopened").save());

        // Move entries out of the current folder and assert the multiplayer screen root again
        clickListWidgetEntry(context, ServerSelectionList.OnlineServerEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, MultiplayerSectionEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 33);
        clickScreenButton(context, "←+");
        clickListWidgetEntry(context, MultiplayerSeparatorEntry.class, 33);
        clickScreenButton(context, "←+");
        context.clickScreenButton("gui.back");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root-move-entries-back").save());

        // Move entries back into the folder and assert the multiplayer screen folder reopened again
        clickListWidgetEntry(context, ServerSelectionList.OnlineServerEntry.class, 33);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        clickListWidgetEntry(context, MultiplayerSectionEntry.class, 33);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 1, 33);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        clickListWidgetEntry(context, MultiplayerSeparatorEntry.class, 33);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 24);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-folder-reopened").save());

        // Delete the folder and assert the multiplayer screen root again
        context.clickScreenButton("gui.back");
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 33);
        context.clickScreenButton("selectServer.delete");
        context.clickScreenButton("selectServer.deleteButton");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root-delete-folder").save());

        testMultiplayerCustomIcon(context);

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);
    }

    private void testSingleplayerCustomIcon(ClientGameTestContext context) {
        // Add a green custom icon
        context.runOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            SingleplayerFolderEntry entry = getListWidgetEntry(listWidget, SingleplayerFolderEntry.class, 0);
            NativeImage image = new NativeImage(64, 64, true);
            image.fillRect(0, 0, 64, 64, 0xFF00FF00);
            entry.getCustomIconTexture().upload(image);
        });
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root-custom-icon").save());
        // Test serialization
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.singleplayer");
        context.waitTick();
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root-custom-icon").save());
        // Delete the custom icon
        clickListWidgetEntry(context, SingleplayerFolderEntry.class, 33);
        context.clickScreenButton("selectWorld.edit");
        context.clickScreenButton("organizableplayscreens:entry.icon.delete");
        context.waitForScreen(ConfirmScreen.class);
        context.clickScreenButton("organizableplayscreens:entry.icon.delete");
        context.clickScreenButton("gui.done");
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.singleplayer");
        context.waitTick();
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root-delete-folder").save());
    }

    private void testMultiplayerCustomIcon(ClientGameTestContext context) {
        // Add a green custom icon
        context.runOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            MultiplayerFolderEntry entry = getListWidgetEntry(listWidget, MultiplayerFolderEntry.class, 0);
            NativeImage image = new NativeImage(64, 64, true);
            image.fillRect(0, 0, 64, 64, 0xFF00FF00);
            entry.getCustomIconTexture().upload(image);
        });
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root-custom-icon").save());
        // Test serialization
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.multiplayer");
        context.clickScreenButton("gui.proceed");
        context.waitTick();
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root-custom-icon").save());
        // Delete the custom icon
        clickListWidgetEntry(context, MultiplayerFolderEntry.class, 33);
        context.clickScreenButton("selectServer.edit");
        context.clickScreenButton("organizableplayscreens:entry.icon.delete");
        context.waitForScreen(ConfirmScreen.class);
        context.clickScreenButton("organizableplayscreens:entry.icon.delete");
        context.clickScreenButton("gui.done");
        context.clickScreenButton("gui.cancel");
        context.clickScreenButton("menu.multiplayer");
        context.clickScreenButton("gui.proceed");
        context.waitTick();
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root-delete-folder").save());
    }

    private void createNewFolder(ClientGameTestContext context) {
        createNewEntry(context, "organizableplayscreens:folder.folder");
    }

    private void createNewEntry(ClientGameTestContext context, String translationKey) {
        // Click the add entry button
        clickScreenButton(context, "+");
        // Assert the new entry screen
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("new-folder-screen").save());
        // Click the entry type button
        context.clickScreenButton(translationKey);
        // Finish creating the new entry
        context.clickScreenButton("gui.done");
    }

    private void clickScreenButton(ClientGameTestContext context, String text) {
        AbstractWidget widget = context.computeOnClient(client -> Optional.ofNullable(client.gui.screen())
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getRenderables)
                .orElse(List.of())
                .stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(clickableWidget -> text.equals(clickableWidget.getMessage().getString()))
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find button '%s' in screen '%s'".formatted(text, Optionull.map(client.gui.screen(), screen -> screen.getClass().getName()))))
        );
        // Move mouse
        setCursorPos(context, widget.getX(), widget.getY());
        // Click
        context.runOnClient(_ -> widget.onClick(new MouseButtonEvent(widget.getX(), widget.getY(), new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false));
    }

    private <T extends ObjectSelectionList.Entry<? super T>> void clickListWidgetEntry(ClientGameTestContext context, Class<T> entryClass, int xOffset) {
        clickListWidgetEntry(context, entryClass, 0, xOffset);
    }

    private <T extends ObjectSelectionList.Entry<? super T>> void clickListWidgetEntry(ClientGameTestContext context, Class<T> entryClass, int ordinal, int xOffset) {
        T entry = context.computeOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            return getListWidgetEntry(listWidget, entryClass, ordinal);
        });
        int x = entry.getContentX() + xOffset;
        int y = entry.getContentY();
        // Move mouse
        setCursorPos(context, x, y);
        // Click
        context.runOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            listWidget.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
        });
    }

    private <T extends ObjectSelectionList.Entry<? super T> & AbstractFolderEntry<?, ? super T>> void clickFolderMoveInto(ClientGameTestContext context, Class<T> folderClass) {
        T folderEntry = context.computeOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            return getListWidgetEntry(listWidget, folderClass, 0);
        });

        // Move mouse
        setCursorPos(context, folderEntry.getButtonMoveInto().getX(), folderEntry.getButtonMoveInto().getY());
        // Click
        context.runOnClient(_ -> folderEntry.getButtonMoveInto().onClick(new MouseButtonEvent(folderEntry.getButtonMoveInto().getX(), folderEntry.getButtonMoveInto().getY(), new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false));
    }

    private ObjectSelectionList<?> getListWidget(Minecraft client) {
        return Optional.ofNullable(client.gui.screen())
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getRenderables)
                .orElse(List.of())
                .stream()
                .filter(ObjectSelectionList.class::isInstance)
                .map(ObjectSelectionList.class::cast)
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find list widget in screen '%s'".formatted((Object) Optionull.map(client.gui.screen(), screen -> screen.getClass().getName()))));
    }

    @SuppressWarnings("unchecked")
    private <T extends ObjectSelectionList.Entry<? super T>> T getListWidgetEntry(ObjectSelectionList<?> listWidget, Class<T> entryClass, int ordinal) {
        return (T) listWidget.children().stream()
                .filter(entryClass::isInstance)
                .skip(ordinal)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not find entry of type '%s' with ordinal '%s' in list widget '%s' with entries '%s'".formatted(entryClass.getName(), ordinal, listWidget.getClass().getName(), listWidget.children())));
    }

    private void setCursorPos(ClientGameTestContext context, int x, int y) {
        var screenPos = getScreenPos(context, x, y);
        context.getInput().setCursorPos(screenPos.x, screenPos.y);
    }

    private Vector2d getScreenPos(ClientGameTestContext context, int x, int y) {
        return context.computeOnClient(client -> {
            Window window = client.getWindow();
            return new Vector2d(
                    (double) x * window.getScreenWidth() / window.getGuiScaledWidth(),
                    (double) y * window.getScreenHeight() / window.getGuiScaledHeight()
            );
        });
    }
}
