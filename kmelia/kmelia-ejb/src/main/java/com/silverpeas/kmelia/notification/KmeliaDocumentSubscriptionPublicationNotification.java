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

import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaDocumentSubscriptionPublicationNotification extends KmeliaSubscriptionPublicationNotification {

  private final Document document;
  private final DocumentVersion documentVersion;

  public KmeliaDocumentSubscriptionPublicationNotification(final NodePK nodePK, final PublicationDetail resource,
      final Document document, final DocumentVersion documentVersion, final String senderName) {
    super(nodePK, resource, NotifAction.REPORT, senderName);
    this.document = document;
    this.documentVersion = documentVersion;
  }

  @Override
  protected String getSubjectKey() {
    return "AlertDocument";
  }

  @Override
  protected String getFileName() {
    return "notificationAttachment";
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);

    template.setAttribute("attachmentFileName", documentVersion.getLogicalName());
    if (isDefined(document.getName())) {
      template.setAttribute("attachmentTitle", document.getName());
    }
    if (isDefined(document.getDescription())) {
      template.setAttribute("attachmentDesc", document.getDescription());
    }
    template
        .setAttribute("attachmentCreationDate", DateUtil.getOutputDate(documentVersion.getCreationDate(), language));
    template.setAttribute("attachmentSize", documentVersion.getDisplaySize());

    final UserDetail authorDetail =
        getOrganizationController().getUserDetail(Integer.toString(documentVersion.getAuthorId()));
    template.setAttribute("attachmentAuthor", authorDetail.getFirstName() + " " + authorDetail.getLastName());

    template.setAttribute("attachmentMajorNumber", documentVersion.getMajorNumber());
    template.setAttribute("attachmentMinorNumber", documentVersion.getMinorNumber());
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getDocumentUrl(resource, document);
  }
}
