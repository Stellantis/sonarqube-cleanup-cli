
package org.psa.sonarqube.cleanup.rest.model;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ComponentTest {

    @Test
    public void testMeasureNClocFilled() {
        Measure m = mock(Measure.class);
        when(m.getMetric()).thenReturn("ncloc");
        when(m.getValue()).thenReturn(42L);

        List<Measure> l = new ArrayList<>();
        l.add(m);

        Component c = spy(Component.class);
        doReturn(l).when(c).getMeasures();

        Assert.assertEquals(42, c.getNcloc());
    }

    @Test
    public void testMeasureBadFilled() {
        Measure m = mock(Measure.class);
        when(m.getMetric()).thenReturn("noexit");
        when(m.getValue()).thenReturn(42L);

        List<Measure> l = new ArrayList<>();
        l.add(m);

        Component c = spy(Component.class);
        doReturn(l).when(c).getMeasures();

        Assert.assertEquals(0, c.getNcloc());
    }

    @Test
    public void testMeasureEmpty() {
        Component c = spy(Component.class);
        doReturn(new ArrayList<>()).when(c).getMeasures();

        Assert.assertEquals(0, c.getNcloc());
    }

    @Test
    public void testMeasureNull() {
        Component c = spy(Component.class);
        doReturn(null).when(c).getMeasures();

        Assert.assertEquals(0, c.getNcloc());
    }
}
