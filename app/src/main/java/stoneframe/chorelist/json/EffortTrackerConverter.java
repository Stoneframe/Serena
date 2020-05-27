package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.chorelist.model.EffortTracker;

/**
 * Created by PC on 2017-02-17.
 */
public interface EffortTrackerConverter extends JsonDeserializer<EffortTracker>
{

}
