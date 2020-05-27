package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.TaskSelector;

public class ScheduleToJsonConverter
{

    public static String convertToJson(Schedule schedule)
    {
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

        return gson.toJson(schedule);
    }

    public static Schedule convertFromJson(
        String json,
        EffortTrackerConverter effortTrackerConverter, TaskSelectorConverter taskSelectorConverter)
    {
        GsonBuilder gsonBuilder = Converters.registerDateTime(new GsonBuilder());

        gsonBuilder.registerTypeAdapter(TaskSelector.class, taskSelectorConverter);
        gsonBuilder.registerTypeAdapter(EffortTracker.class, effortTrackerConverter);

        Gson gson = gsonBuilder.create();

        return gson.fromJson(json, Schedule.class);
    }

}
