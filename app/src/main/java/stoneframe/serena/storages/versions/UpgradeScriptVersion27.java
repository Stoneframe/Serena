package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion27 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 27;
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

            if (isEnhancer(data))
            {
                data.put("okThreshold", 1);
            }
            else
            {
                data.put("okThreshold", 0);
            }
        }

        return jsonObject;
    }

    private boolean isEnhancer(JSONObject data) throws JSONException
    {
        return data.getInt("changePerInterval") < 0;
    }
}
