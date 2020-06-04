package org.artifactory.event.work;

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.mutable.MutableInt;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

/**
 * @author Uriah Levy
 */
@Test
public class StatusProbeServiceImplTest {

    public void testStartProbing() throws NoSuchFieldException, IllegalAccessException {
        StatusProbeImpl externalServiceStatusProbe = new StatusProbeImpl();
        setIntervalTime(externalServiceStatusProbe);
        Boolean[] results = {false, false, false, true};
        MutableInt mutableInt = new MutableInt();
        doProbe(externalServiceStatusProbe, results, mutableInt);
        Assert.assertEquals(mutableInt.intValue(), 4);
    }

    public void testLimitedProbeAttempts() throws NoSuchFieldException, IllegalAccessException {
        StatusProbeImpl externalServiceStatusProbe = new StatusProbeImpl();
        setIntervalTime(externalServiceStatusProbe);
        setMaxAttempts(externalServiceStatusProbe);
        Boolean[] results = {false, false};
        MutableInt mutableInt = new MutableInt();
        doProbe(externalServiceStatusProbe, results, mutableInt);
        Assert.assertEquals(mutableInt.intValue(), 2);

    }

    private void doProbe(StatusProbeImpl externalServiceStatusProbe, Boolean[] results, MutableInt mutableInt) {
        externalServiceStatusProbe.startProbing(MoreExecutors.newDirectExecutorService(), () -> {
            int i = mutableInt.getValue();
            boolean result = results[i];
            mutableInt.increment();
            return result;
        }, () -> {
        });
    }

    private void setIntervalTime(StatusProbeImpl externalServiceStatusProbe)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = externalServiceStatusProbe.getClass().getDeclaredField("intervalTimeMillis");
        field.setAccessible(true);
        field.setInt(externalServiceStatusProbe, 100);
    }

    private void setMaxAttempts(StatusProbeImpl externalServiceStatusProbe)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = externalServiceStatusProbe.getClass().getDeclaredField("maxAttempts");
        field.setAccessible(true);
        field.setInt(externalServiceStatusProbe, 2);
    }
}