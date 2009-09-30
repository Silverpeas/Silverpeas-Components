/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * QuickInfoSessionController.java
 *
 * Created on 8 décembre 2000, 10:21
 */

package com.stratelia.webactiv.quickinfo.control;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.quickinfo.QuickInfoContentManager;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * 
 * @author squere
 * @version
 */
public class QuickInfoSessionController extends
    AbstractComponentSessionController {
  private ResourceLocator message = null;
  private ResourceLocator settings = null;
  private PublicationBm publicationBm = null;
  private QuickInfoContentManager pdcManager = null;

  private int pageId = PAGE_HEADER;

  public static final int PAGE_HEADER = 1;
  public static final int PAGE_CLASSIFY = 2;

  /** Creates new QuickInfoSessionController */
  public QuickInfoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.webactiv.quickinfo.multilang.quickinfo", null,
        "com.stratelia.webactiv.quickinfo.settings.quickInfoSettings");
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("quickinfo",
            "QuickInfoSessionController.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  /**
   * 
   * methods for Users
   * 
   */

  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  // Metier
  public Collection getQuickInfos() throws RemoteException {
    List result = (List) getPublicationBm().getOrphanPublications(
        new PublicationPK("", this.getSpaceId(), this.getComponentId()));
    return sortByDateDesc(result);
  }

  public Collection getVisibleQuickInfos() throws RemoteException {
    // return m_quickInfoSilverObject.getVisibleQuickInfos();
    Collection quickinfos = getQuickInfos();
    ArrayList result = new ArrayList();
    Iterator qi = quickinfos.iterator();

    Date now = new Date();

    try {
      SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
      now = format.parse(format.format(now));
    } catch (Exception e) {
      SilverTrace.error("quickinfo",
          "QuickInfoSessionController.getVisibleQuickInfos()",
          "quickinfo.PARSE_ERROR", e);
    }

    while (qi.hasNext()) {
      PublicationDetail detail = (PublicationDetail) qi.next();
      if (detail.getEndDate() == null) {
        if (detail.getBeginDate() == null)
          result.add(detail);
        else if (detail.getBeginDate().compareTo(now) <= 0)
          result.add(detail);
      } else {
        if (detail.getBeginDate() == null) {
          if (detail.getEndDate().compareTo(now) >= 0)
            result.add(detail);
        } else if ((detail.getEndDate().compareTo(now) >= 0)
            && (detail.getBeginDate().compareTo(now) <= 0))
          result.add(detail);
      }
    }
    return sortByDateDesc(result);
  }

  public PublicationDetail getDetail(String id) throws RemoteException {
    PublicationDetail result = getPublicationBm().getDetail(
        new PublicationPK(id, this.getSpaceId(), this.getComponentId()));
    return result;
  }

  public void add(String name, String description, Date begin, Date end)
      throws RemoteException, CreateException, WysiwygException {
    // m_quickInfoSilverObject.add (name, description, begin, end);
    if (name == null)
      throw new javax.ejb.CreateException("titreObligatoire");
    if (name.length() == 0)
      throw new javax.ejb.CreateException("titreObligatoire");

    if ((begin != null) && (end != null)) {
      if (begin.compareTo(end) > 0) {
        throw new javax.ejb.CreateException("dateDebutAvantDateFin");
      }
    }

    PublicationDetail detail = new PublicationDetail(new PublicationPK(
        "unknown", getSpaceId(), getComponentId()), name, null,
        new java.util.Date(), begin, end, getUserId(), 1, "", "", "");
    try {
      // Create the Publication
      PublicationPK pubPK = getPublicationBm().createPublication(detail);

      try {
        getQuickInfoContentManager().createSilverContent(null, detail,
            getUserId(), true);
      } catch (ContentManagerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Add the wysiwyg content
      WysiwygController.createFileAndAttachment(description, getSpaceId(),
          getComponentId(), pubPK.getId());
    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.add()",
          "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  public void update(String id, String name, String description, Date begin,
      Date end) throws RemoteException, javax.ejb.CreateException,
      WysiwygException {
    if (name == null)
      throw new javax.ejb.CreateException("titreObligatoire");
    if (name.length() == 0)
      throw new javax.ejb.CreateException("titreObligatoire");

    if ((begin != null) && (end != null)) {
      if (begin.compareTo(end) > 0)
        throw new javax.ejb.CreateException("dateDebutAvantDateFin");
    }

    PublicationDetail detail = new PublicationDetail(new PublicationPK(id, this
        .getSpaceId(), this.getComponentId()), name, null,
        new java.util.Date(), begin, end, getUserId(), 1, "", "", "");

    try {
      // Update the Publication
      getPublicationBm().setDetail(detail);

      try {
        getQuickInfoContentManager()
            .updateSilverContentVisibility(detail, true);
      } catch (ContentManagerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Update the Wysiwyg if exists, create one otherwise
      if (detail.getWysiwyg() != null && !"".equals(detail.getWysiwyg()))
        WysiwygController.updateFileAndAttachment(description, getSpaceId(),
            getComponentId(), id, getUserId());
      else
        WysiwygController.createFileAndAttachment(description, getSpaceId(),
            getComponentId(), id);

    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.update()",
          "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  public void remove(String id) throws RemoteException, WysiwygException,
      UtilException {
    try {
      PublicationPK pubPK = new PublicationPK(id, getComponentId());

      // Delete Publication
      getPublicationBm().removePublication(pubPK);

      try {
        getQuickInfoContentManager().deleteSilverContent(null, pubPK);
      } catch (ContentManagerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Delete the Wysiwyg if exists
      if (WysiwygController.haveGotWysiwyg(getSpaceId(), getComponentId(), id))
        FileFolderManager.deleteFile(WysiwygController.getWysiwygPath(
            getComponentId(), id));
    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.remove('" + id
          + "')", "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  public ResourceLocator getMessage() {
    try {
      String langue = getLanguage();
      message = new ResourceLocator(
          "com.stratelia.webactiv.quickinfo.multilang.quickinfo", langue);
    } catch (Exception e) {
      SilverTrace.error("quickinfo", "NewsSessionControl.getMessage()",
          "quickinfo.CANT_GET_LANGUAGE", e);
      if (message == null)
        message = new ResourceLocator(
            "com.stratelia.webactiv.quickinfo.multilang.quickinfo", "fr");
    }
    return message;
  }

  public ResourceLocator getSettings() {
    if (settings == null)
      settings = new ResourceLocator(
          "com.stratelia.webactiv.quickinfo.settings.quickInfoSettings", "");
    return settings;
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null)
      return "yes".equals(value.toLowerCase());
    return false;
  }

  public int getPageId() {
    return pageId;
  }

  public void setPageId(int pageId) {
    this.pageId = pageId;
  }

  public int getSilverObjectId(String objectId) {
    return getQuickInfoContentManager().getSilverObjectId(objectId,
        getComponentId());
  }

  public void close() {
    try {
      if (publicationBm != null)
        publicationBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("quickInfoSession", "QuickInfoSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("quickInfoSession", "QuickInfoSessionController.close",
          "", e);
    }
  }

  public void index() throws RemoteException {
    Collection infos = getQuickInfos();
    PublicationDetail detail = null;
    for (Iterator i = infos.iterator(); i.hasNext();) {
      detail = (PublicationDetail) i.next();
      getPublicationBm().createIndex(detail.getPK());
    }
  }

  public Collection sortByDateDesc(List alPubDetails) {
    Comparator comparator = QuickInfoDateComparatorDesc.comparator;

    Collections.sort(alPubDetails, comparator);

    return alPubDetails;
  }

  private QuickInfoContentManager getQuickInfoContentManager() {
    if (pdcManager == null)
      pdcManager = new QuickInfoContentManager();

    return pdcManager;
  }
}