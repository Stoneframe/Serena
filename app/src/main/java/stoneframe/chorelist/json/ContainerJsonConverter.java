package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.chores.ChoreSelector;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.chores.EffortTracker;
import stoneframe.chorelist.model.routines.Routine;

public class ContainerJsonConverter
{
    public static String toJson(Container container)
    {
        return Converters.registerAll(new GsonBuilder())
            .create()
            .toJson(container);
    }

    public static Container fromJson(
        String json,
        ChoreSelectorConverter choreSelectorConverter,
        EffortTrackerConverter effortTrackerConverter)
    {
        return Converters.registerAll(new GsonBuilder())
            .registerTypeAdapter(ChoreSelector.class, choreSelectorConverter)
            .registerTypeAdapter(EffortTracker.class, effortTrackerConverter)
            .registerTypeAdapter(Routine.class, new RoutineConverter())
            .create()
            .fromJson(json, Container.class);
    }
}
