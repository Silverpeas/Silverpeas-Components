/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.hyperlink.control;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserFull;

/**
 * @author nchaix
 */
public class HyperlinkSessionController extends AbstractComponentSessionController {

  public HyperlinkSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "org.silverpeas.hyperlink.multilang.hyperlinkBundle", null,
        "org.silverpeas.hyperlink.settings.hyperlinkSettings");
  }

  public UserFull getUserFull() {
    return getOrganisationController().getUserFull(getUserId());
  }

  public boolean isClientSSO() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("clientSSO"));
  }

  public String getURL() {
    return getComponentParameterValue("URL");
  }

  public String getMethodType() {
    String methodType = getComponentParameterValue("method");
    return methodType;
  }
}