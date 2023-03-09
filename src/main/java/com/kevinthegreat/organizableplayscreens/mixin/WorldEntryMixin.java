package com.kevinthegreat.organizableplayscreens.mixin;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry {

    @Shadow
    @Final
    WorldListWidget field_19135;

    /**
     * Selects the world entry before the available check to allow moving unavailable worlds across folders.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void organizableplayscreens_setThisSelectedBeforeAvailableCheck(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        field_19135.setSelected(this);
    }
}
