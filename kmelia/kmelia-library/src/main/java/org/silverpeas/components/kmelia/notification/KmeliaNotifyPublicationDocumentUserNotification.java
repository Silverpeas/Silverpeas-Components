/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.util.Collection;
import java.util.Collections;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaNotifyPublicationDocumentUserNotification extends
    AbstractKmeliaPublicationUserNotification {

  private final SimpleDocument document;

  public KmeliaNotifyPublicationDocumentUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final SimpleDocument document) {
    super(nodePK, resource, NotifAction.REPORT);
    this.document = document;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "AlertDocument";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationAttachment";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return Collections.emptyList();
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

    User author = getOrganisationController().getUserDetail(document.getUpdatedBy());
    if(author == null) {
      author = getOrganisationController().getUserDetail(document.getCreatedBy());
    }
    template.setAttribute("attachmentAuthor", author.getFirstName() + " " + author.getLastName());

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
    return KmeliaHelper.getDocumentUrl(resource, document, getComponentInstanceId());
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "kmelia.notifDocumentLinkLabel";
  }
}
