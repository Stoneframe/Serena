package stoneframe.serena.model.storages.versions;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion4 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 4;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject caloriesManager = new JSONObject();

        caloriesManager.put("consumptionTypes", new JSONArray());
        caloriesManager.put("consumptions", new JSONArray());
        caloriesManager.put("incrementPerDay", 250);
        caloriesManager.put("previousConsumption", 0);
        caloriesManager.put("startDate", LocalDate.now().toString("yyyy-MM-dd"));

        jsonObject.put("CaloriesManager", caloriesManager);

        return jsonObject;
    }
}
