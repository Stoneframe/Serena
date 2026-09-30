package stoneframe.serena.gui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.annotation.NonNull;

import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;

public abstract class NotifierReceiver extends BroadcastReceiver
{
    @NonNull
    protected static Serena getSerena(Context context)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();

        return globalState.getOrLoadSerena();
    }
}
