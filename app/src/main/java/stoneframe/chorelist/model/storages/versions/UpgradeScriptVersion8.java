package stoneframe.chorelist.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion8 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 8;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject limiterManager = jsonObject.getJSONObject("LimiterManager");

        JSONArray limiters = limiterManager.getJSONArray("limiters");

        for (int i = 0; i < limiters.length(); i++)
        {
            JSONObject limiter = limiters.getJSONObject(i);

            limiter.put("allowQuick", true);
        }

        return jsonObject;
    }
}
