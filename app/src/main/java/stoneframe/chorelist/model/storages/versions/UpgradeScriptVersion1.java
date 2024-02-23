package stoneframe.chorelist.model.storages.versions;

import org.json.JSONObject;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion1 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 1;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject)
    {
        return jsonObject;
    }
}
