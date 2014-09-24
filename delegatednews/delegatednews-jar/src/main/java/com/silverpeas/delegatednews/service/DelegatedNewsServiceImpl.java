/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.delegatednews.service;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.delegatednews.dao.DelegatedNewsDao;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.ui.DisplayI18NHelper;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.publication.model.PublicationDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.date.Period;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.silverpeas.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DelegatedNewsServiceImpl implements DelegatedNewsService {

  @Inject
  private DelegatedNewsDao dao;

  /**
   * Ajout d'une actualité déléguée
   */
  @Override
  public void submitNews(String id, SilverpeasContent news, String lastUpdaterId, Period visibilityPeriod, String userId) {
    DelegatedNews delegatedNews =
      new DelegatedNews(Integer.parseInt(id), news.getComponentInstanceId(), lastUpdaterId, new Date(),
          visibilityPeriod.getBeginDate(), visibilityPeriod.getEndDate());
    
    notifyDelegatedNewsToValidate(id, news, userId);
    
    dao.saveAndFlush(delegatedNews);
  }

  /**
   * Récupère une actualité déléguée correspondant à la publication Theme Tracker passée en
   * paramètre
   * @param pubId : l'id de la publication de Theme Tracker
   * @return DelegatedNews : l'objet correspondant à l'actualité déléguée ou null si elle n'existe
   * pas
   */
  @Override
  public DelegatedNews getDelegatedNews(int pubId) {
    return dao.findOne(Integer.valueOf(pubId));
  }

  /**
   * Récupère toutes les actualités déléguées inter Theme Tracker
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  @Override
  public List<DelegatedNews> getAllDelegatedNews() {
    Sort sort =
        new Sort(new Order(Direction.ASC, "newsOrder"), new Order(Direction.ASC, "beginDate"),
            new Order(Direction.ASC, "pubId"));
    return dao.findAll(sort);
  }

  /**
   * Récupère toutes les actualités déléguées valides inter Theme Tracker
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  @Override
  public List<DelegatedNews> getAllValidDelegatedNews() {
    return dao.findByStatus(DelegatedNews.NEWS_VALID);
  }

  /**
   * Valide l'actualité déléguée passée en paramètre
   */
  @Override
  public void validateDelegatedNews(int pubId, String validatorId) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(pubId));
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_VALID);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);
      
      notifyValidation(delegatedNews, validatorId);
    }
  }

  /**
   * Refuse l'actualité déléguée passée en paramètre
   */
  @Override
  public void refuseDelegatedNews(int pubId, String validatorId, String refusalMotive) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(pubId));
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_REFUSED);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);
      
      notifyDelegatedNewsRefused(delegatedNews, refusalMotive, validatorId);
    }
  }

  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   */
  @Override
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(pubId));
    if (delegatedNews != null) {
      delegatedNews.setBeginDate(dateHourBegin);
      delegatedNews.setEndDate(dateHourEnd);
      dao.saveAndFlush(delegatedNews);
    }
  }

  /**
   * @return
   */
  protected SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents("delegatednews");
  }

  /**
   * @return
   */
  private OrganisationController getOrganisationController() {
    // must return a new instance each time
    // This is to resolve Serializable problems
    OrganisationController orga = new OrganizationController();
    return orga;
  }
  
  private String getAppId() {
    String[] tabInstanceId = getOrganisationController().getCompoId("delegatednews");
    String delegatednewsInstanceId = null;
    for (String element : tabInstanceId) {
      delegatednewsInstanceId = element;
      break;
    }
    return delegatednewsInstanceId;
  }

  /**
   * @param componentId
   * @return
   */
  private NotificationSender getNotificationSender(String componentId) {
    // must return a new instance each time
    // This is to resolve Serializable problems
    NotificationSender notifSender = new NotificationSender(componentId);
    return notifSender;
  }

  /**
   * @param notifMetaData
   * @param senderId
   */
  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    try {
      if (!StringUtil.isDefined(notifMetaData.getSender())) {
        notifMetaData.setSender(senderId);
      }
      getNotificationSender(notifMetaData.getComponentId()).notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyUsers()",
          "delegatednews.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    }
  }

  /**
   * Notifie l'Equipe éditoriale d'une actualité à valider
   */
  private void notifyDelegatedNewsToValidate(String id, SilverpeasContent news, String senderId) {
    String delegatednewsInstanceId = getAppId();
    // Notification des membres de l'équipe éditoriale
    try {
      if (delegatednewsInstanceId == null) {
        SilverTrace.warn("delegatednews",
            "DelegatedNewsServiceImpl.notifyDelegatedNewsToValidate()",
            "delegatednews.EX_AUCUNE_INSTANCE_DISPONIBLE");
      } else {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", DisplayI18NHelper.
                getDefaultLanguage());
        String subject = message.getString("delegatednews.newsSuggest");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "delegatednewsNotificationToValidate");
        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("publicationId", id);
          template.setAttribute("publicationName", news.getTitle());
          template.setAttribute("senderName", UserDetail.getById(senderId).getDisplayedName());
          ResourceLocator localizedMessage = new ResourceLocator(
              "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", lang);
          subject = localizedMessage.getString("delegatednews.newsSuggest");
          notifMetaData.addLanguage(lang, subject, "");
        }
        List<String> roles = new ArrayList<String>();
        roles.add("admin");
        String[] editors = getOrganisationController().getUsersIdsByRoleNames(
            delegatednewsInstanceId, roles);
        for (String editorId : editors) {
          notifMetaData.addUserRecipient(new UserRecipient(editorId));
        }
        notifMetaData.setComponentId(delegatednewsInstanceId);
        notifMetaData.setLink(URLManager.getSimpleURL(URLManager.URL_PUBLI, id, false));
        notifyUsers(notifMetaData, senderId);
      }
    } catch (Exception e) {
      SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyDelegatedNewsToValidate()",
          "delegatednews.EX_IMPOSSIBLE_DALERTER_LES_EDITEURS", "pubId = " +
              news.getId() + ", pubName = " + news.getTitle(), e);
    }
  }

  /**
   * Met à jour l'actualité déléguée passée en paramètre
   */
  @Override
  public void updateDelegatedNews(String id, SilverpeasContent news, String updaterId, Period visibilityPeriod) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(id));
    if (delegatedNews != null) {
      delegatedNews.setInstanceId(news.getComponentInstanceId());
      delegatedNews.setStatus(DelegatedNews.NEWS_TO_VALIDATE);
      delegatedNews.setContributorId(updaterId);
      delegatedNews.setValidatorId(null);
      delegatedNews.setValidationDate(new Date());
      delegatedNews.setBeginDate(visibilityPeriod.getBeginDate());
      delegatedNews.setEndDate(visibilityPeriod.getEndDate());
      dao.saveAndFlush(delegatedNews);
      
      notifyDelegatedNewsToValidate(id, news, updaterId);
    }
  }

  /**
   * Supprime l'actualité déléguée passée en paramètre
   */
  @Override
  public void deleteDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(pubId));
    if (delegatedNews != null) {
      dao.delete(delegatedNews);
    }
  }

  /**
   * Notifie le dernier contributeur que l'actualité est validée
   */
  private void notifyValidation(DelegatedNews news, String senderId) {
    String delegatednewsInstanceId = getAppId();
    // Notification du dernier contributeur
    try {
      if (delegatednewsInstanceId == null) {
        SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyDelegatedNewsValid()",
            "delegatednews.EX_AUCUNE_INSTANCE_DISPONIBLE");
      } else {
        PublicationDetail publication = news.getPublicationDetail();
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", DisplayI18NHelper.
                getDefaultLanguage());
        String subject = message.getString("delegatednews.newsValid");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "delegatednewsNotificationValid");
        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("publicationId", publication.getId());
          template.setAttribute("publicationName", publication.getName());
          template.setAttribute("senderName", UserDetail.getById(senderId).getDisplayedName());
          ResourceLocator localizedMessage = new ResourceLocator(
              "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", lang);
          subject = localizedMessage.getString("delegatednews.newsValid");
          notifMetaData.addLanguage(lang, subject, "");
        }
        notifMetaData.addUserRecipient(new UserRecipient(publication.getUpdaterId()));
        notifMetaData.setComponentId(delegatednewsInstanceId);
        notifyUsers(notifMetaData, senderId);
      }
    } catch (Exception e) {
      SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyDelegatedNewsValid()",
          "delegatednews.EX_IMPOSSIBLE_DALERTER_LE_CONTRIBUTEUR", "pubId = " +
              news.getPubId(), e);
    }
  }

  /**
   * Notifie le dernier contributeur que l'actualité est refusée
   */
  private void notifyDelegatedNewsRefused(DelegatedNews news, String refusalMotive, String userId) {
    String delegatednewsInstanceId = getAppId();
    // Notification du dernier contributeur
    try {
      if (delegatednewsInstanceId == null) {
        SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyDelegatedNewsRefused()",
            "delegatednews.EX_AUCUNE_INSTANCE_DISPONIBLE");
      } else {
        PublicationDetail publication = news.getPublicationDetail();
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", DisplayI18NHelper.
                getDefaultLanguage());
        String subject = message.getString("delegatednews.newsRefused");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL,
                subject, templates, "delegatednewsNotificationRefused");
        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("publicationId", news.getPubId());
          template.setAttribute("publicationName", publication.getName(lang));
          template.setAttribute("refusalMotive", refusalMotive);
          template.setAttribute("senderName", UserDetail.getById(userId).getDisplayedName());
          ResourceLocator localizedMessage = new ResourceLocator(
              "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle", lang);
          subject = localizedMessage.getString("delegatednews.newsRefused");
          notifMetaData.addLanguage(lang, subject, "");
        }
        notifMetaData.addUserRecipient(new UserRecipient(publication.getUpdaterId()));
        notifMetaData.setComponentId(delegatednewsInstanceId);
        notifyUsers(notifMetaData, userId);
      }
    } catch (Exception e) {
      SilverTrace.warn("delegatednews", "DelegatedNewsServiceImpl.notifyDelegatedNewsRefused()",
          "delegatednews.EX_IMPOSSIBLE_DALERTER_LE_CONTRIBUTEUR", "pubId = " +
              news.getPubId(), e);
    }
  }

  /**
   * Met à jour l'ordre de l'actualité déléguée passée en paramètre
   */
  @Override
  public DelegatedNews updateOrderDelegatedNews(int pubId, int newsOrder) {
    DelegatedNews delegatedNews = dao.findOne(Integer.valueOf(pubId));
    delegatedNews.setNewsOrder(newsOrder);
    return (DelegatedNews) dao.saveAndFlush(delegatedNews);
  }
}
