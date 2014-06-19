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
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.comparator.AbstractComplexComparator;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY;
import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY;

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
      final Date dateReference) {
    switch (visibility) {
      case VISIBLE_ONLY:
      case BY_DEFAULT:
        where(conjunction).append("? between M.beginVisibilityDate and M.endVisibilityDate");
        parameters.add(dateReference.getTime());
        break;
      case HIDDEN_ONLY:
        where(conjunction).append("(? < M.beginVisibilityDate or ? > M.endVisibilityDate)");
        parameters.add(dateReference.getTime());
        parameters.add(dateReference.getTime());
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
        parameters.add(albumId);
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
        orderBy.append(anOrdering.getPropertyName());
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
      Collections.sort(media, new MediaLogicalComparator(logicalOrderBy));
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

  /**
   * This private class handles the logical comparison of media data.
   */
  private static class MediaLogicalComparator extends AbstractComplexComparator<Media> {
    private final List<QUERY_ORDER_BY> logicalOrderBy;

    private static final Integer EMPTY_DIMENSION_ASC = Integer.MAX_VALUE;
    private static final Integer EMPTY_DIMENSION_DESC = Integer.MIN_VALUE;

    private MediaLogicalComparator(final List<QUERY_ORDER_BY> logicalOrderBy) {
      this.logicalOrderBy = logicalOrderBy;
    }

    @Override
    protected ValueBuffer getValuesToCompare(final Media media) {
      ValueBuffer valueBuffer = new ValueBuffer();
      for (QUERY_ORDER_BY queryOrderBy : logicalOrderBy) {
        switch (queryOrderBy) {
          case TITLE_DESC:
          case TITLE_ASC:
            valueBuffer.append(media.getTitle(), queryOrderBy.isAsc());
            break;
          case COMPONENT_INSTANCE_ASC:
          case COMPONENT_INSTANCE_DESC:
            valueBuffer.append(media.getComponentInstanceId(), queryOrderBy.isAsc());
            break;
          case IDENTIFIER_ASC:
          case IDENTIFIER_DESC:
            valueBuffer.append(media.getId(), queryOrderBy.isAsc());
            break;
          case CREATE_DATE_ASC:
          case CREATE_DATE_DESC:
            valueBuffer.append(media.getCreationDate(), queryOrderBy.isAsc());
            break;
          case LAST_UPDATE_DATE_ASC:
          case LAST_UPDATE_DATE_DESC:
            valueBuffer.append(media.getLastUpdateDate(), queryOrderBy.isAsc());
            break;
          case AUTHOR_ASC_EMPTY_END:
          case AUTHOR_DESC_EMPTY_END:
            valueBuffer.append(new StringWrapper(media.getAuthor(), queryOrderBy.isAsc(), true),
                queryOrderBy.isAsc());
            break;
          case AUTHOR_ASC:
          case AUTHOR_DESC:
            valueBuffer.append(media.getAuthor(), queryOrderBy.isAsc());
            break;
          case DIMENSION_ASC:
          case DIMENSION_DESC:
            if (media.getPhoto() != null) {
              Photo photo = media.getPhoto();
              if (photo.getResolutionH() > 0 && photo.getResolutionW() > 0) {
                valueBuffer.append(Math.max(photo.getResolutionW(), photo.getResolutionH()),
                    queryOrderBy.isAsc());
                valueBuffer.append(Math.min(photo.getResolutionW(), photo.getResolutionH()),
                    queryOrderBy.isAsc());
              }
            } else if (media.getVideo() != null) {
              Video video = media.getVideo();
              if (video.getResolutionH() > 0 && video.getResolutionW() > 0) {
                valueBuffer.append(Math.max(video.getResolutionW(), video.getResolutionH()),
                    queryOrderBy.isAsc());
                valueBuffer.append(Math.min(video.getResolutionW(), video.getResolutionH()),
                    queryOrderBy.isAsc());
              }
            } else {
              valueBuffer.append(queryOrderBy.isAsc() ? EMPTY_DIMENSION_ASC : EMPTY_DIMENSION_DESC,
                  queryOrderBy.isAsc());
            }
            break;
          default:
            throw new NotImplementedException(
                "You must add a new logical data order by management...");
        }
      }
      return valueBuffer;
    }
  }
}
