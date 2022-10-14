package nl.motonono.multichronometer;

import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationHelper {
    private static Vibrator vibrator;
    private static VibrationHelper _helper;

    private VibrationHelper(Vibrator vibrator) { this.vibrator = vibrator;}
    public static VibrationHelper creatInstance(Activity activity) {
        if (_helper == null) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            _helper = new VibrationHelper(vibrator);
        }
        return _helper;
    }

    public static VibrationHelper getInstance() {
        return _helper;
    }

    public void vibrateOnClick() {
        // this type of vibration requires API 29
        final VibrationEffect vibrationEffect2;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // create vibrator effect with the constant EFFECT_CLICK
            vibrationEffect2 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            // it is safe to cancel other vibrations currently taking place
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect2);
        }
    }
}
