package com.kevinthegreat.organizableplayscreens.mixin;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import com.kevinthegreat.organizableplayscreens.SingleplayerFolderEntry;
import com.kevinthegreat.organizableplayscreens.WorldListWidgetAccessor;
import com.kevinthegreat.organizableplayscreens.screen.EditFolderScreen;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    @Final
    protected Screen parent;
    @Shadow
    private ButtonWidget selectButton;
    @Shadow
    private ButtonWidget recreateButton;
    @Shadow
    protected TextFieldWidget searchBox;
    @Shadow
    public WorldListWidget levelList;

    @Shadow
    public abstract Supplier<String> getSearchFilter();

    public WorldListWidgetAccessor worldListWidgetAccessor;
    private ButtonWidget organizableplayscreens_buttonCancel;
    private ButtonWidget organizableplayscreens_buttonBack;
    private ButtonWidget organizableplayscreens_buttonMoveEntryBack;
    private ButtonWidget organizableplayscreens_buttonNewFolder;
    @Nullable
    private SingleplayerFolderEntry organizableplayscreens_newFolder;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;levelList:Lnet/minecraft/client/gui/screen/world/WorldListWidget;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void organizableplayscreens_setWorldListWidgetAccessor(CallbackInfo ci) {
        worldListWidgetAccessor = (WorldListWidgetAccessor) levelList;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;worldSelected(Z)V"))
    private void organizableplayscreens_addButtons(CallbackInfo ci) {
        organizableplayscreens_buttonBack = addDrawableChild(new ButtonWidget(8, 8, 20, 20, Text.of("←"), buttonWidget -> {
            if (!worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
                client.setScreen(parent);
            }
        }));
        organizableplayscreens_buttonMoveEntryBack = addDrawableChild(new ButtonWidget(36, 8, 20, 20, Text.of("←+"), buttonWidget -> {
            if (!worldListWidgetAccessor.organizableplayscreens_isRootFolder()) {
                WorldListWidget.Entry entry = levelList.getSelectedOrNull();
                SingleplayerFolderEntry parentFolder = worldListWidgetAccessor.organizableplayscreens_getCurrentFolder().getParent();
                if (entry instanceof WorldListWidget.WorldEntry worldEntry) {
                    worldListWidgetAccessor.organizableplayscreens_getWorlds().put(worldEntry, parentFolder);
                    parentFolder.getWorldEntries().add(worldEntry);
                    OrganizablePlayScreens.sortWorldEntries(parentFolder.getWorldEntries());
                    worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries().remove(worldEntry);
                } else if (entry instanceof SingleplayerFolderEntry folderEntry) {
                    folderEntry.setParent(parentFolder);
                    parentFolder.getFolderEntries().add(folderEntry);
                    worldListWidgetAccessor.organizableplayscreens_getCurrentFolderEntries().remove(folderEntry);
                }
                levelList.setSelected(null);
                worldListWidgetAccessor.organizableplayscreens_updateAndSave();
            }
        }, OrganizablePlayScreens.MOVE_ENTRY_BACK_TOOLTIP_SUPPLIER));
        organizableplayscreens_buttonNewFolder = addDrawableChild(new ButtonWidget(width - 28, 8, 20, 20, Text.of("+"), buttonWidget -> {
            organizableplayscreens_newFolder = new SingleplayerFolderEntry((SelectWorldScreen) (Object) this, worldListWidgetAccessor.organizableplayscreens_getCurrentFolder());
            client.setScreen(new EditFolderScreen(this::organizableplayscreens_addFolder, organizableplayscreens_newFolder, true));
            levelList.setSelected(organizableplayscreens_newFolder);
        }));
    }

    @Inject(method = "method_19945", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifySelectButton(ButtonWidget button, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof SingleplayerFolderEntry folderEntry) {
            worldListWidgetAccessor.organizableplayscreens_setCurrentFolder(folderEntry);
            ci.cancel();
        }
    }

    @Inject(method = "method_19943", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyEditButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof SingleplayerFolderEntry folderEntry) {
            client.setScreen(new EditFolderScreen(this::organizableplayscreens_editFolder, folderEntry, false));
            ci.cancel();
        }
    }

    @Inject(method = "method_19942", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyDeleteButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (levelList.getSelectedOrNull() instanceof SingleplayerFolderEntry folderEntry) {
            client.setScreen(new ConfirmScreen(this::organizableplayscreens_deleteFolder, Text.translatable("organizableplayscreens:folder.deleteFolderQuestion"), Text.translatable("organizableplayscreens:folder.deleteSingleplayerFolderWarning", folderEntry.getName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
            ci.cancel();
        }
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 5))
    private <T extends Element & Drawable & Selectable> T organizableplayscreens_setCancelButton(SelectWorldScreen selectWorldScreen, T element) {
        organizableplayscreens_buttonCancel = addDrawableChild((ButtonWidget) element);
        return element;
    }

    @Inject(method = "method_19939", at = @At("HEAD"), cancellable = true)
    private void organizableplayscreens_modifyCancelButton(ButtonWidget buttonWidget, CallbackInfo ci) {
        if (worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent()) {
            ci.cancel();
        }
    }

    private void organizableplayscreens_addFolder(boolean confirmedAction) {
        if (confirmedAction) {
            worldListWidgetAccessor.organizableplayscreens_getCurrentFolderEntries().add(organizableplayscreens_newFolder);
            worldListWidgetAccessor.organizableplayscreens_updateAndSave();
            organizableplayscreens_newFolder = null;
        }
        client.setScreen(this);
    }

    private void organizableplayscreens_editFolder(boolean confirmedAction) {
        client.setScreen(this);
    }

    private void organizableplayscreens_deleteFolder(boolean confirmedAction) {
        if (confirmedAction && levelList.getSelectedOrNull() instanceof SingleplayerFolderEntry folder) {
            for (SingleplayerFolderEntry folderEntry : folder.getFolderEntries()) {
                folderEntry.setParent(worldListWidgetAccessor.organizableplayscreens_getCurrentFolder());
                worldListWidgetAccessor.organizableplayscreens_getCurrentFolderEntries().add(folderEntry);
            }
            for (WorldListWidget.WorldEntry worldEntry : folder.getWorldEntries()) {
                worldListWidgetAccessor.organizableplayscreens_getWorlds().put(worldEntry, worldListWidgetAccessor.organizableplayscreens_getCurrentFolder());
                worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries().add(worldEntry);
            }
            OrganizablePlayScreens.sortWorldEntries(worldListWidgetAccessor.organizableplayscreens_getCurrentWorldEntries());
            worldListWidgetAccessor.organizableplayscreens_getCurrentFolderEntries().remove(folder);
            levelList.setSelected(null);
            worldListWidgetAccessor.organizableplayscreens_updateAndSave();
        } client.setScreen(this);
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void organizableplayscreens_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(keyCode == GLFW.GLFW_KEY_ESCAPE && !shouldCloseOnEsc() && worldListWidgetAccessor.organizableplayscreens_setCurrentFolderToParent() || super.keyPressed(keyCode, scanCode, modifiers) || searchBox.keyPressed(keyCode, scanCode, modifiers) || levelList.keyPressed(keyCode, scanCode, modifiers));
        cir.cancel();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 8))
    private int organizableplayscreens_modifyTitleY(int original) {
        return worldListWidgetAccessor.organizableplayscreens_getCurrentPath().isEmpty() ? 8 : 12;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/SelectWorldScreen;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", shift = At.Shift.AFTER))
    private void organizableplayscreens_renderPath(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawCenteredText(matrices, textRenderer, worldListWidgetAccessor.organizableplayscreens_getCurrentPath(), width / 2, 2, 0xa0a0a0);
    }

    @Inject(method = "worldSelected", at = @At("RETURN"))
    private void organizableplayscreens_updateButtonStates(boolean active, CallbackInfo ci) {
        WorldListWidget.Entry entry = levelList.getSelectedOrNull();
        if (entry instanceof WorldListWidget.WorldEntry) {
            selectButton.setMessage(Text.translatable("selectWorld.select"));
        } else if (entry instanceof SingleplayerFolderEntry) {
            selectButton.setMessage(Text.translatable("organizableplayscreens:folder.openFolder"));
            recreateButton.active = false;
        }
        boolean notSearching = getSearchFilter().get().isEmpty();
        worldListWidgetAccessor.organizableplayscreens_updateCurrentPath();
        searchBox.y = worldListWidgetAccessor.organizableplayscreens_getCurrentPath().isEmpty() ? 22 : 24;
        organizableplayscreens_buttonBack.active = notSearching;
        organizableplayscreens_buttonMoveEntryBack.active = entry != null && !worldListWidgetAccessor.organizableplayscreens_isRootFolder() && notSearching;
        organizableplayscreens_buttonNewFolder.active = notSearching;
        organizableplayscreens_buttonCancel.setMessage(worldListWidgetAccessor.organizableplayscreens_isRootFolder() ? ScreenTexts.CANCEL : ScreenTexts.BACK);
        for (SingleplayerFolderEntry folderEntry : worldListWidgetAccessor.organizableplayscreens_getCurrentFolderEntries()) {
            folderEntry.updateButtonStates();
        }
    }

    @Override
    public void removed() {
        if (worldListWidgetAccessor != null) {
            worldListWidgetAccessor.organizableplayscreens_saveFile();
        }
        super.removed();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return worldListWidgetAccessor.organizableplayscreens_isRootFolder();
    }
}
