package stoneframe.chorelist.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion3 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 3;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject routineManager = jsonObject.getJSONObject("RoutineManager");

        JSONArray routines = routineManager.getJSONArray("routines");

        for (int i = 0; i < routines.length(); i++)
        {
            JSONObject routine = routines.getJSONObject(i);

            routine.put("isEnabled", true);
        }

        return jsonObject;
    }
}
