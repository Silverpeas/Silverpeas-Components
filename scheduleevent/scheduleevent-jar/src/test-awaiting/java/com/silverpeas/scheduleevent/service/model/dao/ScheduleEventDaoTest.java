/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduleevent.service.model.dao;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import static com.silverpeas.scheduleevent.service.model.dao.ScheduledEventMatcher.isEqualTo;

public class ScheduleEventDaoTest {

  private ConfigurableApplicationContext context;
  private DataSourceDatabaseTester databaseTester;
  private ScheduleEventDao scheduleEventDao;

  public ScheduleEventDaoTest() {
  }

  @Before
  public void bootstrapTestContext() throws Exception {
    context = new ClassPathXmlApplicationContext("/spring-scheduleevent.xml",
        "/spring-scheduleevent-embbed-datasource.xml");
    databaseTester = new DataSourceDatabaseTester(getDataSource());
    databaseTester.setDataSet(getDataSet());
    databaseTester.onSetup();
    scheduleEventDao = context.getBean(ScheduleEventDao.class);
  }

  @After
  public void shutdownTestContext() throws Exception {
    databaseTester.onTearDown();
    context.close();
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        ScheduleEventDaoTest.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/scheduleevent/service/model/dao/scheduleevent-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  protected DataSource getDataSource() {
    return context.getBean(DataSource.class);
  }

  @Test
  public void emptyTest() {
  }

  @Test
  public void createANewScheduleEvent() throws Exception {
    ScheduleEvent event = new ScheduleEvent();
    event.setAuthor(0);
    event.setCreationDate(Timestamp.valueOf("2012-04-10 17:56:35.761"));
    event.setTitle("A new event");
    event.setDescription("test the persistence of the new event");
    Contributor contributor = new Contributor();
    contributor.setUserId(0);
    contributor.setUserName("Bart Simpson");
    contributor.setScheduleEvent(event);
    event.getContributors().add(contributor);
    scheduleEventDao.createScheduleEvent(event);

    int rowCount = databaseTester.getConnection().getRowCount("sc_scheduleevent_list",
        "where id = '" + event.getId() + "'");
    assertThat(rowCount, is(1));
    rowCount = databaseTester.getConnection().getRowCount("sc_scheduleevent_contributor",
        "where id = '" + contributor.getId() + "'");
    assertThat(rowCount, is(1));
  }

  @Test
  public void listScheduleEventsForAnExistingContributor() {
    String contributorId = "2";
    Set<ScheduleEvent> events = scheduleEventDao.listScheduleEventsByContributorId(contributorId);
    assertThat(events.size(), is(1));

    ScheduleEvent theEvent = events.iterator().next();
    assertThat(theEvent, isEqualTo(theScheduledEvent()));
  }

  @Test
  public void listScheduleEventsForAnUnexistingContributor() {
    String contributorId = "100";
    Set<ScheduleEvent> events = scheduleEventDao.listScheduleEventsByContributorId(contributorId);
    assertThat(events.isEmpty(), is(true));
  }

  @Test
  public void getAnExistingScheduleEvent() {
    ScheduleEvent expected = theScheduledEvent();
    ScheduleEvent actual = scheduleEventDao.getScheduleEvent(expected.getId());
    assertThat(actual, isEqualTo(expected));
  }

  @Test
  public void getAnUnexistingScheduleEvent() {
    ScheduleEvent actual = scheduleEventDao.getScheduleEvent("toto");
    assertThat(actual, nullValue());
  }

  @Test
  public void deleteAScheduleEvent() throws Exception {
    ScheduleEvent scheduleEvent = theScheduledEvent();
    scheduleEventDao.deleteScheduleEvent(scheduleEvent);
    int eventCount = databaseTester.getConnection().getRowCount("sc_scheduleevent_list",
        "where id = '" + scheduleEvent.getId() + "'");
    assertThat(eventCount, is(0));
  }

  @Test
  public void removeResponsesOfAGivenUser() {
    ScheduleEvent scheduleEvent = scheduleEventDao.getScheduleEvent(theScheduledEvent().getId());
    assertThat(scheduleEvent.getResponses().size(), is(2));
    scheduleEventDao.purgeResponseScheduleEvent(scheduleEvent, 0);
    ScheduleEvent actual = scheduleEventDao.getScheduleEvent(scheduleEvent.getId());
    assertThat(actual.getResponses().size(), is(1));
  }

  private ScheduleEvent theScheduledEvent() {
    try {
      ScheduleEvent event = new ScheduleEvent();
      event.setId("ff808081369cf58901369cf88a380000");
      event.setAuthor(0);
      event.setCreationDate(Timestamp.valueOf("2012-04-10 17:56:35.761"));
      event.setTitle("Toto");
      event.setDescription("");
      event.setStatus(1);
      event.getContributors().addAll(theContributors());
      event.getDates().addAll(theDateOptions());
      event.getResponses().addAll(theResponses(event));
      return event;

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private Set<Contributor> theContributors() {
    try {
      Set<Contributor> contributors = new HashSet<Contributor>(3);
      Contributor contributor = new Contributor();
      contributor.setId("ff808081369cf58901369cf88a3d0003");
      contributor.setUserId(1);
      contributor.setLastVisit(null);
      contributor.setLastValidation(null);
      contributors.add(contributor);

      contributor = new Contributor();
      contributor.setId("f808081369cf58901369cf88a3d0004");
      contributor.setUserId(2);
      contributor.setLastVisit(Timestamp.valueOf("2012-04-10 17:57:08.8"));
      contributor.setLastValidation(Timestamp.valueOf("2012-04-10 17:57:08.818"));
      contributors.add(contributor);

      contributor = new Contributor();
      contributor.setId("ff808081369cf58901369cf88a3d0005");
      contributor.setUserId(0);
      contributor.setLastVisit(Timestamp.valueOf("2012-04-10 17:56:46.549"));
      contributor.setLastValidation(Timestamp.valueOf("2012-04-10 17:56:46.574"));
      contributors.add(contributor);

      return contributors;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private SortedSet<DateOption> theDateOptions() {
    try {
      SortedSet<DateOption> options = new TreeSet<DateOption>();
      DateOption option = new DateOption();
      option.setId("ff808081369cf58901369cf88a3d0001");
      option.setDay(Timestamp.valueOf("2012-04-24 00:00:00.000"));
      option.setHour(14);
      options.add(option);

      option = new DateOption();
      option.setId("f808081369cf58901369cf88a3d0002");
      option.setDay(Timestamp.valueOf("2012-04-25 00:00:00.000"));
      option.setHour(14);
      options.add(option);

      return options;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private Set<Response> theResponses(final ScheduleEvent event) {
    Set<Response> responses = new HashSet<Response>(2);
    Response response = new Response();
    response.setId("ff808081369cf58901369cf8b45d0006");
    response.setOptionId("ff808081369cf58901369cf88a3d0001");
    response.setUserId(0);
    response.setScheduleEvent(event);
    responses.add(response);

    response = new Response();
    response.setId("ff808081369cf58901369cf90b410009");
    response.setOptionId("ff808081369cf58901369cf88a3d0001");
    response.setUserId(2);
    response.setScheduleEvent(event);
    responses.add(response);

    return responses;
  }
}
