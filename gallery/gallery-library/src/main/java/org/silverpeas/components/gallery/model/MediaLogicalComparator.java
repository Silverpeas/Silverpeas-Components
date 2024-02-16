/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;

import java.util.List;

import static org.silverpeas.components.gallery.model.MediaCriteria.QUERY_ORDER_BY;

/**
 * This class handles the logical comparison of media data.
 */
public class MediaLogicalComparator extends AbstractComplexComparator<Media> {
  private final List<QUERY_ORDER_BY> logicalOrderBy;

  private static final Integer EMPTY_DIMENSION_ASC = Integer.MAX_VALUE;
  private static final Integer EMPTY_DIMENSION_DESC = Integer.MIN_VALUE;

  /**
   * Hidden constructor.
   * @param logicalOrderBy
   */
  private MediaLogicalComparator(final List<QUERY_ORDER_BY> logicalOrderBy) {
    super();
    this.logicalOrderBy = logicalOrderBy;
  }

  /**
   * Easy way to obtain an instance of the comparator.
   * @param logicalOrderBy
   * @return
   */
  public static MediaLogicalComparator on(final QUERY_ORDER_BY... logicalOrderBy) {
    return on(CollectionUtil.asList(logicalOrderBy));
  }

  /**
   * Easy way to obtain an instance of the comparator.
   * @param logicalOrderBy
   * @return
   */
  public static MediaLogicalComparator on(final List<QUERY_ORDER_BY> logicalOrderBy) {
    return new MediaLogicalComparator(logicalOrderBy);
  }

  @Override
  protected ValueBuffer getValuesToCompare(final Media media) {
    ValueBuffer valueBuffer = new ValueBuffer();
    for (QUERY_ORDER_BY queryOrderBy : logicalOrderBy) {
      setValueToCompare(media, valueBuffer, queryOrderBy);
    }
    return valueBuffer;
  }

  private void setValueToCompare(final Media media, final ValueBuffer valueBuffer,
      final QUERY_ORDER_BY queryOrderBy) {
    String author;
    switch (queryOrderBy) {
      case TITLE_DESC:
      case TITLE_ASC:
        setTitle(media, valueBuffer, queryOrderBy);
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
        author = getAuthor(media);
        valueBuffer.append(new StringWrapper(author, queryOrderBy.isAsc(), true),
            queryOrderBy.isAsc());
        break;
      case AUTHOR_ASC:
      case AUTHOR_DESC:
        author = getAuthor(media);
        valueBuffer.append(author, queryOrderBy.isAsc());
        break;
      case SIZE_ASC:
      case SIZE_DESC:
        setSize(media, valueBuffer, queryOrderBy);
        break;
      case DIMENSION_ASC:
      case DIMENSION_DESC:
        setDimensions(media, valueBuffer, queryOrderBy);
        break;
      default:
        throw new UnsupportedOperationException(
            "You must add a new logical data order by management...");
    }
  }

  private void setDimensions(final Media media, final ValueBuffer valueBuffer,
      final QUERY_ORDER_BY queryOrderBy) {
    if (media.getType().isPhoto()) {
      Photo photo = media.getPhoto();
      if (photo.getDefinition().isDefined()) {
        valueBuffer.append(
            Math.max(photo.getDefinition().getWidth(), photo.getDefinition().getHeight()),
            queryOrderBy.isAsc());
        valueBuffer.append(
            Math.min(photo.getDefinition().getWidth(), photo.getDefinition().getHeight()),
            queryOrderBy.isAsc());
      }
    } else if (media.getType().isVideo()) {
      Video video = media.getVideo();
      if (video.getDefinition().isDefined()) {
        valueBuffer.append(
            Math.max(video.getDefinition().getWidth(), video.getDefinition().getHeight()),
            queryOrderBy.isAsc());
        valueBuffer.append(
            Math.min(video.getDefinition().getWidth(), video.getDefinition().getHeight()),
            queryOrderBy.isAsc());
      }
    } else {
      valueBuffer.append(queryOrderBy.isAsc() ? EMPTY_DIMENSION_ASC : EMPTY_DIMENSION_DESC,
          queryOrderBy.isAsc());
    }
  }

  private void setSize(final Media media, final ValueBuffer valueBuffer,
      final QUERY_ORDER_BY queryOrderBy) {
    if (media instanceof InternalMedia) {
      valueBuffer.append(((InternalMedia) media).getFileSize(), queryOrderBy.isAsc());
    } else {
      valueBuffer.append(
          queryOrderBy.isAsc() ? (long) EMPTY_DIMENSION_ASC : (long) EMPTY_DIMENSION_DESC,
          queryOrderBy.isAsc());
    }
  }

  private String getAuthor(final Media media) {
    String author = media.getAuthor();
    if (StringUtil.isDefined(author)) {
      author = author.toLowerCase();
    }
    return author;
  }

  private void setTitle(final Media media, final ValueBuffer valueBuffer,
      final QUERY_ORDER_BY queryOrderBy) {
    String titre = media.getTitle();
    if (StringUtil.isDefined(titre)) {
      titre = titre.toLowerCase();
    }
    valueBuffer.append(titre, queryOrderBy.isAsc());
  }
}
