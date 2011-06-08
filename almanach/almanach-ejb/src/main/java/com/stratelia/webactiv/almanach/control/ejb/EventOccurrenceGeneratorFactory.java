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
package com.stratelia.webactiv.almanach.control.ejb;

/**
 * A factory of EventOccurrenceGenerator instances. It manages the life-cycle of the generators and
 * abstracts the concrete implementation used to generate event occurrences.
 */
public class EventOccurrenceGeneratorFactory {

  private static final EventOccurrenceGeneratorFactory instance = new EventOccurrenceGeneratorFactory();
  
  private static final EventOccurrenceGenerator generator = new ICal4JEventOccurrencesGenerator();
  
  /**
   * Gets a factory of event occurrences generators.
   * The returned factory abstracts the concrete implementation of the EventOccurrenceGenerator
   * interface.
   * @return an EventOccurrenceGeneratorFactory instance.
   */
  public static EventOccurrenceGeneratorFactory getFactory() {
    return instance;
  }
  
  /**
   * Gets a generator of event occurrences.
   * @return an instance of the EventOccurrenceGenerator interface.
   */
  public EventOccurrenceGenerator getEventOccurrenceGenerator() {
    return generator;
  }
}
