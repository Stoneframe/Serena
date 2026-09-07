package stoneframe.serena.storages.versions;

import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion29 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 29;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject taskContainer = jsonObject.getJSONObject("TaskContainer");

        taskContainer.put("maximumNumberOfTasksPerDay", JSONObject.NULL);
        taskContainer.put("numberOfTasksCompletedToday", 0);
        taskContainer.put("taskCompletionCountDate", JSONObject.NULL);

        return jsonObject;
    }
}
