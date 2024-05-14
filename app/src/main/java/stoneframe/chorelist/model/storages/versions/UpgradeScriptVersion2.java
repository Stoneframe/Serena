package stoneframe.chorelist.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion2 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 2;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject checklistManager = new JSONObject();

        checklistManager.put("checklists", new JSONArray());

        jsonObject.put("ChecklistManager", checklistManager);

        return jsonObject;
    }
}
