package com.kevinthegreat.organizableplayscreens.mixin.accessor;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiplayerScreen.class)
public interface MultiplayerScreenAccessor {
    @Accessor("field_62178")
    ThreePartsLayoutWidget getLayout();

    @Accessor
    MultiplayerServerListWidget getServerListWidget();
}
