package stoneframe.serena.model.storages.versions;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion1 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 1;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject taskManager = jsonObject.getJSONObject("TaskManager");

        JSONArray tasks = taskManager.getJSONArray("tasks");

        DateTime completed = DateTime.now().withTimeAtStartOfDay();

        String completedJson = Converters.registerDateTime(new GsonBuilder())
            .create()
            .toJson(completed)
            .replace("\"", "");

        for (int i = 0; i < tasks.length(); i++)
        {
            JSONObject task = tasks.getJSONObject(i);

            if (task.getBoolean("isDone"))
            {
                task.put("completed", completedJson);
            }
        }

        return jsonObject;
    }
}
