package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion26 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 26;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject reminderContainer = new JSONObject();

        reminderContainer.put("reminders", new JSONArray());

        jsonObject.put("ReminderContainer", reminderContainer);

        return jsonObject;
    }
}
