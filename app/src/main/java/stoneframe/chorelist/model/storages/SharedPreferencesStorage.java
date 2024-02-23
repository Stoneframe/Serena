package stoneframe.chorelist.model.storages;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONException;

import stoneframe.chorelist.json.ChoreSelectorConverter;
import stoneframe.chorelist.json.EffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Storage;

public class SharedPreferencesStorage implements Storage
{
    private static final String SAVE_NAME = "DATA";

    private final SharedPreferences sharedPreferences;

    private final JsonConverter jsonConverter;

    public SharedPreferencesStorage(
        Context context,
        JsonConverter jsonConverter)
    {
        this.sharedPreferences = context.getSharedPreferences(SAVE_NAME, 0);
        this.jsonConverter = jsonConverter;
    }

    @Override
    public Container load()
    {
        try
        {
            String json = sharedPreferences.getString(SAVE_NAME, null);

            if (json == null)
            {
                return null;
            }

            return jsonConverter.fromJson(json);
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(@NonNull Container container)
    {
        try
        {
            String json = jsonConverter.toJson(container);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(SAVE_NAME, json);

            editor.apply();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }
}
