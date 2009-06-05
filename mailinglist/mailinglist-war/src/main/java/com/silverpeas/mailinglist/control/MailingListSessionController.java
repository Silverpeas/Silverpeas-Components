package com.silverpeas.mailinglist.control;

import com.silverpeas.mailinglist.service.model.MailingListService;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class MailingListSessionController extends
    AbstractComponentSessionController {

  /**
   * Standard Session Controller Constructeur
   *
   *
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   *
   * @see
   */
  public MailingListSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.mailinglist.multilang.mailingListBundle",
        "com.silverpeas.mailinglist.settings.mailingListIcons");
  }

  public boolean isModerated() {
    String param = getComponentParameterValue(MailingListService.PARAM_MODERATE);
    return param != null
        && (Boolean.valueOf(param).booleanValue()
            || "Y".equalsIgnoreCase(param) || "YES".equalsIgnoreCase(param));
  }
}