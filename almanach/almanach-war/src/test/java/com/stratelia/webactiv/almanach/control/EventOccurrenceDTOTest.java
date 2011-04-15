/*
 * Copyright (C) 2000 - 2011 Silverpeas
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

import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Arrays;
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
    EventOccurrenceDTO eventDTO = new EventOccurrenceDTO(detail,
        new DateDTO(DateUtil.formatAsISO8601Day(detail.getStartDate()), detail.getStartHour()),
        new DateDTO(DateUtil.formatAsISO8601Day(detail.getEndDate()), detail.getEndHour()));
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
    EventOccurrenceDTO eventDTO = new EventOccurrenceDTO(detail,
        new DateDTO(DateUtil.formatAsISO8601Day(detail.getStartDate()), detail.getStartHour()),
        new DateDTO(DateUtil.formatAsISO8601Day(detail.getEndDate()), detail.getEndHour()));
    String eventInJSON = eventDTO.toJSON();
    JSONObject jsonObject = new JSONObject(eventInJSON);
    assertJSONEventMatchesEventDTO(eventDTO, jsonObject);
  }

  /**
   * Check the JSON representation of a list of events is the expected one.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void testJSONRepresentationOfAListOfEvents() throws Exception {
    EventDetail detail = getEventDetail();
    List<EventOccurrenceDTO> events = Arrays.asList(
        new EventOccurrenceDTO(detail,
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getStartDate()), detail.getStartHour()),
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getEndDate()), detail.getEndHour())),
        new EventOccurrenceDTO(detail,
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getStartDate()), detail.getStartHour()),
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getEndDate()), detail.getEndHour())),
        new EventOccurrenceDTO(detail,
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getStartDate()), detail.getStartHour()),
          new DateDTO(DateUtil.formatAsISO8601Day(detail.getEndDate()), detail.getEndHour())));
    String eventsInJSON = EventOccurrenceDTO.toJSON(events);
    JSONArray jsonArray = new org.json.JSONArray(eventsInJSON);
    assertEquals("All events should be encoded in JSON", events.size(), jsonArray.length());
    for (int i = 0; i < events.size(); i++) {
      EventOccurrenceDTO eventDTO = events.get(i);
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      assertJSONEventMatchesEventDTO(eventDTO, jsonObject);
    }
  }

  /**
   * Gets the detail on an event.
   * @return an event detail for testing pupose.
   */
  protected EventDetail getEventDetail() {
    Calendar date = Calendar.getInstance();
    EventDetail eventDetail = new EventDetail(new EventPK("1", "WA1", "almanach1"),
        "An event for testing purpose");
    eventDetail.setTitle("event test");
    eventDetail.setStartDate(date.getTime());
    eventDetail.setStartHour(DateUtil.formatTime(date));
    date.add(Calendar.HOUR_OF_DAY, 2);
    eventDetail.setEndDate(date.getTime());
    eventDetail.setEndHour(DateUtil.formatTime(date));
    return eventDetail;
  }

  /**
   * Asserts the specified JSON object matches the specified event DTO.
   * @param eventDTO the expected object to match.
   * @param jsonObject the actual JSON object.
   */
  protected void assertJSONEventMatchesEventDTO(final EventOccurrenceDTO eventDTO,
      final JSONObject jsonObject) {
    assertEquals(eventDTO.getEventDetail().getTitle(), jsonObject.get("title"));
    assertEquals(eventDTO.getEventDetail().getId(), jsonObject.get("id"));
    assertEquals(eventDTO.getCSSClasses().get(0), ((JSONArray) jsonObject.
        get("className")).get(0));
    assertEquals(eventDTO.getStartDateTimeInISO(), jsonObject.get("start"));
    assertEquals(eventDTO.getEndDateTimeInISO(), jsonObject.get("end"));
    assertEquals(eventDTO.isAllDay(), jsonObject.getBoolean("allDay"));
  }
}