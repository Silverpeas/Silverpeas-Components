/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.components.formsonline.notification;

import com.silverpeas.formsonline.model.FormInstance;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import org.silverpeas.util.template.SilverpeasTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Nicolas EYSSERIC
 */
public class FormsOnlineValidationRequestUserNotification
    extends AbstractFormsOnlineRequestUserNotification {

  public FormsOnlineValidationRequestUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, action);
  }

  @Override
  protected String getBundleSubjectKey() {
    if (NotifAction.VALIDATE.equals(getAction())) {
      return "formsOnline.msgFormValidated";
    }
    return "formsOnline.msgFormRefused";
  }

  @Override
  protected String getFileName() {
    if (NotifAction.VALIDATE.equals(getAction())) {
      return "notificationValidated";
    }
    return "notificationDenied";
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("comment", getResource().getComments());
    if (NotifAction.VALIDATE.equals(getAction())) {
      template.setAttribute("validated", true);
    } else if (NotifAction.REFUSE.equals(getAction())) {
      template.setAttribute("denied", true);
    }
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    List<String> ids = new ArrayList<String>();
    ids.add(getResource().getCreatorId());
    return ids;
  }

  @Override
  protected boolean isSendImmediatly() {
    return true;
  }
}