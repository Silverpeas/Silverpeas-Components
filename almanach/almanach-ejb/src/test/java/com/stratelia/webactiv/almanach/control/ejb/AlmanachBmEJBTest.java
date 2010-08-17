package com.stratelia.webactiv.almanach.control.ejb;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.stratelia.webactiv.almanach.model.Periodicity;
import java.util.Calendar;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.RRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ehugonnet
 */
public class AlmanachBmEJBTest {

  public AlmanachBmEJBTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of generateRecurrenceRule method, of class AlmanachBmEJB.
   */
  @Test
  public void testGenerateRecurrenceRule() {
    Periodicity periodicity = new Periodicity();
    periodicity.setFrequency(1);
    periodicity.setUnity(Periodicity.UNIT_DAY);
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 30);
    calend.set(Calendar.MONTH, Calendar.JUNE);
    calend.set(Calendar.YEAR, 2010);
    calend.set(Calendar.HOUR_OF_DAY, 10);
    calend.set(Calendar.MINUTE, 30);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    periodicity.setUntilDatePeriod(calend.getTime());
    AlmanachBmEJB instance = new AlmanachBmEJB();
    RRule result = instance.generateRecurrenceRule(periodicity);
    Assert.assertNotNull(result);
    Assert.assertFalse(result.isComponentProperty());
    Recur recur = result.getRecur();
    Assert.assertNotNull(recur);
    Assert.assertNotNull(recur.getUntil());
    calend.set(Calendar.HOUR_OF_DAY, 23);
    calend.set(Calendar.MINUTE, 59);
    calend.set(Calendar.SECOND, 59);
    Assert.assertEquals(calend.getTimeInMillis(), recur.getUntil().getTime());
    Assert.assertEquals(Recur.DAILY, recur.getFrequency());
  }
}
