/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package com.silverpeas.classifieds.servlets.handler;

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.classifieds.servlets.FunctionHandler;

public class HandlerProvider {

  /**
   * Map the function name to the function handler
   */
  static private Map<String, FunctionHandler> handlerMap = null;

  /**
   * Inits the function handler
   */
  static {
    handlerMap = new HashMap<>();

    handlerMap.put("Main", new DefaultHandler());
    handlerMap.put("ViewClassifiedToValidate", new ListToValidateHandler());
    handlerMap.put("ViewMyClassifieds", new MyClassifiedsHandler());

    handlerMap.put("SearchClassifieds", new SearchHandler());
    handlerMap.put("Pagination", new PaginationHandler());
    handlerMap.put("searchResult", new SearchResultsHandler());

    handlerMap.put("ViewClassified", new ViewClassifiedHandler());
    handlerMap.put("NewClassified", new ClassifiedCreationFormHandler());
    handlerMap.put("CreateClassified", new ClassifiedCreationHandler());
    handlerMap.put("EditClassified", new ClassifiedUpdateFormHandler());
    handlerMap.put("UpdateClassified", new ClassifiedUpdateHandler());
    handlerMap.put("DeleteClassified", new ClassifiedDeleteHandler());

    handlerMap.put("DraftIn", new DraftInHandler());
    handlerMap.put("DraftOut", new DraftOutHandler());

    handlerMap.put("ValidateClassified", new ClassifiedValidateHandler());
    handlerMap.put("RefusedClassified", new ClassifiedRefuseHandler());

    handlerMap.put("NewSubscription", new SubscriptionCreateFormHandler());
    handlerMap.put("AddSubscription", new SubscriptionCreateHandler());
    handlerMap.put("ViewMySubscriptions", new SubscriptionListHandler());
    handlerMap.put("DeleteSubscription", new SubscriptionDeleteHandler());

    handlerMap.put("ToWysiwygHeader", new ToWysiwygHeaderHandler());
    handlerMap.put("FromTopicWysiwyg", new DefaultHandler());
    
  }

  /**
   * Get specific handler for given use case
   *
   * @param useCase the use case
   *
   * @return  ready to use handler
   */
  public static FunctionHandler getHandler(String useCase) {
    return handlerMap.get(useCase);
  }
}
