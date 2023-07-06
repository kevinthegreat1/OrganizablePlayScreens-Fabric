package com.kevinthegreat.organizableplayscreens.gui;

import org.apache.commons.lang3.mutable.Mutable;

import java.util.function.Supplier;

public interface AbstractEntry extends Mutable<String>, Supplier<EntryType> {
    @Override
    default String getValue() {
        return getName();
    }

    String getName();

    @Override
    default void setValue(String s) {
        setName(s);
    }

    void setName(String s);

    @Override
    default EntryType get() {
        return getType();
    }

    EntryType getType();
}
