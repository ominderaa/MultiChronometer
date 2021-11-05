package nl.motonono.multichronometer.utils;

import java.text.DecimalFormat;

public class TimeFormatter {
    public static synchronized String toTextShort(long timeElapsed) {
        long timetoFormat = timeElapsed;
        if (timeElapsed < 0) timetoFormat = -timeElapsed;
        DecimalFormat singleDigitFormat = new DecimalFormat("0");
        DecimalFormat doubleDigitFormat = new DecimalFormat("00");

        int hours = (int) (timetoFormat / (3600 * 1000));
        int remaining = (int) (timetoFormat % (3600 * 1000));

        int minutes = (remaining / (60 * 1000));
        remaining = (remaining % (60 * 1000));

        int seconds =  (remaining / 1000);

        int milliseconds = (((int) timetoFormat % 1000) / 10);
        String text = "";
        if (hours > 0) {
            text += singleDigitFormat.format(hours) + ":";
            text += doubleDigitFormat.format(minutes) + ":";
            text += doubleDigitFormat.format(seconds) + ".";
            text += doubleDigitFormat.format(milliseconds);
        } else if (minutes > 0) {
            text += singleDigitFormat.format(minutes) + ":";
            text += doubleDigitFormat.format(seconds) + ".";
            text += doubleDigitFormat.format(milliseconds);
        } else  {
            text += singleDigitFormat.format(seconds) + ".";
            text += doubleDigitFormat.format(milliseconds);
        }
        return text;
    }

    public static synchronized String toTextLong(long timeElapsed) {
        long timetoFormat = timeElapsed;
        if (timeElapsed < 0) timetoFormat = -timeElapsed;
        DecimalFormat singleDigitFormat = new DecimalFormat("0");
        DecimalFormat doubleDigitFormat = new DecimalFormat("00");

        int hours = (int) (timetoFormat / (3600 * 1000));
        int remaining = (int) (timetoFormat % (3600 * 1000));

        int minutes = (remaining / (60 * 1000));
        remaining = (remaining % (60 * 1000));

        int seconds =  (remaining / 1000);
        int milliseconds = (((int) timetoFormat % 1000) / 10);

        return singleDigitFormat.format(hours) + ":" +
                doubleDigitFormat.format(minutes) + ":" +
                doubleDigitFormat.format(seconds) + "." +
                doubleDigitFormat.format(milliseconds);
    }
}
