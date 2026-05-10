package stoneframe.serena.storages.versions;

import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion28 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 28;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject sleepContainer = jsonObject.getJSONObject("SleepContainer");

        JSONObject sleep = sleepContainer.getJSONObject("sleep");

        sleep.put("minHoursSleepPerDay", 7d);
        sleep.put("maxHoursSleepPerDay", 8d);

        return jsonObject;
    }
}
