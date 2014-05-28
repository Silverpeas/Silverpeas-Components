/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJBException;
import javax.xml.bind.JAXBException;

import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceFactory;
import org.silverpeas.wysiwyg.WysiwygException;

import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.thumbnail.ThumbnailSettings;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * @author squere
 * @version
 */
public class QuickInfoSessionController extends AbstractComponentSessionController {

  private PublicationBm publicationBm = null;
  private QuickInfoComponentSettings instanceSettings = null;
  
  /**
   * Creates new QuickInfoSessionController
   *
   * @param mainSessionCtrl
   * @param componentContext
   */
  public QuickInfoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        QuickInfoComponentSettings.MESSAGES_PATH, QuickInfoComponentSettings.ICONS_PATH,
        QuickInfoComponentSettings.SETTINGS_PATH);
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        SilverTrace.error("quickinfo", "QuickInfoSessionController.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }
  
  public QuickInfoComponentSettings getInstanceSettings() {
    if (instanceSettings == null) {
      ComponentInstLight app = getOrganisationController().getComponentInstLight(getComponentId()); 
      instanceSettings = new QuickInfoComponentSettings(app.getDescription(getLanguage()));
    }
    instanceSettings.setCommentsEnabled(StringUtil
        .getBooleanValue(getComponentParameterValue(QuickInfoComponentSettings.PARAM_COMMENTS)));
    instanceSettings.setTaxonomyEnabled(StringUtil
        .getBooleanValue(getComponentParameterValue(QuickInfoComponentSettings.PARAM_TAXONOMY)));
    return instanceSettings;
  }

  // Metier
  public List<News> getQuickInfos() throws RemoteException {
    List<News> allNews = getService().getAllNews(getComponentId());
    return sortByDateDesc(allNews);
  }

  public List<News> getVisibleQuickInfos() throws RemoteException {
    List<News> quickinfos = getQuickInfos();
    List<News> result = new ArrayList<News>();
    
    for (News news : quickinfos) {
      if (news.isVisible()) {
        result.add(news);
      }
    }
    return result;
  }

  public News getDetail(String id) {
    return getService().getANews(new PublicationPK(id, getComponentId()));
  }

  /**
   * Create a new quick info (PublicationDetail)
   *
   * @param name the quick info name
   * @param description the quick info description
   * @param begin the start visibility date time
   * @param end the end visibility date time
   * @param positions the JSON positions
   */
  public String add(News news, String positions) {
    List<PdcPosition> pdcPositions = getPositionsFromJSON(positions);
    
    news.setCreatorId(getUserId());
    news.setComponentInstanceId(getComponentId());
    
    return getService().addNews(news, pdcPositions);    
  }
  
  private QuickInfoService getService() {
    return QuickInfoServiceFactory.getQuickInfoService();
  }

  public void update(String id, News updatedNews) {    
    News news = getDetail(id);
    news.setTitle(updatedNews.getTitle());
    news.setDescription(updatedNews.getDescription());
    news.setContent(updatedNews.getContent());
    news.setVisibilityPeriod(updatedNews.getVisibilityPeriod());
    news.setUpdaterId(getUserId());
    news.setBroadcastModes(updatedNews.getBroadcastModes());
    
    getService().updateNews(news);      
  }

  public void remove(String id) throws RemoteException, WysiwygException, UtilException {
    PublicationPK pubPK = new PublicationPK(id, getComponentId());

    getService().removeNews(pubPK);
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
  }

  @Override
  public void close() {
    if (publicationBm != null) {
      publicationBm = null;
    }
  }

  public void index() throws RemoteException {
    List<News> infos = getQuickInfos();
    for (News news : infos) {
      getPublicationBm().createIndex(news.getPublication().getPK());
    }
  }
  
  public ThumbnailSettings getThumbnailSettings() {
    ThumbnailSettings settings = new ThumbnailSettings();
    settings.setMandatory(StringUtil.getBooleanValue(getComponentParameterValue("thumbnailMandatory")));
    setThumbnailDimensions(settings);
    return settings;
  }
  
  public void setThumbnailDimensions(ThumbnailSettings settings) {
    int width = getInt(getComponentParameterValue("thumbnailWidthSize"));
    int height = getInt(getComponentParameterValue("thumbnailHeightSize"));

    if (width == -1 && height == -1) {
      // get global settings if undefined on instance level
      width = getSettings().getInteger("thumbnail.width", 200);
      height = getSettings().getInteger("thumbnail.height", -1);
    }
    settings.setWidth(width);
    settings.setHeight(height);
  }
  
  public Boolean isSubscriberUser() {
    Boolean subscriber = null;
    if (!getUserDetail().isAccessGuest()) {
      SubscriptionService subscriptionService = SubscriptionServiceFactory.getFactory().
          getSubscribeService();
      boolean isUserSubscribed = subscriptionService.existsSubscription(
          new ComponentSubscription(getUserId(), getComponentId()));
      subscriber = new Boolean(isUserSubscribed);
    }
    return subscriber;
  }
  
  private int getInt(String str) {
    if (StringUtil.isInteger(str)) {
      return Integer.parseInt(str);
    }
    return -1;
  }

  private List<News> sortByDateDesc(List<News> listOfNews) {
    Comparator<News> comparator = QuickInfoDateComparatorDesc.comparator;
    Collections.sort(listOfNews, comparator);
    return listOfNews;
  }

  /**
   * Classify the info letter publication on the PdC only if the positions parameter is filled
   *
   * @param positions the string json positions
   */
  private List<PdcPosition> getPositionsFromJSON(String positions) {
    List<PdcPosition> pdcPositions = null;
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity qiClassification = null;
      try {
        qiClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (JAXBException e) {
        SilverTrace.error("quickInfo", "QuickInfoSessionController.classifyQuickInfo",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (qiClassification != null && !qiClassification.isUndefined()) {
        pdcPositions = qiClassification.getPdcPositions();
      }
    }
    return pdcPositions;
  }
  
}
