package com.kevinthegreat.organizableplayscreens;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.mixin.client.gametest.ScreenAccessor;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Nullables;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class OrganizablePlayScreensClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Create a new world
        //noinspection EmptyTryBlock, unused
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {}
        context.waitForScreen(TitleScreen.class);
        // Navigate to the select world screen
        context.clickScreenButton("menu.singleplayer");
        // Create a new folder
        createNewFolder(context);
        // Assert the select world screen
        context.assertScreenshotEquals("select-world-screen");

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);

        // Navigate to the multiplayer screen
        context.clickScreenButton("menu.multiplayer");
        // Skip the multiplayer warning screen
        context.clickScreenButton("gui.proceed");
        // Add a new server
        context.clickScreenButton("selectServer.add");
        context.clickScreenButton("addServer.add");
        // Create a new folder
        createNewFolder(context);
        // Assert the multiplayer screen
        context.assertScreenshotEquals("multiplayer-screen");

        // Navigate back to the title screen
        clickScreenButton(context, "←");
        context.waitForScreen(TitleScreen.class);
    }

    private void createNewFolder(ClientGameTestContext context) {
        // Create a new folder
        clickScreenButton(context, "+");
        // Assert the new folder screen
        context.assertScreenshotEquals("new-folder-screen");
        // Finish creating the new folder
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
}
