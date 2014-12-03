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
package com.silverpeas.kmelia.notification;

import org.silverpeas.attachment.model.SimpleDocument;
import static org.silverpeas.util.StringUtil.isDefined;

import com.silverpeas.usernotification.model.NotificationResourceData;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.FileRepositoryManager;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationDetail;

import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaDocumentSubscriptionPublicationUserNotification extends
    AbstractKmeliaPublicationUserNotification {

  private final SimpleDocument document;

  public KmeliaDocumentSubscriptionPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final SimpleDocument document, final String senderName) {
    super(nodePK, resource, NotifAction.REPORT, senderName);
    this.document = document;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "AlertDocument";
  }

  @Override
  protected String getFileName() {
    return "notificationAttachment";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return null;
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);

    template.setAttribute("attachmentFileName", document.getFilename());
    if (isDefined(document.getTitle())) {
      template.setAttribute("attachmentTitle", document.getTitle());
    }
    if (isDefined(document.getDescription())) {
      template.setAttribute("attachmentDesc", document.getDescription());
    }
    template
        .setAttribute("attachmentCreationDate", DateUtil.getOutputDate(document.getCreated(), language));
    template.setAttribute("attachmentSize", FileRepositoryManager.formatFileSize(document.getSize()));

    String author = document.getUpdatedBy();
    if(!StringUtil.isDefined(author)) {
      author = document.getCreatedBy();
    }
    final UserDetail authorDetail = getOrganisationController().getUserDetail(author);
    template.setAttribute("attachmentAuthor", authorDetail.getFirstName() + " " + authorDetail.getLastName());

    if (document.isVersioned()) {
      template.setAttribute("attachmentMajorNumber", document.getMajorVersion());
      template.setAttribute("attachmentMinorNumber", document.getMinorVersion());
    }
  }

  @Override
  protected void performNotificationResource(final String language, final PublicationDetail resource,
      final NotificationResourceData notificationResourceData) {
    super.performNotificationResource(language, resource, notificationResourceData);

    final StringBuilder sb = new StringBuilder(1024);
    if (isDefined(document.getTitle())) {
      sb.append(document.getTitle());
    }
    if (sb.length() > 0) {
      sb.append(" - ");
    }
    sb.append(document.getFilename());

    sb.insert(0, " - ");
    sb.insert(0, notificationResourceData.getResourceName());
    notificationResourceData.setResourceName(sb.toString());
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getDocumentUrl(resource, document);
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "kmelia.notifDocumentLinkLabel";
  }
}
