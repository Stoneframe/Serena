package stoneframe.serena.storages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.serena.storages.json.ChoreSelectorConverter;
import stoneframe.serena.storages.json.ContainerJsonConverter;
import stoneframe.serena.storages.json.EffortTrackerConverter;
import stoneframe.serena.Container;
import stoneframe.serena.storages.versions.UpgradeScriptVersion1;
import stoneframe.serena.storages.versions.UpgradeScriptVersion10;
import stoneframe.serena.storages.versions.UpgradeScriptVersion11;
import stoneframe.serena.storages.versions.UpgradeScriptVersion12;
import stoneframe.serena.storages.versions.UpgradeScriptVersion13;
import stoneframe.serena.storages.versions.UpgradeScriptVersion14;
import stoneframe.serena.storages.versions.UpgradeScriptVersion15;
import stoneframe.serena.storages.versions.UpgradeScriptVersion16;
import stoneframe.serena.storages.versions.UpgradeScriptVersion17;
import stoneframe.serena.storages.versions.UpgradeScriptVersion18;
import stoneframe.serena.storages.versions.UpgradeScriptVersion19;
import stoneframe.serena.storages.versions.UpgradeScriptVersion2;
import stoneframe.serena.storages.versions.UpgradeScriptVersion20;
import stoneframe.serena.storages.versions.UpgradeScriptVersion21;
import stoneframe.serena.storages.versions.UpgradeScriptVersion22;
import stoneframe.serena.storages.versions.UpgradeScriptVersion23;
import stoneframe.serena.storages.versions.UpgradeScriptVersion24;
import stoneframe.serena.storages.versions.UpgradeScriptVersion25;
import stoneframe.serena.storages.versions.UpgradeScriptVersion3;
import stoneframe.serena.storages.versions.UpgradeScriptVersion4;
import stoneframe.serena.storages.versions.UpgradeScriptVersion5;
import stoneframe.serena.storages.versions.UpgradeScriptVersion6;
import stoneframe.serena.storages.versions.UpgradeScriptVersion7;
import stoneframe.serena.storages.versions.UpgradeScriptVersion8;
import stoneframe.serena.storages.versions.UpgradeScriptVersion9;

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
            new UpgradeScriptVersion15(),
            new UpgradeScriptVersion16(),
            new UpgradeScriptVersion17(),
            new UpgradeScriptVersion18(),
            new UpgradeScriptVersion19(),
            new UpgradeScriptVersion20(),
            new UpgradeScriptVersion21(),
            new UpgradeScriptVersion22(),
            new UpgradeScriptVersion23(),
            new UpgradeScriptVersion24(),
            new UpgradeScriptVersion25())
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
