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

import com.silverpeas.delegatednews.DelegatedNewsRuntimeException;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.silverpeas.delegatednews.web.DelegatedNewsEntity;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import org.silverpeas.util.JSONCodec;
import org.silverpeas.util.exception.EncodingException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.stratelia.webactiv.SilverpeasRole.admin;
import static com.stratelia.webactiv.SilverpeasRole.user;

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
        "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle",
        "org.silverpeas.delegatednews.settings.DelegatedNewsIcons");

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
   * @return boolean : true si l'instanceId passsée en paramètre appartient au tableau passé en
   * paramètre 2
   */
  private boolean isAvailComponentId(String instanceId, String[] allowedComponentIds) {
    if (instanceId == null) {
      return false;
    }

    for (String element : allowedComponentIds) {
      if (instanceId.equals(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Récupère toutes les actualités déléguées inter Theme Tracker dont l'utilisateur courant a des
   * droits
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
  public List<DelegatedNews> getAllAvailDelegatedNews() {
    List<DelegatedNews> listResult = new ArrayList<DelegatedNews>();
    String[] allowedComponentIds = this.getUserAvailComponentIds();

    List<DelegatedNews> list = service.getAllDelegatedNews();
    for (DelegatedNews delegatedNews : list) {
      String instanceId = delegatedNews.getInstanceId();
      if (isAvailComponentId(instanceId, allowedComponentIds)) {
        listResult.add(delegatedNews);
      }
    }
    return listResult;
  }

  /**
   * Valide l'actualité déléguée passée en paramètre
   */
  public void validateDelegatedNews(int pubId) {
    // valide l'actualité
    service.validateDelegatedNews(pubId, getUserId());
  }

  /**
   * Refuse l'actualité déléguée passée en paramètre
   */
  public void refuseDelegatedNews(int pubId, String refuseReasonText) {
    // refuse l'actualité
    service.refuseDelegatedNews(pubId, this.getUserId(), refuseReasonText);
  }

  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   */
  public void updateDateDelegatedNews(int pubId, Date beginDate, Date endDate) {
    service.updateDateDelegatedNews(pubId, beginDate, endDate);
  }

  /**
   * Converts the list of Delegated News into its JSON representation.
   * @return a JSON representation of the list of Delegated News (as string)
   * @throws JAXBException
   */
  public String getListDelegatedNewsJSON(List<DelegatedNews> listDelegatedNews)
      throws JAXBException {
    List<DelegatedNewsEntity> listDelegatedNewsEntity = new ArrayList<DelegatedNewsEntity>();
    for (DelegatedNews delegatedNews : listDelegatedNews) {
      DelegatedNewsEntity delegatedNewsEntity =
          DelegatedNewsEntity.fromDelegatedNews(delegatedNews);
      listDelegatedNewsEntity.add(delegatedNewsEntity);
    }
    return listAsJSON(listDelegatedNewsEntity);
  }

  /**
   * Converts the list of Delegated News Entity into its JSON representation.
   * @param listDelegatedNewsEntity
   * @return a JSON representation of the list of Delegated News Entity (as string)
   * @throws DelegatedNewsRuntimeException
   */
  private String listAsJSON(List<DelegatedNewsEntity> listDelegatedNewsEntity)
      throws DelegatedNewsRuntimeException {
    DelegatedNewsEntity[] entities =
        listDelegatedNewsEntity.toArray(new DelegatedNewsEntity[listDelegatedNewsEntity.size()]);
    try {
      return JSONCodec.encode(entities);
    } catch (EncodingException ex) {
      throw new DelegatedNewsRuntimeException("DelegatedNewsSessionController.listAsJSON()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_NO_MESSAGE", ex);
    }
  }
}
