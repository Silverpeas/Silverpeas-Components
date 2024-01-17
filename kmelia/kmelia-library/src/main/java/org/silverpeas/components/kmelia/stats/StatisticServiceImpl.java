/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.stats;

import org.silverpeas.components.kmelia.model.StatisticActivityVO;
import org.silverpeas.components.kmelia.model.StatsFilterVO;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.OffsetDateTime.now;
import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;
import static org.silverpeas.core.contribution.publication.dao.PublicationCriteria.onComponentInstanceIds;
import static org.silverpeas.core.contribution.publication.model.PublicationDetail.VALID_STATUS;

@Service
public class StatisticServiceImpl implements
    org.silverpeas.components.kmelia.stats.StatisticService {

  private static final String PUBLICATION_TYPE = "Publication";
  @Inject
  private PublicationService publicationService;
  @Inject
  private NodeService nodeService;
  @Inject
  private StatisticService statisticService;

  @Override
  public Integer getNbConsultedPublication(StatsFilterVO statFilter) {
    if (statFilter != null) {
      return getNumberOfConsultedPublications(statFilter);
    }
    return -1;
  }

  @Override
  public Integer getNbStatisticActivityByPeriod(StatsFilterVO statFilter) {
    if (statFilter != null) {
      // Retrieve the list of topic application publications
      List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
      if (!publis.isEmpty()) {
        return (int)countGlobalPublicationActivity(statFilter, publis);
      }
      return 0;
    }
    return -1;
  }

  private Integer getNumberOfConsultedPublications(StatsFilterVO statFilter) {
    List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
    int nbPubli = 0;
    if (!publis.isEmpty()) {
      Integer groupId = statFilter.getGroupId();
      List<ResourceReference> publiPKs = getReferencesToPublications(publis);
      if (groupId != null) {
        // Retrieve the list of user identifiers
        List<String> userIds = getListUserIdsFromGroup(groupId);
        try {
          // Retrieve the number of publication
          nbPubli = getStatisticService().getCountByPeriodAndUser(publiPKs, PUBLICATION_TYPE,
              statFilter.getStartDate(), statFilter.getEndDate(), userIds);

        } catch (Exception e) {
          SilverLogger.getLogger(this)
              .error("Error when counting number of access (getCountByPeriodAndUser)", e);
        }

      } else {
        try {
          nbPubli = getStatisticService().getCountByPeriod(publiPKs, 1, PUBLICATION_TYPE,
              statFilter.getStartDate(), statFilter.getEndDate());
        } catch (Exception e) {
          SilverLogger.getLogger(this)
              .error("Error when counting number of access (getCountByPeriod)", e);
        }

      }
    }
    return nbPubli;
  }

  /**
   * @param publis the list of publication detail where we extract WAPrimaryKey
   * @return a list of references to the specified publications
   */
  private List<ResourceReference> getReferencesToPublications(List<PublicationDetail> publis) {
    List<ResourceReference> publiPKs = new ArrayList<>();
    // Check access for each publication
    for (PublicationDetail publi : publis) {
      publiPKs.add(new ResourceReference(publi.getPK()));
    }
    return publiPKs;
  }

  /**
   * @param groupId the group identifier
   * @return the list of user identifiers which are linked to a group given in parameter, or empty
   * list if an exception occurs
   */
  private List<String> getListUserIdsFromGroup(Integer groupId) {
    List<String> userIds = new ArrayList<>();
    try {
      Group selectedGroup = getAdminService().getGroup(Integer.toString(groupId));
      String[] arrayUserIds = selectedGroup.getUserIds();
      Collections.addAll(userIds, arrayUserIds);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error("Error when loading the list of filtered users", e);
    }
    return userIds;
  }

  /**
   * @param statFilter the stats filter value object
   * @return the list of application publications which respects the stats filter constraint
   */
  private List<PublicationDetail> getValidApplicationPublications(StatsFilterVO statFilter) {
    final NodePK fatherPK = new NodePK(Integer.toString(statFilter.getTopicId()),
        statFilter.getInstanceId());
    return getPublicationService().getPublicationsByCriteria(
        onComponentInstanceIds(statFilter.getInstanceId()).onNodes(
            getNodeService().getSubTree(fatherPK)
                .stream()
                .map(NodeDetail::getId)
                .collect(Collectors.toSet()))
            .ofStatus(VALID_STATUS)
            .visibleAt(now()));
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @param isCreate true if counting create publication activity
   * @param isUpdate true if counting update publication activity
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private long countPublicationActivity(StatsFilterVO statFilter,
      Collection<PublicationDetail> publis, boolean isCreate, boolean isUpdate) {
    long nbPubli;
    Date startTime = statFilter.getStartDate();
    Date endTime = statFilter.getEndDate();
    Integer groupId = statFilter.getGroupId();

    if (groupId != null) {
      List<String> userIds = getListUserIdsFromGroup(groupId);
      if (!userIds.isEmpty()) {
        nbPubli = publis.stream()
            .filter(
                p -> isPubliActivityInsideTimeInterval(startTime, endTime, p, isCreate, isUpdate))
            .flatMap(p -> userIds.stream().map(u -> Pair.of(p, u)))
            .filter(pu -> isUserRelatedWithPubli(pu.getFirst(), pu.getSecond()))
            .count();
      } else {
        nbPubli = 0;
      }
    } else {
      // Check activity for each publication
      nbPubli = publis.stream()
          .filter(p -> isPubliActivityInsideTimeInterval(startTime, endTime, p, isCreate, isUpdate))
          .count();
    }
    return nbPubli;
  }

  /**
   * @param publi the publication detail
   * @param userId the user identifier
   * @return true if user has created, modified or validate this publication
   */
  private boolean isUserRelatedWithPubli(PublicationDetail publi, String userId) {
    return (userId.equals(publi.getCreatorId()) || userId.equals(publi.getUpdaterId()) || userId
        .equals(publi.getValidatorId()));
  }

  /**
   * @param startTime the start time interval
   * @param endTime the end time interval
   * @param publi the publication detail
   * @param isCreate true if check Create activity
   * @param isUpdate true if check Update activity
   * @return true if publication creation date and isCreate or modification date and isUpdate is
   * between startTime and endTime
   */
  private boolean isPubliActivityInsideTimeInterval(Date startTime, Date endTime,
      PublicationDetail publi, boolean isCreate, boolean isUpdate) {
    Date createDate = publi.getCreationDate();
    Date updateDate = publi.getLastUpdateDate();
    return (isCreate && (createDate.after(startTime) || createDate.equals(startTime)) && createDate.
        before(endTime)) || (isUpdate && (updateDate.after(startTime) || updateDate.
        equals(startTime)) && updateDate
        .before(endTime));
  }

  private PublicationService getPublicationService() {
    return publicationService;
  }

  private NodeService getNodeService() {
    return nodeService;
  }

  private StatisticService getStatisticService() {
    return statisticService;
  }

  @Override
  public StatisticActivityVO getStatisticActivity(StatsFilterVO statFilter) {
    List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
    long nbCreate = countCreatePublicationActivity(statFilter, publis);
    long nbUpdate = countUpdatePublicationActivity(statFilter, publis);
    return new StatisticActivityVO((int)nbCreate, (int)nbUpdate);
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private long countCreatePublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, true, false);
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private long countUpdatePublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, false, true);
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private long countGlobalPublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, true, true);
  }

  @Override
  public Integer getNumberOfDifferentConsultedPublications(StatsFilterVO statFilter) {
    if (statFilter != null) {
      List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
      if (!publis.isEmpty()) {
        return countDistinctConsultedPublications(statFilter, publis);
      }
    }
    return -1;
  }

  /**
   * @param statFilter the statistic filter which contains all the statistics filter parameters
   * @param publis the list of publications
   * @return the number of distinct consulted publications
   */
  private Integer countDistinctConsultedPublications(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    int nbPubli = 0;
    List<ResourceReference> publiPKs = getReferencesToPublications(publis);
    if (statFilter.getGroupId() != null) {
      // Retrieve the list of user identifiers
      List<String> userIds = getListUserIdsFromGroup(statFilter.getGroupId());
      try {
        return getStatisticService().getDistinctCountByPeriodUser(publiPKs, 1, PUBLICATION_TYPE,
            statFilter.getStartDate(), statFilter.getEndDate(), userIds);
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Error when computing distinct access to publication", e);
      }
    } else {
      try {
        return getStatisticService().getDistinctCountByPeriod(publiPKs, 1, PUBLICATION_TYPE,
            statFilter.getStartDate(), statFilter.getEndDate());
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Error when computing distinct access to publication", e);
      }
    }
    return nbPubli;
  }
}
