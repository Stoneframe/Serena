package stoneframe.chorelist.model.storages.versions;

import static stoneframe.chorelist.json.JsonUtils.removeAllAttributes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion11 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 11;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject checklistManager = jsonObject.getJSONObject("ChecklistManager");

        JSONArray checklists = checklistManager.getJSONArray("checklists");

        for (int i = 0; i < checklists.length(); i++)
        {
            JSONObject checklist = checklists.getJSONObject(i);

            JSONObject data = new JSONObject(checklist.toString());

            removeAllAttributes(checklist);

            checklist.put("data", data);
        }

        return jsonObject;
    }
}
