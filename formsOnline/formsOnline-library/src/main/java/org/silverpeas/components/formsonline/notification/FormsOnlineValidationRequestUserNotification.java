/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.notification.user.client.constant.NotifAction.*;

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
    if (VALIDATE.equals(getAction())) {
      return "formsOnline.msgFormValidated";
    }
    return "formsOnline.msgFormRefused";
  }

  @Override
  protected String getTitle(final String language) {
    String title = super.getTitle(language);
    if (REFUSE.equals(getAction()) || VALIDATE.equals(getAction()) || PENDING_VALIDATION.equals(getAction())) {
      int nbValidationSteps = getNbValidationSteps();
      if (nbValidationSteps > 1) {
        int step = getCurrentValidationStep();
        title += " " + getBundle(language)
            .getStringWithParams("formsOnline.msgFormValidated.steps", Integer.toString(step),
                Integer.toString(nbValidationSteps));
      }
    }
    return title;
  }

  @Override
  protected String getTemplateFileName() {
    if (VALIDATE.equals(getAction())) {
      return "notificationValidated";
    }
    return "notificationDenied";
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    getResource().getValidations().getLatestValidation()
        .ifPresent(v -> template.setAttribute("comment", v.getComment()));
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return singletonList(getResource().getCreatorId());
  }

  protected int getNbValidationSteps() {
    return getResource().getValidationsSchema().size();
  }

  /**
   * The meaning of the returned step number is "VALIDATED" and not "TO VALIDATE".
   * <p>
   * Please override this method in order to get the "TO VALIDATE" meaning.
   * </p>
   * @return the step number as integer.
   */
  protected int getCurrentValidationStep() {
    return Math.min(getResource().getValidations().size(), getNbValidationSteps());
  }
}