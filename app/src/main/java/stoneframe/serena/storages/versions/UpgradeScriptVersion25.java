package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion25 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 25;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject sleepContainer = jsonObject.getJSONObject("SleepContainer");

        JSONObject sleep = sleepContainer.getJSONObject("sleep");

        JSONArray sleepSessions = sleep.getJSONArray("sleepSessions");

        sleep.put("isEnabled", sleepSessions.length() > 0);

        return jsonObject;
    }
}
