/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.Pagination;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.PublicationHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.security.authorization.AccessControlOperation.search;

public class KmeliaTransversal implements PublicationHelper {

  private String userId = null;
  private PublicationService publicationService = PublicationService.get();
  private OrganizationController organizationControl =
      OrganizationControllerProvider.getOrganisationController();

  public KmeliaTransversal() {
  }

  public KmeliaTransversal(String userId) {
    this.userId = userId;
  }

  public KmeliaTransversal(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  @Override
  public void setMainSessionController(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  public List<PublicationDetail> getPublications() {
    return getPublications(null);
  }

  public List<PublicationDetail> getPublications(int nbPublis) {
    return getPublications(null, nbPublis);
  }

  public List<PublicationDetail> getPublications(String spaceId) {
    return getPublications(spaceId, -1);
  }

  @Override
  public List<PublicationDetail> getPublications(String spaceId, int nbPublis) {
    return getPublications(spaceId, new ArrayList<>(), nbPublis);
  }

  @Override
  public List<PublicationDetail> getPublications(String spaceId, List<String> excluded, int nbPublis) {
    List<String> componentIds = getAvailableComponents(spaceId);
    componentIds.removeAll(excluded);
    final SilverpeasList<PublicationPK> filteredPublicationPKs =
        new Pagination<PublicationPK, SilverpeasList<PublicationPK>>(new PaginationPage(1, nbPublis))
        .paginatedDataSource(p -> {
          try {
            return getPublicationService()
                .getPublicationPKsByStatus(PublicationDetail.VALID_STATUS, componentIds,
                    p.originalSizeIsNotRequired());
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(failureOnGetting("publication pks of space", spaceId));
            return new SilverpeasArrayList<>(0);
          }
        })
        .filter(this::filterPublicationPKs)
        .execute();
    try {
      return getPublicationService().getPublications(filteredPublicationPKs);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(failureOnGetting("publications of space", spaceId));
    }
    return new ArrayList<>();
  }

  @Override
  public List<PublicationDetail> getUpdatedPublications(String spaceId, int since, int nbReturned) {
    int maxAge = since;
    if (maxAge > 0) {
      maxAge = -1 * maxAge;
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, maxAge);
      return getUpdatedPublications(spaceId, calendar.getTime(), nbReturned);
    }
    return getPublications(spaceId, nbReturned);
  }

  protected List<PublicationDetail> getUpdatedPublications(String spaceId, Date since, int nbPublis) {
    List<String> componentIds = getAvailableComponents(spaceId);
    final SilverpeasList<PublicationPK> filteredPublicationPKs =
        new Pagination<PublicationPK, SilverpeasList<PublicationPK>>(new PaginationPage(1, nbPublis))
        .paginatedDataSource(p -> {
          try {
            return getPublicationService()
                .getUpdatedPublicationPKsByStatus(PublicationDetail.VALID_STATUS, since,
                    componentIds, p.originalSizeIsNotRequired());
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(failureOnGetting("publication pks of space", spaceId));
            return new SilverpeasArrayList<>(0);
          }
        })
        .filter(this::filterPublicationPKs)
        .execute();
    try {
      return getPublicationService().getPublications(filteredPublicationPKs);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(failureOnGetting("publications of space", spaceId));
    }
    return new ArrayList<>();
  }

  protected List<String> getAvailableComponents(String spaceId) {
    List<String> componentIds = new ArrayList<>();
    if (!StringUtil.isDefined(spaceId)) {
      String[] cIds = getOrganizationControl().getComponentIdsForUser(userId, "kmelia");
      componentIds.addAll(Arrays.asList(cIds));
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "toolbox");
      componentIds.addAll(Arrays.asList(cIds));
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "kmax");
      componentIds.addAll(Arrays.asList(cIds));
    } else {
      String[] cIds = getOrganizationControl().getAvailCompoIds(spaceId, userId);
      for (String id : cIds) {
        if (id.startsWith("kmelia") || id.startsWith("toolbox") || id.startsWith("kmax")) {
          componentIds.add(id);
        }
      }
    }
    return componentIds;
  }

  public List<PublicationDetail> getPublicationsByComponentId(String componentId) {
    List<PublicationDetail> publications = null;
    try {
      List<String> componentIds = new ArrayList<>();
      componentIds.add(componentId);
      publications = (List<PublicationDetail>) getPublicationService().getPublicationsByStatus("Valid",
          componentIds);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(failureOnGetting("publications of component", componentId));
    }
    return filterPublications(publications, -1);
  }

  private List<PublicationDetail> filterPublications(List<PublicationDetail> publications,
      int nbPublis) {
    List<PublicationDetail> filteredPublications = new ArrayList<>();
    KmeliaAuthorization security = new KmeliaAuthorization();

    PublicationDetail pub = null;
    for (int p = 0; publications != null && p < publications.size(); p++) {
      pub = publications.get(p);
      if (security.isObjectAvailable(pub.getPK().getInstanceId(), userId, pub.getPK().getId(),
          "Publication")) {
        filteredPublications.add(pub);
      }

      if (nbPublis != -1 && filteredPublications.size() == nbPublis) {
        return filteredPublications;
      }
    }

    return filteredPublications;
  }

  private SilverpeasList<PublicationPK> filterPublicationPKs(
      final SilverpeasList<PublicationPK> publicationPKs) {
    return PublicationAccessControl.get()
        .filterAuthorizedByUser(publicationPKs, userId, AccessControlContext.init().onOperationsOf(search))
        .collect(SilverpeasList.collector(publicationPKs));
  }

  private OrganizationController getOrganizationControl() {
    return organizationControl;
  }

  private PublicationService getPublicationService() {
    return publicationService;
  }
}
