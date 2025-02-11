package stoneframe.serena.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonUtils
{
    public static void removeAllAttributes(JSONObject jsonObject)
    {
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext())
        {
            keys.next();
            keys.remove();
        }
    }

    public static void changePropertyName(JSONObject jsonObject, String oldName, String newName) throws JSONException
    {
        Object value = jsonObject.get(oldName);

        jsonObject.remove(oldName);
        jsonObject.put(newName, value);
    }
}
