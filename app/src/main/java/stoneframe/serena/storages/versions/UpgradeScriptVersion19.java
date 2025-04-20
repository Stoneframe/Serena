package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.json.JsonUtils;
import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion19 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 19;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject balancerContainer = jsonObject.getJSONObject("BalancerContainer");

        JSONArray balancers = balancerContainer.getJSONArray("balancers");

        for (int i = 0; i < balancers.length(); i++)
        {
            JSONObject balancer = balancers.getJSONObject(i);

            JSONObject data = balancer.getJSONObject("data");

            JsonUtils.changePropertyName(data, "changePerDay", "changePerInterval");

            data.put("intervalType", 0);
        }

        return jsonObject;
    }
}
