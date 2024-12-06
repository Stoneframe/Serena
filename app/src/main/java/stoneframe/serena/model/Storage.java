package stoneframe.serena.model;

import androidx.annotation.Nullable;

public interface Storage
{
    @Nullable
    Container load();

    void save(@Nullable Container container);

    int getCurrentVersion();
}