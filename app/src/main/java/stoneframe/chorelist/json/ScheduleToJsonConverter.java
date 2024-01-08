package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.ChoreSelector;

public class ScheduleToJsonConverter
{
    public static String convertToJson(ChoreManager choreManager)
    {
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

        return gson.toJson(choreManager);
    }

    public static ChoreManager convertFromJson(
        String json,
        EffortTrackerConverter effortTrackerConverter, ChoreSelectorConverter choreSelectorConverter)
    {
        GsonBuilder gsonBuilder = Converters.registerDateTime(new GsonBuilder());

        gsonBuilder.registerTypeAdapter(ChoreSelector.class, choreSelectorConverter);
        gsonBuilder.registerTypeAdapter(EffortTracker.class, effortTrackerConverter);

        Gson gson = gsonBuilder.create();

        return gson.fromJson(json, ChoreManager.class);
    }
}
