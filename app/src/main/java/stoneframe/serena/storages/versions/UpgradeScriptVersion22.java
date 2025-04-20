package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion22 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 22;
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
                continue;
            }

            invertAmountInTransactionTypes(data);
        }

        return jsonObject;
    }

    private boolean isEnhancer(JSONObject data) throws JSONException
    {
        return data.getInt("changePerInterval") < 0;
    }

    private static void invertAmountInTransactionTypes(JSONObject data) throws JSONException
    {
        JSONArray transactionTypes = data.getJSONArray("transactionTypes");

        for (int i = 0; i < transactionTypes.length(); i++)
        {
            JSONObject transactionType = transactionTypes.getJSONObject(i);

            int amount = transactionType.getInt("amount");

            transactionType.put("amount", -amount);
        }
    }
}
