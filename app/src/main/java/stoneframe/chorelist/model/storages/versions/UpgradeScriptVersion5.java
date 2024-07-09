package stoneframe.chorelist.model.storages.versions;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stoneframe.chorelist.model.storages.UpgradeScript;

public class UpgradeScriptVersion5 implements UpgradeScript
{
    @Override
    public int getVersion()
    {
        return 5;
    }

    @Override
    public JSONObject upgrade(JSONObject jsonObject) throws JSONException
    {
        upgradeRoutines(jsonObject);
        upgradeChores(jsonObject);
        upgradeTasks(jsonObject);

        return jsonObject;
    }

    private void upgradeRoutines(JSONObject jsonObject) throws JSONException
    {
        JSONObject routineManager = jsonObject.getJSONObject("RoutineManager");

        JSONArray routines = routineManager.getJSONArray("routines");

        for (int i = 0; i < routines.length(); i++)
        {
            JSONObject routine = routines.getJSONObject(i);

            updateDateTimeToLocalDateTime(routine, "lastCompleted");
        }
    }

    private void upgradeChores(JSONObject jsonObject) throws JSONException
    {
        JSONObject choreManager = jsonObject.getJSONObject("ChoreManager");

        JSONArray chores = choreManager.getJSONArray("chores");

        for (int i = 0; i < chores.length(); i++)
        {
            JSONObject chore = chores.getJSONObject(i);

            updateDateTimeToLocalDate(chore, "next");
            updateDateTimeToLocalDate(chore, "postpone");
        }

        JSONObject effortTracker = choreManager.getJSONObject("effortTracker");

        updateDateTimeToLocalDate(effortTracker, "previous");
    }

    private void upgradeTasks(JSONObject jsonObject) throws JSONException
    {
        JSONObject taskManager = jsonObject.getJSONObject("TaskManager");

        JSONArray tasks = taskManager.getJSONArray("tasks");

        for (int i = 0; i < tasks.length(); i++)
        {
            JSONObject task = tasks.getJSONObject(i);

            updateDateTimeToLocalDate(task, "deadline");
            updateDateTimeToLocalDate(task, "ignoreBefore");
            updateDateTimeToLocalDate(task, "completed");
        }
    }

    private static void updateDateTimeToLocalDateTime(
        JSONObject jsonObject,
        String property) throws JSONException
    {
        if (!jsonObject.has(property)) return;

        DateTime dateTime = DateTime.parse(jsonObject.getString(property));

        jsonObject.put(property, dateTime.toLocalDateTime());
    }

    private void updateDateTimeToLocalDate(
        JSONObject jsonObject,
        String property) throws JSONException
    {
        if (!jsonObject.has(property)) return;

        DateTime dateTime = DateTime.parse(jsonObject.getString(property));

        jsonObject.put(property, dateTime.toLocalDate());
    }
}
