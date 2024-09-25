package stoneframe.serena.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.GsonBuilder;

import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.Container;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.routines.Routine;

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
