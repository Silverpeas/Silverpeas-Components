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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.delegatednews.service;

import com.silverpeas.delegatednews.model.DelegatedNews;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.silverpeas.delegatednews.dao.DelegatedNewsDao;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.ResourceLocator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class DelegatedNewsServiceImpl implements DelegatedNewsService {
  
	@Inject
	private DelegatedNewsDao dao;
	
	/**
   * Ajout d'une actualité déléguée
   *
   */
	@Override
	public void addDelegatedNews(int pubId, String instanceId, String contributorId, Date validationDate, Date beginDate, Date endDate) {
	  DelegatedNews delegatedNews = new DelegatedNews(pubId, instanceId, contributorId, validationDate, beginDate, endDate);
		dao.saveAndFlush(delegatedNews);
	}
	
	/**
   * Récupère une actualité déléguée correspondant à la publication Theme Tracker passée en paramètre
   *
   * @param pubId : l'id de la publication de Theme Tracker
   * @return DelegatedNews : l'objet correspondant à l'actualité déléguée ou null si elle n'existe pas
   */
	@Override
	public DelegatedNews getDelegatedNews(int pubId) {
	  DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
	  return delegatedNews;
	}
	
	 /**
   * Récupère toutes les actualités déléguées valides inter Theme Tracker
   *
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  @Override
  public List<DelegatedNews> getAllDelegatedNews() {
    List<DelegatedNews> list = dao.readAll(); //TODO utiliser plutot readAll(Sort) sur la date de validation 
    return list;
  }
  
  /**
   * Récupère toutes les actualités déléguées inter Theme Tracker
   *
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  @Override
  public List<DelegatedNews> getAllValidDelegatedNews() {
    List<DelegatedNews> list = dao.readAll(); //TODO utiliser plutot readAll(Specification) sur la date de validation et sur le statut valid 
    return list;
  }
  
  /**
   * Valide l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void validateDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setStatus(DelegatedNews.NEWS_VALID);
    dao.saveAndFlush(delegatedNews);
  }

  /**
   * Refuse l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void refuseDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setStatus(DelegatedNews.NEWS_REFUSED);
    dao.saveAndFlush(delegatedNews);
  }
  
  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setBeginDate(dateHourBegin);
    delegatedNews.setEndDate(dateHourEnd);
    dao.saveAndFlush(delegatedNews);
  }
	
	/**
	 * @return
	 */
	protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
        new ResourceLocator("com.silverpeas.delegatednews.settings.DelegatedNewsSettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString(
        "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
        "customersTemplatePath"));

    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }
	
	/**
	 * @return
	 */
	private OrganizationController getOrganizationController() {
    // must return a new instance each time
    // This is to resolve Serializable problems
    OrganizationController orga = new OrganizationController();
    return orga;
  }

	
	/**
   * Notifie l'Equipe éditoriale d'une actualité à valider
   *
   */
  public void notifyDelegatedNewsToValidate(String pubId, String pubName, String senderName, String[] editors) {
    
    //Notification des membres de l'équipe éditoriale
    
    try {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "com.silverpeas.delegatednews.multilang.DelegatedNewsBundle", DisplayI18NHelper.getDefaultLanguage());
        String subject = message.getString("delegatednews.newsSuggest");
        
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, "delegatednewsNotificationToValidate");
        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("publicationId", pubId);
          template.setAttribute("publicationName", pubName);
          template.setAttribute("senderName", senderName);
          ResourceLocator localizedMessage = new ResourceLocator(
              "com.silverpeas.delegatednews.multilang.DelegatedNewsBundle", lang);
          subject = localizedMessage.getString("delegatednews.newsSuggest");
          notifMetaData.addLanguage(lang, subject, "");
        }
        List<String> roles = new ArrayList<String>();
        roles.add("admin");
        String[] editors = getOrganizationController().getUsersIdsByRoleNames(pubDetail.getPK().
            getInstanceId(), roles);
        notifMetaData.addUserRecipients(editors);
        notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
        notifyUsers(notifMetaData, userIdWhoRefuse);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendValidationNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
          + fatherPK.getId() + ", pubPK = " + pubDetail.getPK(), e);
    }
  }
}
