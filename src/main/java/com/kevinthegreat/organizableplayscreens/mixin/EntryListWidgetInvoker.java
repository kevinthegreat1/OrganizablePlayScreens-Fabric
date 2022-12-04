package com.kevinthegreat.organizableplayscreens.mixin;

import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntryListWidget.class)
public interface EntryListWidgetInvoker {
    @Invoker("getRowTop")
    int rowTop(int index);
}
