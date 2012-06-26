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
package com.silverpeas.kmelia.notification;

import static com.silverpeas.util.StringUtil.isDefined;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaAttachmentSubscriptionPublicationUserNotification extends
    KmeliaSubscriptionPublicationUserNotification {

  private final AttachmentDetail attachmentDetail;

  public KmeliaAttachmentSubscriptionPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final AttachmentDetail attachmentDetail, final String senderName) {
    super(nodePK, resource, NotifAction.REPORT, senderName);
    this.attachmentDetail = attachmentDetail;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "AlertAttachment";
  }

  @Override
  protected String getFileName() {
    return "notificationAttachment";
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);

    template.setAttribute("attachmentFileName", attachmentDetail.getLogicalName(language));
    if (isDefined(attachmentDetail.getTitle(language))) {
      template.setAttribute("attachmentTitle", attachmentDetail.getTitle(language));
    }
    if (isDefined(attachmentDetail.getInfo(language))) {
      template.setAttribute("attachmentDesc", attachmentDetail.getInfo(language));
    }
    template.setAttribute("attachmentCreationDate", DateUtil.getOutputDate(attachmentDetail.
        getCreationDate(), language));
    template.setAttribute("attachmentSize", attachmentDetail.getAttachmentFileSize(language));
    final UserDetail authorDetail = getOrganizationController().getUserDetail(
        attachmentDetail.getAuthor());
    template.setAttribute("attachmentAuthor", authorDetail.getFirstName() + " " + authorDetail.
        getLastName());
  }

  @Override
  protected void performNotificationResource(final String language, final PublicationDetail resource,
      final NotificationResourceData notificationResourceData) {
    super.performNotificationResource(language, resource, notificationResourceData);

    final StringBuffer sb = new StringBuffer();
    if (isDefined(attachmentDetail.getTitle(language))) {
      sb.append(attachmentDetail.getTitle(language));
    }
    if (sb.length() > 0) {
      sb.append(" - ");
    }
    sb.append(attachmentDetail.getLogicalName(language));

    sb.insert(0, " - ");
    sb.insert(0, notificationResourceData.getResourceName());
    notificationResourceData.setResourceName(sb.toString());
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getAttachmentUrl(resource, attachmentDetail);
  }
}
