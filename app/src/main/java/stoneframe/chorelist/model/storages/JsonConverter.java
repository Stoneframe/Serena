package stoneframe.chorelist.model.storages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.chorelist.json.ChoreSelectorConverter;
import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.EffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.storages.versions.*;

public class JsonConverter
{
    private static final String VERSION = "Version";

    private final Map<Integer, UpgradeScript> upgradeScripts = Stream.of(
            new UpgradeScriptVersion1(),
            new UpgradeScriptVersion2(),
            new UpgradeScriptVersion3(),
            new UpgradeScriptVersion4(),
            new UpgradeScriptVersion5(),
            new UpgradeScriptVersion6(),
            new UpgradeScriptVersion7(),
            new UpgradeScriptVersion8(),
            new UpgradeScriptVersion9(),
            new UpgradeScriptVersion10(),
            new UpgradeScriptVersion11(),
            new UpgradeScriptVersion12(),
            new UpgradeScriptVersion13(),
            new UpgradeScriptVersion14(),
            new UpgradeScriptVersion15())
        .collect(Collectors.toMap(UpgradeScript::getVersion, s -> s));

    private final ChoreSelectorConverter choreSelectorConverter;
    private final EffortTrackerConverter effortTrackerConverter;

    public JsonConverter(
        ChoreSelectorConverter choreSelectorConverter,
        EffortTrackerConverter effortTrackerConverter)
    {
        this.choreSelectorConverter = choreSelectorConverter;
        this.effortTrackerConverter = effortTrackerConverter;
    }

    public Container fromJson(String json) throws JSONException
    {
        return ContainerJsonConverter.fromJson(
            upgrade(json),
            choreSelectorConverter,
            effortTrackerConverter);
    }

    public String toJson(Container container) throws JSONException
    {
        String json = ContainerJsonConverter.toJson(container);

        JSONObject jsonObject = new JSONObject(json);

        return jsonObject.toString();
    }

    private String upgrade(String json) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);

        int jsonVersion = getJsonVersion(jsonObject);
        int currentVersion = getCurrentVersion();

        for (int version = jsonVersion + 1; version <= currentVersion; version++)
        {
            UpgradeScript upgradeScript = upgradeScripts.get(version);

            assert upgradeScript != null;

            jsonObject = upgradeScript.upgrade(jsonObject);
            jsonObject.put(VERSION, upgradeScript.getVersion());
        }

        return jsonObject.toString();
    }

    private static int getJsonVersion(JSONObject jsonObject) throws JSONException
    {
        return jsonObject.has(VERSION) ? jsonObject.getInt(VERSION) : 0;
    }

    int getCurrentVersion()
    {
        return upgradeScripts.keySet().stream().max(Integer::compareTo).get();
    }
}
