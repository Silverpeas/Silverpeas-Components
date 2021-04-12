/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.components.formsonline.model.FormInstanceValidation;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

import java.util.Optional;

/**
 * @author Nicolas EYSSERIC
 */
public class FormsOnlineProcessedRequestOtherValidatorsUserNotification
    extends AbstractFormsOnlineProcessedRequestUserNotification {

  public FormsOnlineProcessedRequestOtherValidatorsUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, action);
    Optional<FormInstanceValidation> latestValidation = resource.getValidations().getLatestValidation();
    if (latestValidation.isPresent()) {
      if (latestValidation.get().getValidationType().isFinal()) {
        setUserIdsToNotify(resource.getForm().getReceiversAsUsers());
        setGroupIdsToNotify(resource.getForm().getReceiversAsGroups());
      } else if (latestValidation.get().getValidationType().isIntermediate()) {
        setUserIdsToNotify(resource.getForm().getIntermediateReceiversAsUsers());
        setGroupIdsToNotify(resource.getForm().getIntermediateReceiversAsGroups());
      }
    }
  }

  @Override
  protected String getBundleSubjectKey() {
    return "formsOnline.msgFormProcessed";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationProcessedOtherValidatorsSameLevel";
  }

}