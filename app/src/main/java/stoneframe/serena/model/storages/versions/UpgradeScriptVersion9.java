package stoneframe.serena.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion9 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 9;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject choreManager = jsonObject.getJSONObject("ChoreManager");

        JSONArray chores = choreManager.getJSONArray("chores");

        for (int i = 0; i < chores.length(); i++)
        {
            JSONObject chore = chores.getJSONObject(i);

            chore.put("isEnabled", true);
        }

        return jsonObject;
    }
}
