/*
 * HyperlinkSessionController.java
 *
 */

package com.stratelia.webactiv.hyperlink.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserFull;

/**
 * 
 * @author nchaix
 * @version
 */
public class HyperlinkSessionController extends
    AbstractComponentSessionController {

  public HyperlinkSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.silverpeas.hyperlink.multilang.hyperlinkBundle", null,
        "com.silverpeas.hyperlink.settings.hyperlinkSettings");
  }

  public UserFull getUserFull() {
    return getOrganizationController().getUserFull(getUserId());
  }
}