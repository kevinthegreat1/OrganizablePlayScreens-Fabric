package com.kevinthegreat.organizableplayscreens.option;

import com.kevinthegreat.organizableplayscreens.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;

/**
 * A text field widget for use with {@link SimpleOption} similar to {@link SimpleOption.OptionSliderWidgetImpl}.
 *
 * @param <N> the type of number for the option
 */
@SuppressWarnings("JavadocReference")
public abstract class OptionTextFieldWidgetImpl<N extends Number> extends TextFieldWidget {
    protected final SimpleOption<N> option;
    protected final SimpleOption.SliderCallbacks<N> callbacks;
    protected final SimpleOption.TooltipFactory<N> tooltipFactory;

    public OptionTextFieldWidgetImpl(int x, int y, int width, int height, SimpleOption<N> option, SimpleOption.SliderCallbacks<N> callbacks, SimpleOption.TooltipFactory<N> tooltipFactory) {
        super(MinecraftClient.getInstance().textRenderer, x, y, width, height, ((SimpleOptionAccessor) (Object) option).getTextGetter().apply(option.getValue()));
        this.option = option;
        this.callbacks = callbacks;
        this.tooltipFactory = tooltipFactory;
        setTextPredicate(this::isValid);
        setChangedListener(string -> {
            if (string.isEmpty()) {
                string = "0";
                setText(getDisplayValueString());
            }
            N value = parseValue(string);
            if (value.equals(option.getValue())) {
                return;
            }
            option.setValue(value);
            setText(getDisplayValueString());
            setMessage(((SimpleOptionAccessor) (Object) option).getTextGetter().apply(option.getValue()));
            setTooltip(tooltipFactory.apply(option.getValue()));
        });
        setText(getDisplayValueString());
    }

    public abstract @NotNull String getDisplayValueString();

    public abstract boolean isValid(String value);

    public abstract @NotNull N parseValue(String value);
}
