package com.example.civsetu;

import static org.junit.Assert.assertEquals;
import com.example.civsetu.utils.Constants;
import com.example.civsetu.utils.SLAUtils;
import org.junit.Test;
import java.util.Date;

public class SLAUtilsTest {

    @Test
    public void testGetSLADays() {
        assertEquals(1, SLAUtils.getSLADays(Constants.PRIORITY_CRITICAL));
        assertEquals(3, SLAUtils.getSLADays(Constants.PRIORITY_HIGH));
        assertEquals(7, SLAUtils.getSLADays(Constants.PRIORITY_MEDIUM));
        assertEquals(14, SLAUtils.getSLADays(Constants.PRIORITY_LOW));
        assertEquals(14, SLAUtils.getSLADays("unknown"));
    }

    @Test
    public void testGetSLAStatusOnTrack() {
        Date now = new Date();
        // Critical is 1 day. 0 hours passed.
        assertEquals(Constants.STATUS_ON_TRACK, SLAUtils.getSLAStatus(now, Constants.PRIORITY_CRITICAL));
    }

    @Test
    public void testGetSLAStatusBreached() {
        // Critical is 1 day. 2 days ago.
        long twoDaysAgo = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000);
        assertEquals(Constants.STATUS_BREACHED, SLAUtils.getSLAStatus(new Date(twoDaysAgo), Constants.PRIORITY_CRITICAL));
    }
}
