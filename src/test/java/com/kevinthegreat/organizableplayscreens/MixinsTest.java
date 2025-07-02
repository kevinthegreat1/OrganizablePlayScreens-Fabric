package com.kevinthegreat.organizableplayscreens;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public class MixinsTest {
    @BeforeAll
    static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void auditMixins() {
        Assertions.assertInstanceOf(IMixinTransformer.class, MixinEnvironment.getCurrentEnvironment().getActiveTransformer());
        // Temporarily disable mixin audit since it throws an `IllegalStateException` trying to load `GuiRenderer` and call `RenderSystem.getDevice()` in 1.21.7.
        // Client game test covers this anyway.
        // MixinEnvironment.getCurrentEnvironment().audit();
    }
}
