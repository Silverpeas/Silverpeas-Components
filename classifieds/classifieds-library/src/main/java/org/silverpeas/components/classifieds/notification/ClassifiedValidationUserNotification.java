/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.notification;

import java.util.Collection;
import java.util.Collections;

import org.owasp.encoder.Encode;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/**
 * @author Yohann Chastagnier
 */
public class ClassifiedValidationUserNotification extends AbstractClassifiedUserNotification {

  private final String userIdWhoRefuse;
  private final String refusalMotive;
  private final String userToBeNotified;

  public ClassifiedValidationUserNotification(final ClassifiedDetail resource, final String userIdWhoRefuse,
      final String refusalMotive, final String userToBeNotified) {
    super(resource);
    this.userIdWhoRefuse = userIdWhoRefuse;
    this.refusalMotive = refusalMotive;
    this.userToBeNotified = userToBeNotified;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(userToBeNotified);
  }

  @Override
  protected String getBundleSubjectKey() {
    if (!getResource().isValid()) {
      return "classifieds.classifiedRefused";
    }
    return "classifieds.classifiedValidated";
  }

  @Override
  protected String getTemplateFileName() {
    if (!getResource().isValid()) {
      return "refused";
    }
    return "validated";
  }

  @Override
  protected void perform(final ClassifiedDetail resource) {
    super.perform(resource);
    getNotificationMetaData().setOriginalExtraMessage(refusalMotive);
  }

  @Override
  protected void performTemplateData(final String language, final ClassifiedDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("refusalMotive", Encode.forHtml(refusalMotive));
  }

  @Override
  protected NotifAction getAction() {
    if (!getResource().isValid()) {
      return NotifAction.REFUSE;
    }
    return NotifAction.VALIDATE;
  }

  @Override
  protected String getSender() {
    return userIdWhoRefuse;
  }
}
