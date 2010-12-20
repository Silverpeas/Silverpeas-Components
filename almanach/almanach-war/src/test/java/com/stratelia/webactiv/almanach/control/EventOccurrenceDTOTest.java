/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.control;

import org.json.JSONArray;
import org.json.JSONObject;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests on the EventOccurrenceDTO.
 * The tests are on the JSON encoding of the DTO.
 */
public class EventOccurrenceDTOTest {

  public EventOccurrenceDTOTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * For checking all is ok by default.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * The CSS class of an event DTO should be the identifier of the almanach instance it belongs to.
   */
  @Test
  public void theCSSClassOfAnEventShouldBeTheAlmanachId() {
    final String almanachId = "almanach1";
    EventDetail detail = getEventDetail();

    EventOccurrenceDTO eventDTO = new EventOccurrenceDTO(detail, new Date(), new Date());
    assertEquals("The CSS class should be the almanach instance id", almanachId, eventDTO.
        getCSSClasses().get(0));
  }

  /**
   * Check the JSON representation of a given event is the expected one.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void testJSONRepresentationOfAnEvent() throws Exception {
    EventDetail detail = getEventDetail();
    Date startDate = DateUtil.parse("2010-10-12T12:00:00Z", DateUtil.ISO8601DATE_FORMATTER.
        getPattern());
    Date endDate = DateUtil.parse("2010-10-12T14:00:00Z",
        DateUtil.ISO8601DATE_FORMATTER.getPattern());

    EventOccurrenceDTO eventDTO = new EventOccurrenceDTO(detail, startDate, endDate);
    String eventInJSON = eventDTO.toJSON();
    JSONObject jsonObject = new JSONObject(eventInJSON);
    assertEquals("The title should be the event title", eventDTO.getEventDetail().getTitle(), jsonObject.
        get("title"));
    assertEquals("The identifier should be the event id", eventDTO.getEventDetail().getId(), jsonObject.
        get("id"));
    assertEquals("The CSS class should be the expected one", eventDTO.getCSSClasses().get(0), ((JSONArray) jsonObject.
        get(
        "className")).get(0));
    assertEquals("The start date should be the event start date", eventDTO.getStartDateTimeInISO(), jsonObject.
        get("start"));
    assertEquals("The end date should be the event end date", eventDTO.getEndDateTimeInISO(), jsonObject.
        get("end"));
  }

  /**
   * Check the JSON representation of a list of events is the expected one.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void testJSONRepresentationOfAListOfEvents() throws Exception {
    EventDetail detail1 = getEventDetail();
    Date startDate1 = DateUtil.parse("2010-10-12T12:00:00Z", DateUtil.ISO8601DATE_FORMATTER.
        getPattern());
    Date endDate1 = DateUtil.parse("2010-10-12T14:00:00Z",
        DateUtil.ISO8601DATE_FORMATTER.getPattern());
    EventDetail detail2 = getEventDetail();
    Date startDate2 = DateUtil.parse("2010-10-12T12:00:00Z", DateUtil.ISO8601DATE_FORMATTER.
        getPattern());
    Date endDate2 = DateUtil.parse("2010-10-12T14:00:00Z",
        DateUtil.ISO8601DATE_FORMATTER.getPattern());
    EventDetail detail3 = getEventDetail();
    Date startDate3 = DateUtil.parse("2010-10-12T12:00:00Z", DateUtil.ISO8601DATE_FORMATTER.
        getPattern());
    Date endDate3 = DateUtil.parse("2010-10-12T14:00:00Z",
        DateUtil.ISO8601DATE_FORMATTER.getPattern());

    List<EventOccurrenceDTO> events = Arrays.asList(
        new EventOccurrenceDTO(detail1, startDate1, endDate1),
        new EventOccurrenceDTO(detail2, startDate2, endDate2),
        new EventOccurrenceDTO(detail3, startDate3, endDate3));
    String eventsInJSON = EventOccurrenceDTO.toJSON(events);
    JSONArray jsonArray = new org.json.JSONArray(eventsInJSON);
    assertEquals("All events should be encoded in JSON", events.size(), jsonArray.length());
    for (EventOccurrenceDTO event : events) {
      boolean found = false;
      for (int i = 0; i < jsonArray.length() && !found; i++) {
        if (jsonArray.getString(i).equals(event.toJSON())) {
          found = true;
        }
      }
      assertTrue(found);
    }
  }

  /**
   * Gets the detail on an event.
   * @return an event detail for testing pupose.
   */
  protected EventDetail getEventDetail() {
    EventDetail eventDetail = new EventDetail(new EventPK("1", "WA1", "almanach1"),
        "An event for testing purpose");
    eventDetail.setTitle("event test");
    return eventDetail;
  }
}