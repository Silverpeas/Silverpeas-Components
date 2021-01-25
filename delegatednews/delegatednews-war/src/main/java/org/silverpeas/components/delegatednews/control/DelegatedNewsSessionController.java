/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.delegatednews.control;

import org.silverpeas.components.delegatednews.DelegatedNewsRuntimeException;
import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.components.delegatednews.service.DelegatedNewsServiceProvider;
import org.silverpeas.components.delegatednews.web.DelegatedNewsEntity;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.exception.EncodingException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.USER;

public class DelegatedNewsSessionController extends AbstractComponentSessionController {

  private transient DelegatedNewsService service;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public DelegatedNewsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle",
        "org.silverpeas.delegatednews.settings.DelegatedNewsIcons");
    service = DelegatedNewsServiceProvider.getDelegatedNewsService();
  }

  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (USER.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (ADMIN.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is component instance available
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
    List<DelegatedNews> listResult = new ArrayList<>();
    String[] allowedComponentIds = this.getUserAvailComponentIds();

    List<DelegatedNews> list = getService().getAllDelegatedNews();
    for (DelegatedNews delegatedNews : list) {
      String instanceId = delegatedNews.getInstanceId();
      if (isAvailComponentId(instanceId, allowedComponentIds)) {
        listResult.add(delegatedNews);
      }
    }
    return listResult;
  }

  /**
   * Validate delegated news identified by pubId
   * @param pubId the delegated news identifier to validate
   */
  public void validateDelegatedNews(String pubId) {
    getService().validateDelegatedNews(pubId, getUserId());
  }

  /**
   * Refuse delegated news identified by pubId
   * @param pubId the delegated news identifer to refuse
   * @param refuseReasonText the reason why delegated news has been refused
   */
  public void refuseDelegatedNews(String pubId, String refuseReasonText) {
    // refuse l'actualité
    getService().refuseDelegatedNews(pubId, this.getUserId(), refuseReasonText);
  }

  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   */
  public void updateDateDelegatedNews(String pubId, Date beginDate, Date endDate) {
    final OffsetDateTime periodStart =
        beginDate != null ? beginDate.toInstant().atOffset(ZoneOffset.UTC) : null;
    final OffsetDateTime periodEnd =
        endDate != null ? endDate.toInstant().atOffset(ZoneOffset.UTC) : null;
    final Period visibilityPeriod =
        beginDate != null && endDate != null ? Period.betweenNullable(periodStart, periodEnd) :
            null;
    getService().updateDateDelegatedNews(pubId, visibilityPeriod);
  }

  /**
   * Converts the list of Delegated News into its JSON representation.
   * @return a JSON representation of the list of Delegated News (as string)
   * @throws DelegatedNewsRuntimeException
   */
  public String getListDelegatedNewsJSON(List<DelegatedNews> listDelegatedNews) {
    List<DelegatedNewsEntity> listDelegatedNewsEntity = new ArrayList<>();
    for (DelegatedNews delegatedNews : listDelegatedNews) {
      DelegatedNewsEntity delegatedNewsEntity =
          DelegatedNewsEntity.fromDelegatedNews(delegatedNews);
      listDelegatedNewsEntity.add(delegatedNewsEntity);
    }
    return listAsJSON(listDelegatedNewsEntity);
  }

  /**
   * Converts the list of Delegated News Entity into its JSON representation.
   * @param listDelegatedNewsEntity the list of delegated news to convert into JSON representation
   * @return a JSON representation of the list of Delegated News Entity (as string)
   * @throws DelegatedNewsRuntimeException
   */
  private String listAsJSON(List<DelegatedNewsEntity> listDelegatedNewsEntity) {
    DelegatedNewsEntity[] entities =
        listDelegatedNewsEntity.toArray(new DelegatedNewsEntity[listDelegatedNewsEntity.size()]);
    try {
      return JSONCodec.encode(entities);
    } catch (EncodingException ex) {
      throw new DelegatedNewsRuntimeException("DelegatedNewsSessionController.listAsJSON()",
          SilverpeasRuntimeException.ERROR, "root.EX_NO_MESSAGE", ex);
    }
  }

  private DelegatedNewsService getService() {
    if (service == null) {
      service = DelegatedNewsServiceProvider.getDelegatedNewsService();
    }
    return service;
  }
}
