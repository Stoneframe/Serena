package stoneframe.serena.gui.util;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Consumer;

public class TextChangedListener implements TextWatcher
{
    private final Consumer<String> consumer;

    public TextChangedListener(Consumer<String> consumer)
    {
        this.consumer = consumer;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        consumer.accept(editable.toString());
    }
}
