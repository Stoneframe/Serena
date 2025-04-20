package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion16 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 16;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject limiterContainer = jsonObject.getJSONObject("LimiterContainer");

        JSONArray limiters = limiterContainer.getJSONArray("limiters");

        for (int i = 0; i < limiters.length(); i++)
        {
            JSONObject limiter = limiters.getJSONObject(i);

            JSONObject data = limiter.getJSONObject("data");

            updateExpenditureTypes(data);
            updateExpenditures(data);
            updatePreviousExpenditure(data);
            updateIncrementPerDay(data);
        }

        renameArray(limiterContainer, "limiters", "balancers");
        renameObject(jsonObject, "LimiterContainer", "BalancerContainer");

        return jsonObject;
    }

    private static void updateExpenditureTypes(JSONObject data) throws JSONException
    {
        JSONArray expenditureTypes = data.getJSONArray("expenditureTypes");

        for (int i = 0; i < expenditureTypes.length(); i++)
        {
            JSONObject expenditureType = expenditureTypes.getJSONObject(i);

            int amount = expenditureType.getInt("amount");

            expenditureType.put("amount", -amount);
        }

        renameArray(data, "expenditureTypes", "transactionTypes");
    }

    private static void updateExpenditures(JSONObject data) throws JSONException
    {
        JSONArray expenditures = data.getJSONArray("expenditures");

        for (int i = 0; i < expenditures.length(); i++)
        {
            JSONObject expenditure = expenditures.getJSONObject(i);

            JSONObject first = expenditure.getJSONObject("first");

            int amount = first.getInt("amount");

            first.put("amount", -amount);
        }

        renameArray(data, "expenditures", "transactions");
    }

    private static void updatePreviousExpenditure(JSONObject data) throws JSONException
    {
        int previousExpenditure = data.getInt("previousExpenditure");

        data.put("previousExpenditure", -previousExpenditure);

        renameInteger(data, "previousExpenditure", "previousTransactions");
    }

    private static void updateIncrementPerDay(JSONObject data) throws JSONException
    {
        renameInteger(data, "incrementPerDay", "changePerDay");
    }

    private static void renameObject(
        JSONObject jsonObject,
        String oldName,
        String newName) throws JSONException
    {
        JSONObject object = jsonObject.getJSONObject(oldName);
        jsonObject.remove(oldName);
        jsonObject.put(newName, object);
    }

    private static void renameArray(
        JSONObject jsonObject,
        String oldName,
        String newName) throws JSONException
    {
        JSONArray array = jsonObject.getJSONArray(oldName);
        jsonObject.remove(oldName);
        jsonObject.put(newName, array);
    }

    private static void renameInteger(
        JSONObject jsonObject,
        String oldName,
        String newName) throws JSONException
    {
        int integer = jsonObject.getInt(oldName);
        jsonObject.remove(oldName);
        jsonObject.put(newName, integer);
    }
}
