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
    handlerMap = new HashMap<String, FunctionHandler>();

    handlerMap.put("Main", new DefaultHandler());
    handlerMap.put("ViewClassifiedToValidate", new ListToValidateHandler());
    handlerMap.put("ViewMyClassifieds", new MyClassifiedsHandler());
    handlerMap.put("ViewAllClassifiedsByCategory", new ClassifiedsListByCategoryHandler());

    handlerMap.put("SearchClassifieds", new SearchHandler());
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
