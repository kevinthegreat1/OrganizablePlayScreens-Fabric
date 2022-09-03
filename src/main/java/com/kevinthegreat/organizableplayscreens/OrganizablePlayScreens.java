package com.kevinthegreat.organizableplayscreens;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizablePlayScreens implements ModInitializer {
    public static final String MOD_ID = "organizableplayscreens";
    public static final String MOD_NAME = "Organizable Play Screens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info(MOD_NAME + " initialized.");
    }
}
