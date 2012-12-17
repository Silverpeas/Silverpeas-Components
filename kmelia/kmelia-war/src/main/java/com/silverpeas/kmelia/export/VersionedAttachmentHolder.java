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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.util.StringUtil;

import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * Holder of a versioned attachment for the ODTDocumentBuilder.
 * It provides useful operation on the holded versioned attachment for the ODTDocumentBuilder
 * instances.
 */
class VersionedAttachmentHolder {

  public static VersionedAttachmentHolder hold(final SimpleDocument versionedAttachment) {
    return new VersionedAttachmentHolder(versionedAttachment);
  }
  private final SimpleDocument versionedAttachment;

  private VersionedAttachmentHolder(final SimpleDocument versionedAttachment) {
    this.versionedAttachment = versionedAttachment;
  }

  /**
   * Gets the name of the creator or of each validators of the specified version of the holded 
   * attachment.
   * If the version isn't well defined (it is a dummy version or the document corresponding to
   * the version is empty), then the creator of the holded attachment iself is returned.
   * @param version a version of the holded attachment.
   * @return the display name of the creator or of the validators. In the case of validators, each
   * of their name is separated by a comma.
   */
  public String getCreatorOrValidatorsDisplayedName(final SimpleDocument version) {
    StringBuilder creatorOrValidators = new StringBuilder();
    if (isVersionDefined(version) && !version.isReadOnly()) {
      String author = version.getUpdatedBy();
      if(!StringUtil.isDefined(author)) {
        author = version.getCreatedBy();
      }
      creatorOrValidators.append(getOrganizationService().getUserDetail(author).getDisplayedName());
    } else {
      String author = versionedAttachment.getEditedBy();
      creatorOrValidators.append(getOrganizationService().getUserDetail(author).getDisplayedName());
    }
    return creatorOrValidators.toString();
  }
  
  /**
   * Gets the version number of the specified version of the holded attachment.
   * @param version a version of the holded attachment.
   * @return the version number (major number plus minor number) of the specified version.
   */
  public String getVersionNumber(final SimpleDocument version) {
    String versionNumber = "";
    if (isVersionDefined(version)) {
      versionNumber = version.getMajorVersion() + "." + version.getMinorVersion();
    }
    return versionNumber;
  }

  /**
   * Is the specified version of the holded attachment is well defined?
   * A version is well defined if the document corresponding to the specified version exists (not
   * dummy) and isn't empty.
   * @param version the version of the holded attachment.
   * @return true if the version is well defined, false otherwise.
   */
  private boolean isVersionDefined(final SimpleDocument version) {
    return version.getSize() != 0L || !"dummy".equals(version.getFile());
  }
  
  private OrganizationController getOrganizationService() {
    return new OrganizationController();
  }
}
