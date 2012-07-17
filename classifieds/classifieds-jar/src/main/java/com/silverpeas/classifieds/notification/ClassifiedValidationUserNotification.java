/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.classifieds.notification;

import java.util.Collection;
import java.util.Collections;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;

/**
 * @author Yohann Chastagnier
 */
public class ClassifiedValidationUserNotification extends AbstractClassifiedUserNotification {

  private final String userIdWhoRefuse;
  private final String refusalMotive;
  private final String userToBeNotified;

  public ClassifiedValidationUserNotification(final ClassifiedDetail resource, final String userIdWhoRefuse,
      final String refusalMotive, final String userToBeNotified) {
    super(resource, null, "subscription");
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
    if (!ClassifiedDetail.VALID.equals(getResource().getStatus())) {
      return "classifieds.classifiedRefused";
    }
    return "classifieds.classifiedValidated";
  }

  @Override
  protected String getFileName() {
    if (!ClassifiedDetail.VALID.equals(getResource().getStatus())) {
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
    template.setAttribute("refusalMotive", refusalMotive);
  }

  @Override
  protected NotifAction getAction() {
    if (!ClassifiedDetail.VALID.equals(getResource().getStatus())) {
      return NotifAction.REFUSE;
    }
    return NotifAction.VALIDATE;
  }

  @Override
  protected String getSender() {
    return userIdWhoRefuse;
  }
}
