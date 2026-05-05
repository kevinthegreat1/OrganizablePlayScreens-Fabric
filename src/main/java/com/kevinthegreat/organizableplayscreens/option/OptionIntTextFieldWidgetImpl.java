package com.kevinthegreat.organizableplayscreens.option;

import net.minecraft.client.OptionInstance;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link OptionTextFieldWidgetImpl} implementation for use with integer {@link OptionInstance} and
 * {@link BothSuppliableIntSliderCallbacks} similar to {@link OptionInstance.OptionInstanceSliderButton}.
 */
@SuppressWarnings("JavadocReference")
public class OptionIntTextFieldWidgetImpl extends OptionTextFieldWidgetImpl<Integer> {
    public OptionIntTextFieldWidgetImpl(int x, int y, int width, int height, OptionInstance<Integer> option, BothSuppliableIntSliderCallbacks callbacks, OptionInstance.TooltipSupplier<Integer> tooltipFactory) {
        super(x, y, width, height, option, callbacks, tooltipFactory);
    }

    @Override
    public @NotNull String getDisplayValueString() {
        return String.valueOf(((BothSuppliableIntSliderCallbacks) callbacks).displayValueGetter().apply(option.get()));
    }

    @Override
    public boolean isValid(String string) {
        if (string.isEmpty()) {
            return true;
        }
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @NotNull Integer parseValue(String string) {
        return ((BothSuppliableIntSliderCallbacks) callbacks).displayValueParser().apply(string);
    }
}
