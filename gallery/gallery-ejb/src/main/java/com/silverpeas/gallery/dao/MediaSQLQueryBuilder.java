/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.gallery.dao;

import com.silverpeas.calendar.DateTime;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaCriteriaProcessor;
import com.silverpeas.gallery.model.MediaLogicalComparator;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY;
import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY;
import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY.HIDDEN_ONLY;

/**
 * A dynamic builder of a SQL query.
 */
public class MediaSQLQueryBuilder implements MediaCriteriaProcessor {

  private StringBuilder orderBy = null;
  private boolean done = false;
  private final StringBuilder sqlQuery = new StringBuilder();
  private final StringBuilder from = new StringBuilder();
  private final StringBuilder where = new StringBuilder();
  private final List<Object> parameters = new ArrayList<Object>();
  private String conjunction = "";
  private List<QUERY_ORDER_BY> logicalOrderBy;
  private boolean distinct = false;

  @Override
  public void startProcessing() {
    sqlQuery.append("select M.mediaId, M.mediaType, M.instanceId")
        .append(", M.title, M.description, M.author, M.keyWord")
        .append(", M.beginVisibilityDate, M.endVisibilityDate")
        .append(", M.createDate, M.createdBy, M.lastUpdateDate, M.lastUpdatedBy ");
    from.append("from SC_Gallery_Media M ");
  }

  @Override
  public void endProcessing() {
    if (distinct) {
      sqlQuery.insert(6, " distinct");
    }
    sqlQuery.append(from.toString());
    if (where.length() > 0) {
      sqlQuery.append(" where ").append(where.toString());
    }
    if (orderBy != null && orderBy.length() > 0) {
      sqlQuery.append(orderBy.toString());
    }
    done = true;
  }

  @Override
  public Pair<String, List<Object>> result() {
    return Pair.of(sqlQuery.toString(), parameters);
  }

  @Override
  public MediaCriteriaProcessor then() {
    if (!done) {
      conjunction = " and ";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processVisibility(final VISIBILITY visibility,
      final Date dateReference, final UserDetail creator) {
    switch (visibility) {
      case VISIBLE_ONLY:
      case HIDDEN_ONLY:
      case BY_DEFAULT:
        final StringBuilder clause;
        if (visibility == HIDDEN_ONLY) {
          clause =
              where(conjunction).append("((? < M.beginVisibilityDate or ? > M.endVisibilityDate)");
          parameters.add(dateReference.getTime());
          parameters.add(dateReference.getTime());
        } else {
          clause = where(conjunction)
              .append("((? between M.beginVisibilityDate and M.endVisibilityDate)");
          parameters.add(dateReference.getTime());
        }
        if (creator != null) {
          clause.append(" or M.createdBy = ?");
          parameters.add(creator.getId());
        }
        clause.append(")");
        break;
      case FORCE_GET_ALL:
        // No clause
        break;
    }
    conjunction = "";
    return this;
  }

  @Override
  public MediaCriteriaProcessor processComponentInstance(String componentInstanceId) {
    if (!done) {
      where(conjunction).append("M.instanceId = ?");
      parameters.add(componentInstanceId);
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processAlbums(final List<String> albumIds) {
    if (!done) {
      StringBuilder params = new StringBuilder();
      for (String albumId : albumIds) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
        parameters.add(Integer.valueOf(albumId));
      }
      distinct = true;
      from.append(
          "join SC_Gallery_Path A on M.mediaId = A.mediaId and M.instanceId = A.instanceId ");
      where(conjunction).append("A.nodeId in (").append(params.toString()).append(")");
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processCreator(UserDetail creator) {
    if (!done) {
      where(conjunction).append("M.createdBy = ?");
      parameters.add(creator.getId());
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processMediaTypes(List<MediaType> mediaTypes) {
    if (!done) {
      StringBuilder params = new StringBuilder();
      for (MediaType mediaType : mediaTypes) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
        parameters.add(mediaType.name());
      }
      where(conjunction).append("M.mediaType in (").append(params.toString()).append(")");
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processNbDaysBeforeThatMediaIsNotVisible(final Date referenceDate,
      final int nbDaysBeforeThatMediaIsNotVisible) {
    if (!done) {
      where(conjunction).append("M.endVisibilityDate between ? and ?");
      DateTime date =
          new DateTime(DateUtils.addDays(referenceDate, nbDaysBeforeThatMediaIsNotVisible));
      parameters.add(date.getBeginOfDay().getTime());
      parameters.add(date.getEndOfDay().getTime());
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings) {
    if (!done) {
      for (QUERY_ORDER_BY anOrdering : orderings) {
        if (!anOrdering.isApplicableOnSQLQuery()) {
          logicalOrderBy = orderings;
          orderBy = null;
          break;
        }
        if (orderBy == null) {
          orderBy = new StringBuilder(" order by ");
        } else {
          orderBy.append(", ");
        }
        orderBy.append(anOrdering.getInstructionBase());
        orderBy.append(" ");
        orderBy.append(anOrdering.isAsc() ? "asc" : "desc");
      }
      conjunction = "";
    }
    return this;
  }

  @Override
  public MediaCriteriaProcessor processIdentifiers(List<String> identifiers) {
    if (!done) {
      StringBuilder params = new StringBuilder();
      for (String identifier : identifiers) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
        parameters.add(identifier);
      }
      where(conjunction).append("M.mediaId in (").append(params.toString()).append(")");
      conjunction = "";
    }
    return this;
  }

  @Override
  public List<Media> orderingResult(final List<Media> media) {
    if (CollectionUtil.isNotEmpty(logicalOrderBy)) {
      Collections.sort(media, MediaLogicalComparator.on(logicalOrderBy));
    }
    return media;
  }

  /**
   * Centralization to perform the where clause.
   * @param conjunction
   * @return
   */
  private StringBuilder where(String conjunction) {
    if (where.length() > 0) {
      where.append(conjunction);
    }
    return where;
  }
}
