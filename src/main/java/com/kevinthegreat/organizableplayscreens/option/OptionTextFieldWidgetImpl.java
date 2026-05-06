package com.kevinthegreat.organizableplayscreens.option;

import com.kevinthegreat.organizableplayscreens.mixin.accessor.OptionInstanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.input.CharacterEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A text field widget for use with {@link OptionInstance} similar to {@link OptionInstance.OptionInstanceSliderButton}.
 *
 * @param <N> the type of number for the option
 */
public abstract class OptionTextFieldWidgetImpl<N extends Number> extends EditBox {
    protected final OptionInstance<N> option;
    protected final OptionInstance.SliderableValueSet<N> callbacks;
    protected final OptionInstance.TooltipSupplier<N> tooltipFactory;

    public OptionTextFieldWidgetImpl(int x, int y, int width, int height, OptionInstance<N> option, OptionInstance.SliderableValueSet<N> callbacks, OptionInstance.TooltipSupplier<N> tooltipFactory) {
        super(Minecraft.getInstance().font, x, y, width, height, ((OptionInstanceAccessor) (Object) option).getToString().apply(option.get()));
        this.option = option;
        this.callbacks = callbacks;
        this.tooltipFactory = tooltipFactory;
        setResponder(string -> {
            if (string.isEmpty()) {
                string = "0";
                setValue(getDisplayValueString());
            }
            N value = parseValue(string);
            if (value.equals(option.get())) {
                return;
            }
            option.set(value);
            setValue(getDisplayValueString());
            setMessage(((OptionInstanceAccessor) (Object) option).getToString().apply(option.get()));
            setTooltip(tooltipFactory.apply(option.get()));
        });
        setValue(getDisplayValueString());
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return isValid(event.codepointAsString()) && super.charTyped(event);
    }

    public abstract @NotNull String getDisplayValueString();

    public abstract boolean isValid(String value);

    public abstract @NotNull N parseValue(String value);
}
