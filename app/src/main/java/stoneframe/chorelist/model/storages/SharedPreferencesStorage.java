package stoneframe.chorelist.model.storages;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import stoneframe.chorelist.json.ChoreSelectorConverter;
import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.EffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Storage;

public class SharedPreferencesStorage implements Storage
{
    private static final String SAVE_NAME = "DATA";

    private final SharedPreferences sharedPreferences;

    private final ChoreSelectorConverter choreSelectorConverter;
    private final EffortTrackerConverter effortTrackerConverter;

    public SharedPreferencesStorage(
        Context context,
        ChoreSelectorConverter choreSelectorConverter,
        EffortTrackerConverter effortTrackerConverter)
    {
        this.sharedPreferences = context.getSharedPreferences(SAVE_NAME, 0);
        this.choreSelectorConverter = choreSelectorConverter;
        this.effortTrackerConverter = effortTrackerConverter;
    }

    @Override
    public Container load()
    {
        String json = sharedPreferences.getString(SAVE_NAME, null);

        if (json == null)
        {
            return null;
        }

        return ContainerJsonConverter.fromJson(
            json,
            choreSelectorConverter,
            effortTrackerConverter);
    }

    @Override
    public void save(@NonNull Container container)
    {
        String json = ContainerJsonConverter.toJson(container);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SAVE_NAME, json);

        editor.apply();
    }
}
