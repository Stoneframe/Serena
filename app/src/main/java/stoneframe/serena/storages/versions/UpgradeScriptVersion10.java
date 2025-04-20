package stoneframe.serena.storages.versions;

import static stoneframe.serena.storages.json.JsonUtils.removeAllAttributes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion10 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 10;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject routineManager = jsonObject.getJSONObject("RoutineManager");

        JSONArray routines = routineManager.getJSONArray("routines");

        for (int i = 0; i < routines.length(); i++)
        {
            JSONObject routine = routines.getJSONObject(i);

            int routineType = routine.getInt("routineType");

            JSONObject data = new JSONObject(routine.toString());

            data.remove("routineType");

            removeAllAttributes(routine);

            routine.put("routineType", routineType);
            routine.put("data", data);
        }

        return jsonObject;
    }
}
