package stoneframe.serena.gui.util;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibratorManager
{
    public static void startVibration(Context context)
    {
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator())
        {
            long[] pattern = {0, 1000, 500, 1000, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
    }

    public static void stopVibration(Context context)
    {
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null)
        {
            vibrator.cancel();
        }
    }
}
