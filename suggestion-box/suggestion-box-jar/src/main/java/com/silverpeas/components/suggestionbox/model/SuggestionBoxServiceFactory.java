/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package com.silverpeas.components.suggestionbox.model;

import javax.inject.Inject;

/**
 * A factory of {@link SuggestionBoxService} instances. It abstracts and manages the life-cycle of
 * these instances by using the underlying IoC container.
 * @author mmoquillon
 */
public class SuggestionBoxServiceFactory {

  private static final SuggestionBoxServiceFactory instance = new SuggestionBoxServiceFactory();

  @Inject
  private SuggestionBoxService service;

  private SuggestionBoxServiceFactory() {
  }

  /**
   * Gets a {@link SuggestionBoxServiceFactory} instance.
   * @return a factory instance.
   */
  public static final SuggestionBoxServiceFactory getFactory() {
    return instance;
  }

  /**
   * Gets a {@link SuggestionBoxService} instance.
   * <p>
   * It is a convenient method to invoke directly a SuggestionBoxService instance to a factory
   * instance.
   * @return a SuggestionBoxService instance.
   */
  public static SuggestionBoxService getServiceInstance() {
    return getFactory().getSuggestionBoxService();
  }

  /**
   * Gets a {@link SuggestionBoxService} instance.
   * @return a SuggestionBoxService instance.
   */
  public SuggestionBoxService getSuggestionBoxService() {
    return service;
  }
}
