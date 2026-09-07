package stoneframe.serena.storages.versions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class UpgradeScriptVersion29Test
{
    @Test
    public void upgrade_addsUnlimitedTaskSettings() throws Exception
    {
        JSONObject taskContainer = new JSONObject();
        JSONObject container = new JSONObject().put("TaskContainer", taskContainer);

        JSONObject upgradedContainer = new UpgradeScriptVersion29().upgrade(container);
        JSONObject upgradedTaskContainer = upgradedContainer.getJSONObject("TaskContainer");

        assertTrue(upgradedTaskContainer.isNull("maximumNumberOfTasksPerDay"));
        assertEquals(0, upgradedTaskContainer.getInt("numberOfTasksCompletedToday"));
        assertTrue(upgradedTaskContainer.isNull("taskCompletionCountDate"));
    }
}
