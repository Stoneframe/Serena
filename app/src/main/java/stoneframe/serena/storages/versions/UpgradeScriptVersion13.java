package stoneframe.serena.storages.versions;

import static stoneframe.serena.storages.json.JsonUtils.removeAllAttributes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion13 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 13;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject choreContainer = jsonObject.getJSONObject("ChoreContainer");

        JSONArray chores = choreContainer.getJSONArray("chores");

        for (int i = 0; i < chores.length(); i++)
        {
            JSONObject chore = chores.getJSONObject(i);

            JSONObject data = new JSONObject(chore.toString());

            removeAllAttributes(chore);

            chore.put("data", data);
        }

        return jsonObject;
    }
}
