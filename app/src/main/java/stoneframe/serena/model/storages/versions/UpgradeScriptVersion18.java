package stoneframe.serena.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion18 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 18;
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

            invertAmountInTransactionTypes(data);
            invertAmoutInTransactions(data);
        }

        return jsonObject;
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

    private static void invertAmoutInTransactions(JSONObject data) throws JSONException
    {
        JSONArray transactions = data.getJSONArray("transactions");

        for (int i = 0; i < transactions.length(); i++)
        {
            JSONObject transaction = transactions.getJSONObject(i);

            JSONObject first = transaction.getJSONObject("first");

            int amount = first.getInt("amount");

            first.put("amount", -amount);
        }
    }
}
