package stoneframe.serena.storages.versions;

import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion23 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 23;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject sleepContainer = new JSONObject();

        sleepContainer.put("startDateTime", LocalDateTime.now());
        sleepContainer.put("state", 1);
        sleepContainer.put("sleepSessions", new JSONArray());

        jsonObject.put("SleepContainer", sleepContainer);

        return jsonObject;
    }
}
