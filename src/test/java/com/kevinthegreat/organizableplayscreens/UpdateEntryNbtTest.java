package com.kevinthegreat.organizableplayscreens;

import com.kevinthegreat.organizableplayscreens.api.EntryType;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpdateEntryNbtTest {
    @Test
    void testUpdateTrueType() {
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putBoolean("type", true);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, false);
        Assertions.assertEquals(EntryType.FOLDER.id().toString(), nbtCompound.getStringOr("type", ""));
    }

    @Test
    void testUpdateFalseTypeSingleplayer(){
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, false);
        Assertions.assertEquals("minecraft:world", nbtCompound.getStringOr("type", ""));
    }

    @Test
    void testUpdateFalseTypeMultiplayer() {
        CompoundTag nbtCompound = new CompoundTag();
        nbtCompound.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound, true);
        Assertions.assertEquals("minecraft:server", nbtCompound.getStringOr("type", ""));
    }

    @Test
    void testUpdateDefaultTypeSingleplayer() {
        CompoundTag nbtCompound4 = new CompoundTag();
        OrganizablePlayScreens.updateEntryNbt(nbtCompound4, false);
        Assertions.assertEquals("minecraft:world", nbtCompound4.getStringOr("type", ""));
    }

    @Test
    void testUpdateDefaultTypeMultiplayer() {
        CompoundTag nbtCompound5 = new CompoundTag();
        OrganizablePlayScreens.updateEntryNbt(nbtCompound5, true);
        Assertions.assertEquals("minecraft:server", nbtCompound5.getStringOr("type", ""));
    }

    @Test
    void testUpdateSectionType() {
        CompoundTag nbtCompound6 = new CompoundTag();
        nbtCompound6.putString("type", EntryType.SECTION.id().toString());
        OrganizablePlayScreens.updateEntryNbt(nbtCompound6, false);
        Assertions.assertEquals(EntryType.SECTION.id().toString(), nbtCompound6.getStringOr("type", ""));
    }

    @Test
    void testUpdateEmptyStringType() {
        CompoundTag nbtCompound7 = new CompoundTag();
        nbtCompound7.putString("type", "");
        Assertions.assertEquals("", nbtCompound7.getStringOr("type", ""));
    }
}
