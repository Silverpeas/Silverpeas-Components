/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.quickinfo.control;

import java.rmi.RemoteException;
import java.sql.Connection;
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
import javax.xml.bind.JAXBException;

import org.silverpeas.wysiwyg.WysiwygException;
import org.silverpeas.wysiwyg.control.WysiwygController;

import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.quickinfo.QuickInfoContentManager;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * @author squere
 * @version
 */
public class QuickInfoSessionController extends AbstractComponentSessionController {

  private ResourceLocator message = null;
  private ResourceLocator settings = new ResourceLocator(
      "org.silverpeas.quickinfo.settings.quickInfoSettings", "");
  private PublicationBm publicationBm = null;
  private QuickInfoContentManager pdcManager = null;
  private int pageId = PAGE_HEADER;
  public static final int PAGE_HEADER = 1;
  public static final int PAGE_CLASSIFY = 2;

  /**
   * Creates new QuickInfoSessionController
   */
  public QuickInfoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.quickinfo.multilang.quickinfo", null,
        "org.silverpeas.quickinfo.settings.quickInfoSettings");
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = (EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("quickinfo", "QuickInfoSessionController.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  /**
   * methods for Users
   */
  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  // Metier
  public Collection<PublicationDetail> getQuickInfos() throws RemoteException {
    List<PublicationDetail> result =
        new ArrayList<PublicationDetail>(getPublicationBm().getOrphanPublications(
        new PublicationPK("", this.getSpaceId(), this.getComponentId())));
    return sortByDateDesc(result);
  }

  public Collection<PublicationDetail> getVisibleQuickInfos() throws RemoteException {
    // return m_quickInfoSilverObject.getVisibleQuickInfos();
    Collection<PublicationDetail> quickinfos = getQuickInfos();
    List<PublicationDetail> result = new ArrayList<PublicationDetail>();
    Iterator<PublicationDetail> qi = quickinfos.iterator();

    Date now = new Date();

    try {
      SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
      now = format.parse(format.format(now));
    } catch (Exception e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.getVisibleQuickInfos()",
          "quickinfo.PARSE_ERROR", e);
    }

    while (qi.hasNext()) {
      PublicationDetail detail = (PublicationDetail) qi.next();
      if (detail.getEndDate() == null) {
        if (detail.getBeginDate() == null) {
          result.add(detail);
        } else if (detail.getBeginDate().compareTo(now) <= 0) {
          result.add(detail);
        }
      } else {
        if (detail.getBeginDate() == null) {
          if (detail.getEndDate().compareTo(now) >= 0) {
            result.add(detail);
          }
        } else if ((detail.getEndDate().compareTo(now) >= 0)
            && (detail.getBeginDate().compareTo(now) <= 0)) {
          result.add(detail);
        }
      }
    }
    return sortByDateDesc(result);
  }

  public PublicationDetail getDetail(String id) throws RemoteException {
    PublicationDetail result = getPublicationBm().getDetail(
        new PublicationPK(id, this.getSpaceId(), this.getComponentId()));
    return result;
  }

  /**
   * Create a new quick info (PublicationDetail)
   *
   * @param name the quick info name
   * @param description the quick info description
   * @param begin the start visibility date time
   * @param end the end visibility date time
   * @param positions the JSON positions
   * @throws RemoteException
   * @throws CreateException
   * @throws WysiwygException
   */
  public void add(String name, String description, Date begin, Date end, String positions)
      throws RemoteException, CreateException, WysiwygException {
    validateInformations(name, begin, end);

    PublicationDetail detail = new PublicationDetail(new PublicationPK("unknown", getSpaceId(),
        getComponentId()), name, null, new Date(), begin, end, getUserId(), 1, "", "", "");
    try {
      // Create the Publication
      PublicationPK pubPK = getPublicationBm().createPublication(detail);

      try {
        getQuickInfoContentManager().createSilverContent(null, detail, getUserId(), true);
      } catch (ContentManagerException e) {
        SilverTrace.error("quickinfo", "QuickInfoSessionController.add()",
            "root.ContentManagerException", e);
      }
      // Add the wysiwyg content
      WysiwygController.createFileAndAttachment(description, pubPK, getUserId(),
          I18NHelper.defaultLanguage);
      classifyQuickInfo(detail, positions);
    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.add()",
          "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  public void update(String id, String name, String description, Date begin, Date end)
      throws RemoteException, javax.ejb.CreateException, WysiwygException {
    validateInformations(name, begin, end);

    PublicationDetail detail = new PublicationDetail(new PublicationPK(id, getSpaceId(),
        getComponentId()), name, null, new java.util.Date(), begin, end, getUserId(), 1, "",
        "", "");

    try {
      // Update the Publication
      getPublicationBm().setDetail(detail);

      try {
        getQuickInfoContentManager().updateSilverContentVisibility(detail, true);
      } catch (ContentManagerException e) {
        SilverTrace.error("quickinfo", "QuickInfoSessionController.update()",
            "root.ContentManagerException", e);
      }

      // Update the Wysiwyg if exists, create one otherwise
      if (detail.getWysiwyg() != null && !"".equals(detail.getWysiwyg())) {
        WysiwygController.updateFileAndAttachment(description, getComponentId(), id, getUserId(),
            I18NHelper.defaultLanguage);
      } else {
        WysiwygController.createFileAndAttachment(description, detail.getPK(), getUserId(),
            I18NHelper.defaultLanguage);
      }

    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.update()",
          "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  private void validateInformations(String name, Date begin, Date end)
      throws CreateException {
    if (!StringUtil.isDefined(name)) {
      throw new CreateException("titreObligatoire");
    }

    if ((begin != null) && (end != null)) {
      if (begin.after(end)) {
        throw new CreateException("dateDebutAvantDateFin");
      }
    }
  }

  public void remove(String id) throws RemoteException, WysiwygException, UtilException {
    Connection connection = null;

    try {
      PublicationPK pubPK = new PublicationPK(id, getComponentId());

      // Delete Publication
      getPublicationBm().removePublication(pubPK);

      try {
        connection = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);

        getQuickInfoContentManager().deleteSilverContent(connection, pubPK);
      } catch (ContentManagerException e) {
        SilverTrace.error("quickinfo", "QuickInfoSessionController.remove('" + id + "')",
            "ContentManagerExceptino", e);
      } finally {
        DBUtil.close(connection);
      }

      // Delete the Wysiwyg if exists
      if (WysiwygController.haveGotWysiwyg(getComponentId(), id, I18NHelper.defaultLanguage)) {
        WysiwygController.deleteFileAndAttachment(getComponentId(), id);
      }
    } catch (RemoteException e) {
      SilverTrace.error("quickinfo", "QuickInfoSessionController.remove('" + id + "')",
          "root.REMOTE_EXCEPTION", e);
      throw e;
    }
  }

  public ResourceLocator getMessage() {
    try {
      message = new ResourceLocator("org.silverpeas.quickinfo.multilang.quickinfo", getLanguage());
    } catch (Exception e) {
      SilverTrace.error("quickinfo", "NewsSessionControl.getMessage()",
          "quickinfo.CANT_GET_LANGUAGE", e);
      if (message == null) {
        message = new ResourceLocator("org.silverpeas.quickinfo.multilang.quickinfo",
            I18NHelper.defaultLanguage);
      }
    }
    return message;
  }

  public ResourceLocator getSettings() {
    return settings;
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
  }

  public int getPageId() {
    return pageId;
  }

  public void setPageId(int pageId) {
    this.pageId = pageId;
  }

  public int getSilverObjectId(String objectId) {
    return getQuickInfoContentManager().getSilverObjectId(objectId, getComponentId());
  }

  public void close() {
    try {
      if (publicationBm != null) {
        publicationBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("quickInfoSession", "QuickInfoSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("quickInfoSession", "QuickInfoSessionController.close", "", e);
    }
  }

  public void index() throws RemoteException {
    Collection<PublicationDetail> infos = getQuickInfos();
    for (PublicationDetail publicationDetail : infos) {
      getPublicationBm().createIndex(publicationDetail.getPK());
    }
  }

  public Collection<PublicationDetail> sortByDateDesc(List<PublicationDetail> alPubDetails) {
    Comparator<PublicationDetail> comparator = QuickInfoDateComparatorDesc.comparator;
    Collections.sort(alPubDetails, comparator);
    return alPubDetails;
  }

  private QuickInfoContentManager getQuickInfoContentManager() {
    if (pdcManager == null) {
      pdcManager = new QuickInfoContentManager();
    }
    return pdcManager;
  }

  /**
   * Classify the info letter publication on the PdC only if the positions parameter is filled
   *
   * @param publi the quickInfo PublicationDetail to classify
   * @param positions the string json positions
   */
  private void classifyQuickInfo(PublicationDetail publi, String positions) {
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity qiClassification = null;
      try {
        qiClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (JAXBException e) {
        SilverTrace.error("quickInfo", "QuickInfoSessionController.classifyQuickInfo",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (qiClassification != null && !qiClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = qiClassification.getPdcPositions();
        String qiId = publi.getPK().getId();
        PdcClassification classification = aPdcClassificationOfContent(qiId,
            publi.getInstanceId()).withPositions(pdcPositions);
        if (!classification.isEmpty()) {
          PdcClassificationService service =
              PdcServiceFactory.getFactory().getPdcClassificationService();
          classification.ofContent(qiId);
          service.classifyContent(publi, classification);
        }
      }
    }
  }
}