package stoneframe.serena.model.storages.versions;

import static stoneframe.serena.json.JsonUtils.removeAllAttributes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion14 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 14;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject limiterContainer = jsonObject.getJSONObject("LimiterContainer");

        JSONArray limiters = limiterContainer.getJSONArray("limiters");

        for (int i = 0; i < limiters.length(); i++)
        {
            JSONObject limiter = limiters.getJSONObject(i);

            JSONObject data = new JSONObject(limiter.toString());

            removeAllAttributes(limiter);

            limiter.put("data", data);
        }

        return jsonObject;
    }
}
