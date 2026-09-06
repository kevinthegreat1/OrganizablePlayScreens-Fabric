package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.mixin.ServerSelectionListMixin;
import com.kevinthegreat.organizableplayscreens.mixin.WorldSelectionListMixin;
import com.kevinthegreat.organizableplayscreens.option.OrganizablePlayScreensOptions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface AbstractFolderEntry<T extends ObjectSelectionList<E>, E extends ObjectSelectionList.Entry<E>> extends AbstractEntry<T, E> {
    /**
     * Gets all entries contained in this folder.
     */
    List<E> getEntries();

    /**
     * Gets the button that moves the selected entry into this folder.
     *
     * @return the button that moves the selected entry into this folder
     */
    Button getButtonMoveInto();

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateScreenButtonStates(Button selectButton, Button editButton, Button deleteButton, @Nullable Button recreateButton) {
        AbstractEntry.super.updateScreenButtonStates(selectButton, editButton, deleteButton, recreateButton);
        selectButton.setMessage(Component.translatable("organizableplayscreens:folder.openFolder"));
        selectButton.active = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void updateButtonStates(@Nullable E selectedEntry) {
        getButtonMoveInto().active = selectedEntry != null && !(selectedEntry instanceof ServerSelectionList.LANHeader) && selectedEntry != this;
    }

    @Override
    default void render(GuiGraphicsExtractor context, int index, int y, int x, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int listSize) {
        int textX = x + 32 + 3;
        List<E> entries = getEntries();
        MutableComponent entriesText = Component.translatable("organizableplayscreens:entry.entries", entries.size());
        Button buttonMoveInto = getButtonMoveInto();

        context.text(client.font, name, textX, y + 1, 0xFFFFFFFF);
        context.text(client.font, entriesText, textX, y + 12, 0xFF808080);

        if (mouseX >= textX && mouseX < textX + client.font.width(entriesText) && mouseY >= y + 12 && mouseY < y + 12 + 9) {
            List<Component> tooltip = new ArrayList<>();
            long servers = entries.stream().filter(ServerSelectionList.OnlineServerEntry.class::isInstance).count();
            if (servers > 0) {
                tooltip.add(Component.translatable("organizableplayscreens:entry.servers", servers));
            }
            long worlds = entries.stream().filter(WorldSelectionList.WorldListEntry.class::isInstance).count();
            if (worlds > 0) {
                tooltip.add(Component.translatable("organizableplayscreens:entry.worlds", worlds));
            }
            long folders = entries.stream().filter(AbstractFolderEntry.class::isInstance).count();
            if (folders > 0) {
                tooltip.add(Component.translatable("organizableplayscreens:entry.folders", folders));
            }
            context.setComponentTooltipForNextFrame(client.font, tooltip, mouseX, mouseY);
        }

        renderIcon(context, x, y, 32, 32, mouseX, mouseY, hovered, tickDelta, name, 0);
        AbstractEntry.renderEntry(context, index, y, x, mouseX, mouseY, hovered, listSize, true);
        OrganizablePlayScreensOptions options = OrganizablePlayScreens.getInstance().options;
        if (options.showMoveEntryIntoButton.get()) {
            buttonMoveInto.setPosition(options.getValue(options.moveEntryIntoButtonX), y + options.moveEntryIntoButtonY.get());
            buttonMoveInto.extractRenderState(context, mouseX, mouseY, tickDelta);
        }
    }

    @Override
    default void renderIcon(GuiGraphicsExtractor context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta, String name, int depth) {
        if (getCustomIcon().isPresent()) {
            AbstractEntry.super.renderIcon(context, x, y, width, height, mouseX, mouseY, hovered, tickDelta, name, depth);
            return;
        }

        if (depth >= 5) return;

        List<EntryWithIcon> entriesWithIcon = getEntriesWithIcon(depth).limit(4).toList();
        switch (entriesWithIcon.size()) {
            case 0 -> {}
            case 1 -> entriesWithIcon.getFirst().renderIcon(context, x + width / 4, y + height / 4, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
            case 2 -> {
                entriesWithIcon.getFirst().renderIcon(context, x, y + height / 4, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.getLast().renderIcon(context, x + width / 2, y + height / 4, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
            }
            case 3 -> {
                entriesWithIcon.get(0).renderIcon(context, x + width / 4, y, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.get(1).renderIcon(context, x, y + height / 2, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.get(2).renderIcon(context, x + width / 2, y + height / 2, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
            }
            default -> {
                entriesWithIcon.get(0).renderIcon(context, x, y, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.get(1).renderIcon(context, x + width / 2, y, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.get(2).renderIcon(context, x, y + height / 2, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
                entriesWithIcon.get(3).renderIcon(context, x + width / 2, y + height / 2, width / 2, height / 2, mouseX, mouseY, hovered, tickDelta, name, depth);
            }
        }
    }

    /**
     * Gets the entries with an icon in this folder.
     *
     * @param depth How deeply nested within the icon this current entry is.
     *              0 means that the icon of this entry is being calculated and rendered.
     *              At depth 0, the size of child icons is 16.
     */
    private Stream<EntryWithIcon> getEntriesWithIcon(int depth) {
        if (depth >= 5) return Stream.empty();

        return getEntries().stream()
                .<EntryWithIcon>map(entry -> switch (entry) {
                    case EntryWithIcon entryWithIcon when entryWithIcon.getCustomIcon().isPresent() -> entryWithIcon;
                    case ServerSelectionList.OnlineServerEntry serverEntry -> (context, x, y, width, height, _, _, _, _, _, _) -> context.blit(RenderPipelines.GUI_TEXTURED, ((ServerSelectionListMixin.ServerEntryAccessor) serverEntry).getIcon().textureLocation(), x, y, 0, 0, width, height, 32, 32, 32, 32);
                    case WorldSelectionList.WorldListEntry worldEntry -> (context, x, y, width, height, _, _, _, _, _, _) -> context.blit(RenderPipelines.GUI_TEXTURED, ((WorldSelectionListMixin.WorldEntryAccessor) worldEntry).getIcon().textureLocation(), x, y, 0, 0, width, height, 32, 32, 32, 32);
                    case AbstractFolderEntry<?, ?> folderEntry when folderEntry.getEntriesWithIcon(depth + 1).findAny().isPresent() -> folderEntry;
                    default -> null;
                }).filter(Objects::nonNull);
    }
}
