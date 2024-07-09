package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;

import java.io.IOException;

import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.EffortTracker;

public class ChoreManagerToJsonConverter
{
    public static String convertToJson(ChoreManager choreManager)
    {
        GsonBuilder builder = new GsonBuilder();

        Converters.registerLocalDate(builder);
        Converters.registerLocalTime(builder);
        Converters.registerLocalDateTime(builder);

        Gson gson = builder.create();

        return gson.toJson(choreManager);
    }

    public static ChoreManager convertFromJson(
        String json,
        EffortTrackerConverter effortTrackerConverter,
        ChoreSelectorConverter choreSelectorConverter)
    {
        GsonBuilder builder = new GsonBuilder();

        Converters.registerLocalDate(builder);
        Converters.registerLocalTime(builder);
        Converters.registerLocalDateTime(builder);

        builder.registerTypeAdapter(ChoreSelector.class, choreSelectorConverter);
        builder.registerTypeAdapter(EffortTracker.class, effortTrackerConverter);

        Gson gson = builder.create();

        return gson.fromJson(json, ChoreManager.class);
    }
}
