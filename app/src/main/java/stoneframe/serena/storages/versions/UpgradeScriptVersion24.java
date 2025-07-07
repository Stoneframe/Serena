package stoneframe.serena.storages.versions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion24 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 24;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        JSONObject noteContainer = jsonObject.getJSONObject("NoteContainer");

        noteContainer.put("groups", new JSONArray());

        JSONArray notes = noteContainer.getJSONArray("notes");

        for (int i = 0; i < notes.length(); i++)
        {
            JSONObject limiter = notes.getJSONObject(i);

            JSONObject data = limiter.getJSONObject("data");

            data.put("groupId", "00000000-0000-0000-0000-000000000000");
        }

        return jsonObject;
    }
}
