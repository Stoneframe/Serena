package stoneframe.chorelist.model.storages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.chorelist.json.ChoreSelectorConverter;
import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.EffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.storages.versions.UpgradeScriptVersion1;

public class JsonConverter
{
    private static final String VERSION = "Version";

    private final Map<Integer, UpgradeScript> upgradeScripts = Stream.of(
            new UpgradeScriptVersion1())
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

        jsonObject.put(VERSION, getCurrentVersion());

        return jsonObject.toString();
    }

    private String upgrade(String json) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);

        int jsonVersion = getJsonVersion(jsonObject);
        int currentVersion = getCurrentVersion();

        for (int version = jsonVersion; version <= currentVersion; version++)
        {
            jsonObject = Objects.requireNonNull(upgradeScripts.get(version)).upgrade(jsonObject);
        }

        return jsonObject.toString();
    }

    private static int getJsonVersion(JSONObject jsonObject) throws JSONException
    {
        return jsonObject.has(VERSION) ? jsonObject.getInt(VERSION) : 0;
    }

    private int getCurrentVersion()
    {
        return upgradeScripts.keySet().stream().max(Integer::compareTo).get();
    }
}
