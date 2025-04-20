package stoneframe.serena.storages.versions;

import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.serena.storages.UpgradeScript;

public class UpgradeScriptVersion12 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 12;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        renameManagerToContainer(jsonObject, "ChoreManager", "ChoreContainer");
        renameManagerToContainer(jsonObject, "TaskManager", "TaskContainer");
        renameManagerToContainer(jsonObject, "RoutineManager", "RoutineContainer");
        renameManagerToContainer(jsonObject, "ChecklistManager", "ChecklistContainer");
        renameManagerToContainer(jsonObject, "LimiterManager", "LimiterContainer");

        return jsonObject;
    }

    private static void renameManagerToContainer(
        JSONObject jsonObject,
        String managerName,
        String containerName) throws JSONException
    {
        JSONObject manager = jsonObject.getJSONObject(managerName);
        jsonObject.remove(managerName);
        jsonObject.put(containerName, manager);
    }
}
