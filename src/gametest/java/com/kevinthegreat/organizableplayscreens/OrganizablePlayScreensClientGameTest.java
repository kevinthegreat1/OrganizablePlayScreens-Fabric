package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.gui.*;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.AbstractSelectionListInvoker;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.mixin.client.gametest.gui.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.Optionull;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class OrganizablePlayScreensClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Singleplayer
        // Create a new world and navigate to the select world screen
        //noinspection EmptyTryBlock, unused
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {}
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

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);
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
        context.runOnClient(client -> Optional.ofNullable(client.screen)
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getDrawables)
                .orElse(List.of())
                .stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(clickableWidget -> text.equals(clickableWidget.getMessage().getString()))
                .findAny()
                .ifPresentOrElse(clickableWidget -> clickableWidget.onClick(new MouseButtonEvent(clickableWidget.getX(), clickableWidget.getY(), new MouseButtonInfo(0, 0)), false), () -> {
                    throw new AssertionError("Could not find button '%s' in screen '%s'".formatted(text, Optionull.map(client.screen, screen -> screen.getClass().getName())));
                })
        );
    }

    private <T extends ObjectSelectionList.Entry<? super T>> void clickListWidgetEntry(ClientGameTestContext context, Class<T> entryClass, int xOffset) {
        clickListWidgetEntry(context, entryClass, 0, xOffset);
    }

    private <T extends ObjectSelectionList.Entry<? super T>> void clickListWidgetEntry(ClientGameTestContext context, Class<T> entryClass, int ordinal, int xOffset) {
        context.runOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            T entry = getListWidgetEntry(listWidget, entryClass, ordinal);
            int i = listWidget.children().indexOf(entry);
            int x = listWidget.getRowLeft() + xOffset;
            int y = ((AbstractSelectionListInvoker) listWidget).rowTop(i);
            listWidget.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)), false);
        });
    }

    private <T extends ObjectSelectionList.Entry<? super T> & AbstractFolderEntry<?, ? super T>> void clickFolderMoveInto(ClientGameTestContext context, Class<T> folderClass) {
        context.runOnClient(client -> {
            ObjectSelectionList<?> listWidget = getListWidget(client);
            T folderEntry = getListWidgetEntry(listWidget, folderClass, 0);
            folderEntry.getButtonMoveInto().onClick(new MouseButtonEvent(folderEntry.getButtonMoveInto().getX(), folderEntry.getButtonMoveInto().getY(), new MouseButtonInfo(0, 0)), false);
        });
    }

    private ObjectSelectionList<?> getListWidget(Minecraft client) {
        return Optional.ofNullable(client.screen)
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getDrawables)
                .orElse(List.of())
                .stream()
                .filter(ObjectSelectionList.class::isInstance)
                .map(ObjectSelectionList.class::cast)
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find list widget in screen '%s'".formatted((Object) Optionull.map(client.screen, screen -> screen.getClass().getName()))));
    }

    @SuppressWarnings("unchecked")
    private <T extends ObjectSelectionList.Entry<? super T>> T getListWidgetEntry(ObjectSelectionList<?> listWidget, Class<T> entryClass, int ordinal) {
        return (T) listWidget.children().stream()
                .filter(entryClass::isInstance)
                .skip(ordinal)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not find entry of type '%s' with ordinal '%s' in list widget '%s' with entries '%s'".formatted(entryClass.getName(), ordinal, listWidget.getClass().getName(), listWidget.children())));
    }
}
