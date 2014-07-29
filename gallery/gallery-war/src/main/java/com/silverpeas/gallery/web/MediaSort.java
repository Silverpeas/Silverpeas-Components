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
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaCriteria;
import com.silverpeas.gallery.model.MediaLogicalComparator;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY.*;

/**
 * This enumeration defines functional sorting rules.
 * All the rule defines can defined as not exposed to the user (displayed parameter).
 * @author: Yohann Chastagnier
 */
public enum MediaSort {
  CreationDateAsc(true, "gallery.dateCreatAsc", CREATE_DATE_ASC, IDENTIFIER_ASC),
  CreationDateDesc(true, "gallery.dateCreatDesc", CREATE_DATE_DESC, IDENTIFIER_ASC),
  Title(true, "GML.title", TITLE_ASC, CREATE_DATE_DESC, IDENTIFIER_ASC),
  Size(true, "gallery.media.size", SIZE_ASC, CREATE_DATE_DESC, IDENTIFIER_ASC),
  Author(true, "GML.author", AUTHOR_ASC_EMPTY_END, CREATE_DATE_DESC, IDENTIFIER_ASC),
  Definition(false, "gallery.dimension", DIMENSION_ASC, CREATE_DATE_DESC, IDENTIFIER_ASC);

  @SuppressWarnings("unchecked")
  public final static Set<MediaSort> ALL = UnmodifiableSet.decorate(EnumSet.allOf(MediaSort.class));

  private final boolean displayed;
  private final String bundleKey;
  private final MediaCriteria.QUERY_ORDER_BY[] orderBies;

  MediaSort(final boolean displayed, final String bundleKey,
      final MediaCriteria.QUERY_ORDER_BY... orderBies) {
    this.displayed = displayed;
    this.bundleKey = bundleKey;
    this.orderBies = orderBies;
  }

  /**
   * Gets the enum instance according to the specified type.
   * @param type
   * @return
   */
  @JsonCreator
  public static MediaSort from(String type) {
    try {
      return valueOf(type);
    } catch (Exception e) {
      return null;
    }
  }

  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * Indicates if the sorting rule can be displayed to the user.
   * @return true if the rule can be displayed, false otherwise.
   */
  public boolean isDisplayed() {
    return displayed;
  }

  /**
   * Gets the bundle key in order to perform right label into the user language.
   * @return the label bundle key.
   */
  public String getBundleKey() {
    return bundleKey;
  }

  /**
   * Performs the sort of the specified list.
   * @param mediaList
   */
  public void perform(List<Media> mediaList) {
    Collections.sort(mediaList, MediaLogicalComparator.on(orderBies));
  }
}
