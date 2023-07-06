package com.kevinthegreat.organizableplayscreens.gui;

import com.kevinthegreat.organizableplayscreens.OrganizablePlayScreens;
import net.minecraft.text.Text;

public class EntryType {
    public static final EntryType FOLDER = new EntryType(OrganizablePlayScreens.MOD_ID + ":folder", OrganizablePlayScreens.MOD_ID + ":folder.folder");
    public static final EntryType SECTION = new EntryType(OrganizablePlayScreens.MOD_ID + ":section", OrganizablePlayScreens.MOD_ID + ":entry.section");
    public static final EntryType SEPARATOR = new EntryType(OrganizablePlayScreens.MOD_ID + ":separator", OrganizablePlayScreens.MOD_ID + ":entry.separator");
    private final String id;
    private final Text text;

    public String id() {
        return id;
    }

    public Text text() {
        return text;
    }

    public EntryType(String id, String key) {
        this.id = id;
        this.text = Text.translatable(key);
    }
}
