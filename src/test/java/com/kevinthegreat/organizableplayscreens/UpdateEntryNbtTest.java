package com.kevinthegreat.organizableplayscreens;

import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateEntryNbtTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEntryNbtTest.class);

    public static void main(String[] args) {
        NbtCompound nbtCompound1 = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        NbtCompound nbtCompound3 = new NbtCompound();
        NbtCompound nbtCompound4 = new NbtCompound();
        NbtCompound nbtCompound5 = new NbtCompound();
        NbtCompound nbtCompound6 = new NbtCompound();
        NbtCompound nbtCompound7 = new NbtCompound();
        nbtCompound1.putBoolean("type", true);
        nbtCompound2.putBoolean("type", false);
        nbtCompound3.putBoolean("type", false);
        nbtCompound6.putString("type", "organizableplayscreens:section");
        nbtCompound7.putString("type", "");
        OrganizablePlayScreens.updateEntryNbt(nbtCompound1, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound2, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound3, true);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound4, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound5, true);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound6, false);
        LOGGER.info("[UpdateEntryNbtTest] Test 1: " + nbtCompound1.getString("type").equals("organizableplayscreens:folder"));
        LOGGER.info("[UpdateEntryNbtTest] Test 2: " + nbtCompound2.getString("type").equals("minecraft:world"));
        LOGGER.info("[UpdateEntryNbtTest] Test 3: " + nbtCompound3.getString("type").equals("minecraft:server"));
        LOGGER.info("[UpdateEntryNbtTest] Test 4: " + nbtCompound4.getString("type").equals("minecraft:world"));
        LOGGER.info("[UpdateEntryNbtTest] Test 5: " + nbtCompound5.getString("type").equals("minecraft:server"));
        LOGGER.info("[UpdateEntryNbtTest] Test 6: " + nbtCompound6.getString("type").equals("organizableplayscreens:section"));
        LOGGER.info("[UpdateEntryNbtTest] Test 7: " + nbtCompound7.getString("type").equals(""));
    }
}
