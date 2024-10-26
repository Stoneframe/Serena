package stoneframe.serena.model.util;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DeepCopy
{
    /** @noinspection unchecked*/
    public static <T> T copy(T original)
    {
        Gson gson = Converters.registerAll(new GsonBuilder()).create();

        return (T)gson.fromJson(gson.toJson(original), original.getClass());
    }
}
