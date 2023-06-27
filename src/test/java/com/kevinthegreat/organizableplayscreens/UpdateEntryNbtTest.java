package com.kevinthegreat.organizableplayscreens;

import net.minecraft.nbt.NbtCompound;

public class UpdateEntryNbtTest {
    public static void main(String[] args) {
        NbtCompound nbtCompound1 = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        NbtCompound nbtCompound3 = new NbtCompound();
        NbtCompound nbtCompound4 = new NbtCompound();
        NbtCompound nbtCompound5 = new NbtCompound();
        nbtCompound1.putBoolean("type", true);
        nbtCompound2.putBoolean("type", false);
        nbtCompound3.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound1, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound2, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound3, true);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound4, false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound5, true);
        System.out.println(nbtCompound1.getString("type").equals("organizableplayscreens:folder"));
        System.out.println(nbtCompound2.getString("type").equals("minecraft:world"));
        System.out.println(nbtCompound3.getString("type").equals("minecraft:server"));
        System.out.println(nbtCompound4.getString("type").equals("minecraft:world"));
        System.out.println(nbtCompound5.getString("type").equals("minecraft:server"));
    }
}
