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

import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Holder of a versioned attachment for the ODTDocumentBuilder.
 * It provides useful operation on the holded versioned attachment for the ODTDocumentBuilder
 * instances.
 */
class VersionedAttachmentHolder {

  public static VersionedAttachmentHolder hold(final Document versionedAttachment) {
    return new VersionedAttachmentHolder(versionedAttachment);
  }
  private final Document versionedAttachment;

  private VersionedAttachmentHolder(final Document versionedAttachment) {
    this.versionedAttachment = versionedAttachment;
  }

  /**
   * Is the specified user has the priviledges to access the holded attachment?
   * @param user the user.
   * @return true if the user is authorized to access the holded attachment, false otherwise.
   */
  public boolean isUserAuthorized(final UserDetail user) {
    VersioningUtil versioningService = getVersioningService();
    int userId = Integer.parseInt(user.getId());
    return versioningService.isReader(versionedAttachment, userId) || versioningService.isWriter(
            versionedAttachment, userId) || user.isAccessAdmin();
  }

  /**
   * Gets the last version of the holded attachment that can be accessible by the specified user.
   * If an error occurs while getting the version of the attachment, a runtime exception
   * DocumentBuildException is thrown (as the holder is used in the context of the export document
   * build).
   * @param user the user.
   * @return the last version of the holded attachment the user can access.
   */
  public DocumentVersion getLastVersionAccessibleBy(final UserDetail user) {
    try {
      DocumentVersion documentVersion = null;
      VersioningUtil versioningService = getVersioningService();
      int userId = Integer.parseInt(user.getId());
      ArrayList<DocumentVersion> documentFilteredVersions =
              versioningService.getDocumentFilteredVersions(versionedAttachment.getPk(), userId);
      if (!documentFilteredVersions.isEmpty()) {
        documentVersion = documentFilteredVersions.get(0);
      }
      return documentVersion;
    } catch (RemoteException ex) {
      throw new DocumentBuildException(ex.getMessage(), ex);
    }
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
  public String getCreatorOrValidatorsDisplayedName(final DocumentVersion version) {
    StringBuilder creatorOrValidators = new StringBuilder();
    if (isVersionDefined(version)) {
      if (isOrderedWithApprobation(versionedAttachment)) {
        ArrayList<Worker> workers = versionedAttachment.getWorkList();
        for (Worker worker : workers) {
          if (worker.isApproval()) {
            if (creatorOrValidators.length() > 0) {
              creatorOrValidators.append(", ");
            }
            creatorOrValidators.append(getOrganizationService().getUserDetail(
                    String.valueOf(worker.getUserId())).getDisplayedName());
          }
        }
      } else {
        creatorOrValidators.append(getOrganizationService().getUserDetail(
                String.valueOf(version.getAuthorId())).getDisplayedName());
      }
    } else {
      int creatorId = versionedAttachment.getOwnerId();
      creatorOrValidators.append(getOrganizationService().getUserDetail(String.valueOf(creatorId)).
              getDisplayedName());
    }
    return creatorOrValidators.toString();
  }
  
  /**
   * Gets the version number of the specified version of the holded attachment.
   * @param version a version of the holded attachment.
   * @return the version number (major number plus minor number) of the specified version.
   */
  public String getVersionNumber(final DocumentVersion version) {
    String versionNumber = "";
    if (isVersionDefined(version)) {
      versionNumber = version.getMajorNumber() + "." + version.getMinorNumber();
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
  private boolean isVersionDefined(final DocumentVersion version) {
    return version.getSize() != 0 || !"dummy".equals(version.getLogicalName());
  }

  private boolean isOrderedWithApprobation(final Document versionedAttachment) {
    return 2 == versionedAttachment.getTypeWorkList();
  }

  private VersioningUtil getVersioningService() {
    return new VersioningUtil();
  }

  private OrganizationController getOrganizationService() {
    return new OrganizationController();
  }
}
