package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion21 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 21;
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

            int sumOfRecentTransactions = getSumOfRecentTransactions(data);

            int previousTransactions = data.getInt("previousTransactions");

            previousTransactions += sumOfRecentTransactions;

            data.put("previousTransactions", previousTransactions);

            data.remove("transactions");
        }

        return jsonObject;
    }

    private static int getSumOfRecentTransactions(JSONObject data) throws JSONException
    {
        int sumOfRecentTransactions = 0;

        JSONArray recentTransactions = data.getJSONArray("transactions");

        for (int j = 0; j < recentTransactions.length(); j++)
        {
            JSONObject recentTransaction = recentTransactions.getJSONObject(j);

            JSONObject first = recentTransaction.getJSONObject("first");

            sumOfRecentTransactions += first.getInt("amount");
        }

        return sumOfRecentTransactions;
    }
}
