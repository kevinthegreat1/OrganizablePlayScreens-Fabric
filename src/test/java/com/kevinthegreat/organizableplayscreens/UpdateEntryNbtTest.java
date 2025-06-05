package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpdateEntryNbtTest {
    @Test
    void testUpdateTrueType() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("type", true);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, false);
        Assertions.assertEquals(EntryType.FOLDER.id().toString(), nbtCompound.getString("type", ""));
    }

    @Test
    void testUpdateFalseTypeSingleplayer(){
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, false);
        Assertions.assertEquals("minecraft:world", nbtCompound.getString("type", ""));
    }

    @Test
    void testUpdateFalseTypeMultiplayer() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, true);
        Assertions.assertEquals("minecraft:server", nbtCompound.getString("type", ""));
    }

    @Test
    void testUpdateDefaultTypeSingleplayer() {
        NbtCompound nbtCompound4 = new NbtCompound();
        OrganizablePlayScreens.updateEntryNbt(nbtCompound4, false);
        Assertions.assertEquals("minecraft:world", nbtCompound4.getString("type", ""));
    }

    @Test
    void testUpdateDefaultTypeMultiplayer() {
        NbtCompound nbtCompound5 = new NbtCompound();
        OrganizablePlayScreens.updateEntryNbt(nbtCompound5, true);
        Assertions.assertEquals("minecraft:server", nbtCompound5.getString("type", ""));
    }

    @Test
    void testUpdateSectionType() {
        NbtCompound nbtCompound6 = new NbtCompound();
        nbtCompound6.putString("type", EntryType.SECTION.id().toString());
        OrganizablePlayScreens.updateEntryNbt(nbtCompound6, false);
        Assertions.assertEquals(EntryType.SECTION.id().toString(), nbtCompound6.getString("type", ""));
    }

    @Test
    void testUpdateEmptyStringType() {
        NbtCompound nbtCompound7 = new NbtCompound();
        nbtCompound7.putString("type", "");
        Assertions.assertEquals("", nbtCompound7.getString("type", ""));
    }
}
