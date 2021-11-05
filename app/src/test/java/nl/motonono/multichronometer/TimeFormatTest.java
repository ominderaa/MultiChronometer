package nl.motonono.multichronometer;
import org.junit.Test;

import static org.junit.Assert.*;

import nl.motonono.multichronometer.utils.TimeFormatter;

public class TimeFormatTest {
    @Test
    public void testTimeFormatShort() {
        assertEquals("0.00", TimeFormatter.toTextShort(0L));
        assertEquals("0.50", TimeFormatter.toTextShort(500L));
        assertEquals("1.00", TimeFormatter.toTextShort(1000L));
        assertEquals("1.23", TimeFormatter.toTextShort(1234L));
        assertEquals("2.34", TimeFormatter.toTextShort(2345L));
        assertEquals("1:00.00", TimeFormatter.toTextShort(60000L));
        assertEquals("1:01.23", TimeFormatter.toTextShort(61234L));
        assertEquals("2:00.00", TimeFormatter.toTextShort(120000L));
        assertEquals("3:00.00", TimeFormatter.toTextShort(180000L));
        assertEquals("4:00.00", TimeFormatter.toTextShort(240000L));
        assertEquals("5:00.00", TimeFormatter.toTextShort(300000L));
        assertEquals("6:00.00", TimeFormatter.toTextShort(360000L));
        assertEquals("7:00.00", TimeFormatter.toTextShort(420000L));
        assertEquals("10:00.00", TimeFormatter.toTextShort(600000L));
        assertEquals("1:00:00.00", TimeFormatter.toTextShort(3600000L));
    }

    @Test
    public void testTimeFormatLong() {
        assertEquals("0:00:00.00", TimeFormatter.toTextLong(0L));
        assertEquals("0:00:00.50", TimeFormatter.toTextLong(500L));
        assertEquals("0:00:01.00", TimeFormatter.toTextLong(1000L));
        assertEquals("0:00:01.23", TimeFormatter.toTextLong(1234L));
        assertEquals("0:00:02.34", TimeFormatter.toTextLong(2345L));
        assertEquals("0:01:00.00", TimeFormatter.toTextLong(60000L));
        assertEquals("0:01:01.23", TimeFormatter.toTextLong(61234L));
        assertEquals("0:02:00.00", TimeFormatter.toTextLong(120000L));
        assertEquals("0:03:00.00", TimeFormatter.toTextLong(180000L));
        assertEquals("0:04:00.00", TimeFormatter.toTextLong(240000L));
        assertEquals("0:05:00.00", TimeFormatter.toTextLong(300000L));
        assertEquals("0:06:00.00", TimeFormatter.toTextLong(360000L));
        assertEquals("0:07:00.00", TimeFormatter.toTextLong(420000L));
        assertEquals("0:10:00.00", TimeFormatter.toTextLong(600000L));
        assertEquals("0:15:00.00", TimeFormatter.toTextLong(900000L));
        assertEquals("0:15:00.00", TimeFormatter.toTextLong(900000L));
        assertEquals("1:00:00.00", TimeFormatter.toTextShort(3600000L));
    }
}
