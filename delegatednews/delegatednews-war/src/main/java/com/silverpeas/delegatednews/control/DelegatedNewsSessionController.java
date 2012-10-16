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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.delegatednews.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

import static com.stratelia.webactiv.SilverpeasRole.*;

public class DelegatedNewsSessionController extends AbstractComponentSessionController {

	private DelegatedNewsService service = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public DelegatedNewsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.delegatednews.multilang.DelegatedNewsBundle",
        "com.silverpeas.delegatednews.settings.DelegatedNewsIcons");

    service = ServicesFactory.getDelegatedNewsService();
  }
  
  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (user.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (admin.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Est-ce qu'une instanceId fait partie d'un tableau ?
   *
   * @return boolean : true si l'instanceId passsée en paramètre appartient au tableau passé en paramètre 2 
   */
  private boolean isAvailComponentId(String instanceId, String[] allowedComponentIds) {
    if(instanceId == null) {
      return false;
    }
    
    for (String element : allowedComponentIds) {
      if(instanceId.equals(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Récupère toutes les actualités déléguées inter Theme Tracker dont l'utilisateur courant a des droits
   *
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  public List<DelegatedNews> getAllAvailDelegatedNews() {
    List<DelegatedNews> listResult = new ArrayList<DelegatedNews>();
    String[] allowedComponentIds = this.getUserAvailComponentIds();
     
    List<DelegatedNews> list = service.getAllDelegatedNews();
    for (DelegatedNews delegatedNews : list) {
      String instanceId = delegatedNews.getInstanceId();
      if(isAvailComponentId(instanceId, allowedComponentIds)) {
        listResult.add(delegatedNews);
      }
    }
    return listResult;
  }
  
  /**
   * Valide l'actualité déléguée passée en paramètre
   *
   */
  public void validateDelegatedNews(int pubId) {
    //valide l'actualité
    service.validateDelegatedNews(pubId, this.getUserId());
    
    DelegatedNews delegatedNews = service.getDelegatedNews(pubId);
    PublicationDetail pubDetail = delegatedNews.getPublicationDetail();
    
    //alerte le dernier contributeur de la décision
    service.notifyDelegatedNewsValid(pubDetail.getPK().getId(), pubDetail.getName(this.getLanguage()), this.getUserId(), this.getUserDetail().getDisplayedName(), delegatedNews.getContributorId(), this.getComponentId());
    
  }
  
  /**
   * Refuse l'actualité déléguée passée en paramètre
   *
   */
  public void refuseDelegatedNews(int pubId, String refuseReasonText) {
    //refuse l'actualité
    service.refuseDelegatedNews(pubId, this.getUserId());
    
    DelegatedNews delegatedNews = service.getDelegatedNews(pubId);
    PublicationDetail pubDetail = delegatedNews.getPublicationDetail();
    
    //alerte le dernier contributeur de la décision
    service.notifyDelegatedNewsRefused(pubDetail.getPK().getId(), pubDetail.getName(this.getLanguage()), refuseReasonText, this.getUserId(), this.getUserDetail().getDisplayedName(), delegatedNews.getContributorId(), this.getComponentId());
    
  }
  
  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   *
   */
  public void updateDateDelegatedNews(int pubId, Date beginDate, Date endDate) {
    
    service.updateDateDelegatedNews(pubId, beginDate, endDate);
  }
}
