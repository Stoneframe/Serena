package stoneframe.chorelist.json;

import androidx.annotation.NonNull;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.EffortTracker;

public class ContainerJsonConverter
{
    public static String toJson(Container container)
    {
        return Converters.registerDateTime(new GsonBuilder())
            .create()
            .toJson(container);
    }

    public static Container fromJson(
        String json,
        ChoreSelectorConverter choreSelectorConverter,
        EffortTrackerConverter effortTrackerConverter)
    {
        return Converters.registerDateTime(new GsonBuilder())
            .registerTypeAdapter(ChoreSelector.class, choreSelectorConverter)
            .registerTypeAdapter(EffortTracker.class, effortTrackerConverter)
            .create()
            .fromJson(json, Container.class);
    }

}
