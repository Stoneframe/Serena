package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion6 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 6;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject caloriesManager = (JSONObject)jsonObject.remove("CaloriesManager");

        assert caloriesManager != null;

        caloriesManager.put("name", "Calories");
        caloriesManager.put("unit", "kcal");

        replaceKey(caloriesManager, "previousConsumption", "previousExpenditure");

        replaceKey(caloriesManager, "consumptionTypes", "expenditureTypes");
        replaceKey(caloriesManager, "consumptions", "expenditures");

        replaceKeyInArray(caloriesManager, "expenditureTypes", "calories", "amount");
        replaceKeyInArray(caloriesManager, "expenditures", "calories", "amount");

        JSONArray limiters = new JSONArray().put(caloriesManager);

        JSONObject limiterManager = new JSONObject().put("limiters", limiters);

        jsonObject.put("LimiterManager", limiterManager);

        return jsonObject;
    }

    private static void replaceKeyInArray(
        JSONObject jsonObject,
        String arrayName,
        String oldKey,
        String newKey) throws JSONException
    {
        JSONArray jsonArray = jsonObject.getJSONArray(arrayName);

        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject element = jsonArray.getJSONObject(i);

            replaceKey(element, oldKey, newKey);
        }
    }

    private static void replaceKey(
        JSONObject caloriesManager,
        String oldKey,
        String newKey) throws JSONException
    {
        caloriesManager.put(newKey, caloriesManager.get(oldKey));
        caloriesManager.remove(oldKey);
    }
}
