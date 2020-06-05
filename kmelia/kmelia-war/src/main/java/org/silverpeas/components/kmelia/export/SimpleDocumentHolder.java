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
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.export;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import org.silverpeas.core.util.StringUtil;

import static org.silverpeas.core.util.DateUtil.getOutputDate;

/**
 * Holder of a simple document for the ODTDocumentBuilder. It provides useful operation on the
 * holden simple document for the ODTDocumentBuilder instances.
 */
class SimpleDocumentHolder {

  public static SimpleDocumentHolder hold(final SimpleDocument document) {
    return new SimpleDocumentHolder(document);
  }
  private final SimpleDocument document;

  private SimpleDocumentHolder(final SimpleDocument versionedAttachment) {
    this.document = versionedAttachment;
  }

  /**
   * Gets the name of the creator or of each validators of the version of the holden
   * attachment. If the version isn't well defined (it is a dummy version or the document
   * corresponding to the version is empty), then the creator of the holden attachment itself is
   * returned.
   * @return the display name of the creator or of the validators. In the case of validators, each
   * of their name is separated by a comma.
   */
  public String getAuthorFullName() {
    String author = document.getUpdatedBy();
    if (!StringUtil.isDefined(author)) {
      author = document.getCreatedBy();
    }
    return OrganizationControllerProvider.getOrganisationController().getUserDetail(author)
        .getDisplayedName();
  }

  /**
   * Returns the last modification date of the simple document.
   *
   * @param lang the language used for formatting.
   * @return the last modification date of the simple document.
   */
  public String getLastModification(String lang) {
    return getOutputDate(document.getUpdated(), lang);
  }

  /**
   * Gets the version number of the version of the holden attachment.
   * @return the version number (major number plus minor number) of the specified version.
   */
  public String getVersionNumber() {
    String versionNumber = "";
    if (document.isVersioned()) {
      versionNumber = document.getMajorVersion() + "." + document.getMinorVersion();
    }
    return versionNumber;
  }
}
