package com.kevinthegreat.organizableplayscreens;

import net.minecraft.nbt.NbtCompound;

public class UpdateEntryNbtTest {
    public static void main(String[] args) {
        NbtCompound nbtCompound1 = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        nbtCompound1.putBoolean("type", true);
        nbtCompound2.putBoolean("type", false);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound1);
        OrganizablePlayScreens.updateEntryNbt(nbtCompound2);
        System.out.println(nbtCompound1);
        System.out.println(nbtCompound2);
    }
}
