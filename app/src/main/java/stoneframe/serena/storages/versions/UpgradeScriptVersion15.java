package stoneframe.serena.storages.versions;

import static stoneframe.serena.storages.json.JsonUtils.removeAllAttributes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion15 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 15;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject taskContainer = jsonObject.getJSONObject("TaskContainer");

        JSONArray tasks = taskContainer.getJSONArray("tasks");

        for (int i = 0; i < tasks.length(); i++)
        {
            JSONObject task = tasks.getJSONObject(i);

            JSONObject data = new JSONObject(task.toString());

            removeAllAttributes(task);

            task.put("data", data);
        }

        return jsonObject;
    }
}
