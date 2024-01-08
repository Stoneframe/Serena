package stoneframe.chorelist.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Storage
{
    @Nullable
    Container load();

    void save(@NonNull Container container);
}
