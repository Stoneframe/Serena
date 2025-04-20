package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion17 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 17;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject balancerContainer = jsonObject.getJSONObject("BalancerContainer");

        JSONArray balancers = balancerContainer.getJSONArray("balancers");

        for (int i = 0; i < balancers.length(); i++)
        {
            JSONObject balancersJSONObject = balancers.getJSONObject(i);

            JSONObject data = balancersJSONObject.getJSONObject("data");

            data.put("isEnabled", true);
        }

        return jsonObject;
    }
}
