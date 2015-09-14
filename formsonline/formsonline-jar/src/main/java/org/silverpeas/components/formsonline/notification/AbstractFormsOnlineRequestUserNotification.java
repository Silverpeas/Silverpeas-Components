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
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractFormsOnlineRequestUserNotification
    extends AbstractFormsOnlineUserNotification<FormInstance> {

  private final NotifAction action;
  private final String senderName;

  protected AbstractFormsOnlineRequestUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, null, null);
    this.action = action;
    if (NotifAction.PENDING_VALIDATION.equals(action)) {
      this.senderName = UserDetail.getById(resource.getCreatorId()).getDisplayedName();
    } else {
      this.senderName = UserDetail.getById(resource.getValidatorId()).getDisplayedName();
    }
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("form", resource.getForm());
    template.setAttribute("request", resource);
    template.setAttribute("formName", resource.getForm().getName());
    template.setAttribute("senderName", getSenderName());
    template.setAttribute("requester", resource.getCreator());
    template.setAttribute("validator", resource.getValidator());
  }

  @Override
  protected void performNotificationResource(final String language,
      final FormInstance resource, final NotificationResourceData notificationResourceData) {
    // do nothing ???
  }

  @Override
  protected String getResourceURL(final FormInstance resource) {
    return URLManager.getComponentInstanceURL(resource.getComponentInstanceId()) +
        "/ViewRequest?Id=" + resource.getId();
  }

  protected String getSenderName() {
    return senderName;
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    if (NotifAction.PENDING_VALIDATION.equals(action)) {
      return getResource().getCreatorId();
    }
    return getResource().getValidatorId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "formsOnline.notifLinkLabel";
  }
}