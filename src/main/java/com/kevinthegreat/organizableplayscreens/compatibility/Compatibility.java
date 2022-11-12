package com.kevinthegreat.organizableplayscreens.compatibility;

import gg.essential.api.EssentialAPI;
import net.fabricmc.loader.api.FabricLoader;

public class Compatibility {
    public static boolean essential_preventMultiplayerFeatures() {
        return FabricLoader.getInstance().isModLoaded("essential") && EssentialAPI.getConfig().getCurrentMultiplayerTab() != 0;
    }
}
