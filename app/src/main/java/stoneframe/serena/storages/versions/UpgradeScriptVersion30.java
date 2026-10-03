package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion30 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 30;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject taskContainer = jsonObject.getJSONObject("TaskContainer");

        taskContainer.put("smsTaskCreationEnabled", false);
        taskContainer.put("trustedSmsSenders", new JSONArray());

        return jsonObject;
    }
}
