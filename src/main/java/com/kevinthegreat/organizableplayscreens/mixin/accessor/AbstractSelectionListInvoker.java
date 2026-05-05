package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListInvoker {
    @Invoker("getRowTop")
    int rowTop(int index);
}
