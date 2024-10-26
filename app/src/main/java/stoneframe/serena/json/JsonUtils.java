package stoneframe.serena.json;

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
}
