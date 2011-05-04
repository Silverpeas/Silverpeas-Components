/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/legal/licensing"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.model;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A publication as defined in a Kmelia component.
 * A publication in Kmelia can be positionned in the PDC, it can have
 * attachments and it can be commented.
 */
public class KmeliaPublication implements Serializable {

  private static final long serialVersionUID = 4861635754389280165L;
  private PublicationDetail detail;
  private CompletePublication completeDetail;
  private boolean versioned = false;

  public static KmeliaPublication aKmeliaPublicationWithId(final PublicationPK id) {
    return new KmeliaPublication(id);
  }
  
  /**
   * Sets this publication as versionned.
   * @return itself.
   */
  public KmeliaPublication versioned() {
    this.versioned = true;
    return this;
  }
  
  /**
   * Is this publication versionned?
   * @return true if this publication is versionned, false otherwise.
   */
  public boolean isVersioned() {
    return this.versioned;
  }

  public PublicationPK getId() {
    return id;
  }

  /**
   * Gets the details about this publication.
   * @return the publication details.
   */
  public PublicationDetail getDetail() {
    if (detail == null) {
      try {
        detail = getKmeliaService().getPublicationDetail(id);
      } catch (RemoteException ex) {
        throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getDetail()",
                SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", ex);
      }
    }
    return detail;
  }

  /**
   * Gets the complete detail about this publication.
   * @return the publication complete details.
   */
  public CompletePublication getCompleteDetail() {
    if (completeDetail == null) {
      try {
        completeDetail = getKmeliaService().getCompletePublication(id);
      } catch (RemoteException ex) {
        throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getCompleteDetail()",
                SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", ex);
      }
    }
    return completeDetail;
  }

  /**
   * Gets the creator of this publication (the initial author).
   * @return the detail about the creator of this publication.
   */
  public UserDetail getCreator() {
    String creatorId = getDetail().getCreatorId();
    return getOrganizationService().getUserDetail(creatorId);
  }

  /**
   * Gets the user that has lastly modified this publication.
   * @return the detail about the last modifier of this publication.
   */
  public UserDetail getLastModifier() {
    String modifierId = getDetail().getUpdaterId();
    return getOrganizationService().getUserDetail(modifierId);
  }

  /**
   * Gets the comments on this publication.
   * @return an unmodifiable list with the comments on this publication.
   */
  public List<Comment> getComments() {
    return Collections.unmodifiableList(getCommentService().getAllCommentsOnPublication(id));
  }

  /**
   * Gets the attachments that belong to this publication and that were uploaded by a user.
   * If this publication is versioned, this method isn't supported. In that case, please use the
   * getVersionnedAttachments() method instead.
   * @return an unmodifiable list with the details of each uploaded attachment of this publication.
   */
  public List<AttachmentDetail> getAttachments() {
    if (isVersioned()) {
      throw new UnsupportedOperationException();
    }
    try {
      return Collections.unmodifiableList(new ArrayList<AttachmentDetail>(getKmeliaService().
              getAttachments(id)));
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getAttachments()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", ex);
    }
  }
  
  /**
   * Gets the versioned attachments that belongs to this publication.
   * If this publication isn't versioned, this method is then not supported. In that case, please
   * use the getAttachments() method instead.
   * @return an unmodifiable list with the versioned documents attached to this publication.
   */
  public List<Document> getVersionedAttachments() {
    if (!isVersioned()) {
      throw new UnsupportedOperationException();
    }
    try {
      return Collections.unmodifiableList(getVersioningService().getDocuments(new ForeignPK(id)));
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getVersionedAttachments()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", ex);
    }
  }

  /**
   * Gets the positions in the PDC of this publication.
   * @return an unmodifiable list with the PDC positions of this publication.
   */
  public List<ClassifyPosition> getPDCPositions() {
    try {
      int silverObjectId = getKmeliaService().getSilverObjectId(id);
      return getKmeliaService().getPdcBm().getPositions(silverObjectId, id.getInstanceId());
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getPDCPositions()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_POSTIONSPDC", ex);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KmeliaPublication other = (KmeliaPublication) obj;
    if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
    return hash;
  }
  private static final OrganizationController organisationService = new OrganizationController();
  private final PublicationPK id;

  private KmeliaPublication(PublicationPK id) {
    this.id = id;
  }

  private KmeliaBm getKmeliaService() {
    KmeliaBm KmeliaBm = null;
    try {
      KmeliaBmHome KmeliaBmHome = (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(
              JNDINames.KMELIABM_EJBHOME,
              KmeliaBmHome.class);
      KmeliaBm = KmeliaBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getKmeliaBm()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_KmeliaBm_HOME", e);
    }
    return KmeliaBm;
  }

  private CommentService getCommentService() {
    return CommentServiceFactory.getFactory().getCommentService();
  }
  
  private VersioningUtil getVersioningService() {
    return new VersioningUtil();
  }

  private OrganizationController getOrganizationService() {
    return organisationService;
  }
}
