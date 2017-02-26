package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.TaskSelector;
import stoneframe.chorelist.model.ToDoList;

public class TodoListToJsonConverter {

    public static String convertToJson(ToDoList todoList) {
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

        return gson.toJson(todoList);
    }

    public static ToDoList convertFromJson(String json,
                                           TaskSelectorConverter taskSelectorConverter,
                                           EffortTrackerConverter effortTrackerConverter) {
        GsonBuilder gsonBuilder = Converters.registerDateTime(new GsonBuilder());

        gsonBuilder.registerTypeAdapter(TaskSelector.class, taskSelectorConverter);
        gsonBuilder.registerTypeAdapter(EffortTracker.class, effortTrackerConverter);

        Gson gson = gsonBuilder.create();

        return gson.fromJson(json, ToDoList.class);
    }

}
