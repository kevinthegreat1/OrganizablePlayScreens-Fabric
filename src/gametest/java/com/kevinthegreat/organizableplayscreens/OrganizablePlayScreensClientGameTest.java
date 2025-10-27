package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.gui.AbstractFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.MultiplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.gui.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.mixin.accessor.EntryListWidgetInvoker;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.mixin.client.gametest.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Nullables;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class OrganizablePlayScreensClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Create a new world and navigate to the select world screen
        //noinspection EmptyTryBlock, unused
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {}
        context.waitForScreen(TitleScreen.class);
        context.clickScreenButton("menu.singleplayer");
        // Create a new folder and assert the select world screen root
        createNewFolder(context);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-root").save());

        // Move the world into the folder
        clickListWidgetEntry(context, WorldListWidget.WorldEntry.class);
        clickFolderMoveInto(context, SingleplayerFolderEntry.class);
        // Open the folder
        clickListWidgetEntry(context, SingleplayerFolderEntry.class);
        clickListWidgetEntry(context, SingleplayerFolderEntry.class);
        // Create a new folder, section, and separator inside the folder and assert the select world screen folder
        createNewFolder(context);
        createNewEntry(context, "organizableplayscreens:entry.section");
        createNewEntry(context, "organizableplayscreens:entry.separator");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("select-world-screen-folder").save());

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);

        // Navigate to the multiplayer screen, skip the multiplayer warning screen, and add a new server
        context.clickScreenButton("menu.multiplayer");
        context.clickScreenButton("gui.proceed");
        context.clickScreenButton("selectServer.add");
        context.clickScreenButton("addServer.add");
        // Create a new folder and assert the multiplayer screen root
        createNewFolder(context);
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-root").save());

        // Move the server into the folder
        clickListWidgetEntry(context, MultiplayerServerListWidget.ServerEntry.class);
        clickFolderMoveInto(context, MultiplayerFolderEntry.class);
        // Open the folder
        clickListWidgetEntry(context, MultiplayerFolderEntry.class);
        clickListWidgetEntry(context, MultiplayerFolderEntry.class);
        // Create a new folder, section, and separator inside the folder and assert the multiplayer screen folder
        createNewFolder(context);
        createNewEntry(context, "organizableplayscreens:entry.section");
        createNewEntry(context, "organizableplayscreens:entry.separator");
        context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("multiplayer-screen-folder").save());

        // Navigate back to the title screen
        clickScreenButton(context, "←");
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
        context.runOnClient(client -> Optional.ofNullable(client.currentScreen)
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getDrawables)
                .orElse(List.of())
                .stream()
                .filter(ClickableWidget.class::isInstance)
                .map(ClickableWidget.class::cast)
                .filter(clickableWidget -> text.equals(clickableWidget.getMessage().getString()))
                .findAny()
                .ifPresentOrElse(clickableWidget -> clickableWidget.onClick(clickableWidget.getX(), clickableWidget.getY()), () -> {
                    throw new AssertionError("Could not find button '%s' in screen '%s'".formatted(text, Nullables.map(client.currentScreen, screen -> screen.getClass().getName())));
                })
        );
    }

    private <T extends AlwaysSelectedEntryListWidget.Entry<? super T>> void clickListWidgetEntry(ClientGameTestContext context, Class<T> entryClass) {
        context.runOnClient(client -> {
            AlwaysSelectedEntryListWidget<?> listWidget = getListWidget(client);
            T entry = getListWidgetEntry(listWidget, entryClass);
            int i = listWidget.children().indexOf(this);
            int x = listWidget.getRowLeft();
            int y = ((EntryListWidgetInvoker) listWidget).rowTop(i);
            int w = listWidget.getRowWidth();
            entry.mouseClicked(x + w, y, 0);
        });
    }

    private <T extends AlwaysSelectedEntryListWidget.Entry<? super T> & AbstractFolderEntry<?, ? super T>> void clickFolderMoveInto(ClientGameTestContext context, Class<T> folderClass) {
        context.runOnClient(client -> {
            AlwaysSelectedEntryListWidget<?> listWidget = getListWidget(client);
            T folderEntry = getListWidgetEntry(listWidget, folderClass);
            folderEntry.getButtonMoveInto().onClick(folderEntry.getButtonMoveInto().getX(), folderEntry.getButtonMoveInto().getY());
        });
    }

    private AlwaysSelectedEntryListWidget<?> getListWidget(MinecraftClient client) {
        return Optional.ofNullable(client.currentScreen)
                .map(ScreenAccessor.class::cast)
                .map(ScreenAccessor::getDrawables)
                .orElse(List.of())
                .stream()
                .filter(AlwaysSelectedEntryListWidget.class::isInstance)
                .map(AlwaysSelectedEntryListWidget.class::cast)
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find list widget in screen '%s'".formatted((Object) Nullables.map(client.currentScreen, screen -> screen.getClass().getName()))));
    }

    @SuppressWarnings("unchecked")
    private <T extends AlwaysSelectedEntryListWidget.Entry<? super T>> T getListWidgetEntry(AlwaysSelectedEntryListWidget<?> listWidget, Class<T> entryClass) {
        return (T) listWidget.children().stream()
                .filter(entryClass::isInstance)
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find entry of type '%s' in list widget '%s'".formatted(entryClass.getName(), listWidget.getClass().getName())));
    }
}
