package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion7 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 7;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject limiterManager = jsonObject.getJSONObject("LimiterManager");

        JSONArray limiters = limiterManager.getJSONArray("limiters");

        for (int i = 0; i < limiters.length(); i++)
        {
            JSONObject limiter = limiters.getJSONObject(i);

            JSONArray expenditures = limiter.getJSONArray("expenditures");

            for (int j = 0; j < expenditures.length(); j++)
            {
                JSONObject expenditure = expenditures.getJSONObject(j);

                String name = expenditure.getString("name");
                int amount = expenditure.getInt("amount");
                String time = expenditure.getString("time");

                expenditure.remove("name");
                expenditure.remove("amount");
                expenditure.remove("time");

                JSONObject first = new JSONObject()
                    .put("name", name)
                    .put("amount", amount);

                expenditure.put("first", first);
                expenditure.put("second", time);
            }
        }

        return jsonObject;
    }
}
