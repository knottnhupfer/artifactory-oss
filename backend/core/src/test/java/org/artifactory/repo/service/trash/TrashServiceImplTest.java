package org.artifactory.repo.service.trash;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Calendar;

/**
 * @author Shay Bagants
 */
@Test
public class TrashServiceImplTest extends ArtifactoryHomeBoundTest {

    /**
     * The time that a file was stored in a trashcan is saved in the DB as millis as text.
     * The GC (trashcan cleanup phase) search for files that are in the trashcan for more than x days, and delete these.
     * The date (timestamp) comparision is done in the DB, on the text values, therefore, it is mandatory that all
     * timestamps will be at the same length (same number of digits)
     *
     * We decided that the min allowed date (oldest time of file in trashcan) is 2003 January 1
     */
    @Test(dataProvider = "provideInvalidTimestamps", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Illegal retention date. Retention period goes back too far.")
    public void testValidateRetentionPeriodTimestampInvalidTimestamps(long timetamp) {
        TrashServiceImpl trashService = new TrashServiceImpl();
        trashService.validateRetentionPeriodTimestamp(timetamp);
    }

    @DataProvider
    public static Object[][] provideInvalidTimestamps() {
        Object[][] objects = new Object[22][];
        Calendar cal = Calendar.getInstance();
        int i = 0;
        for (int year = 1990; year < 2002; year++) {
            cal.set(year, 01, 01);
            objects[i] = new Object[]{cal.getTime().toInstant().toEpochMilli()};
            i++;
        }
        return objects;
    }

    @Test(dataProvider = "provideValidTimestamps")
    public void testValidateRetentionPeriodTimestamp(long timstamp) {
        TrashServiceImpl trashService = new TrashServiceImpl();
        trashService.validateRetentionPeriodTimestamp(timstamp);
    }

    @DataProvider
    public static Object[][] provideValidTimestamps() {
        Object[][] objects = new Object[27][];
        Calendar cal = Calendar.getInstance();
        int i = 0;
        for (int year = 2003; year < 2030; year++) {
            cal.set(year, 01, 02);
            objects[i] = new Object[]{cal.getTime().toInstant().toEpochMilli()};
            i++;
        }
        return objects;
    }
}