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
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}
