package stoneframe.serena.model.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.model.storages.UpgradeScript;

public class UpgradeScriptVersion20 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 20;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject noteContainer = new JSONObject();

        noteContainer.put("notes", new JSONArray());

        jsonObject.put("NoteContainer", noteContainer);

        return jsonObject;
    }
}
