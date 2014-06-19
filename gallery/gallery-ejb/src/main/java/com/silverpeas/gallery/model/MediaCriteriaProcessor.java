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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaType;
import com.stratelia.webactiv.beans.admin.UserDetail;

import java.util.Date;
import java.util.List;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY;
import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY;

/**
 * A processor of a media criteria. The aim of a such processor is to process each
 * criterion of the criteria in the order expected by the caller in order to perform some specific
 * works.
 * @author mmoquillon
 */
public interface MediaCriteriaProcessor {

  /**
   * Informs the processor the start of the process. The processor use this method to allocate all
   * the resources required by the processing here. It uses it to initialize the processor state
   * machine.
   */
  void startProcessing();

  /**
   * Informs the processor the process is ended. The processor use this method to deallocate all
   * the resources that were used during the processing. It uses it to tear down the processor
   * state
   * machine or to finalize some treatments.
   * <p/>
   * The processing has to stop once this method is called. Hence, the call of process methods
   * should result to nothing or to an exception.
   */
  void endProcessing();

  /**
   * Informs the processor that there is a new criterion to process. This method must be used by
   * the caller to chain the different criterion processings.
   * @return the processor itself.
   */
  MediaCriteriaProcessor then();

  /**
   * Processes the criterion on the media visibility.
   * @param visibility the requested visibility.
   * @param dateReference
   * @return the processor itself.
   */
  MediaCriteriaProcessor processVisibility(final VISIBILITY visibility, final Date dateReference);

  /**
   * Processes the criterion on the media identifiers.
   * @param identifiers the media identifiers concerned by the criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processIdentifiers(final List<String> identifiers);

  /**
   * Processes the criterion on the component instance identifier.
   * @param componentInstanceId the identifier of the component instance concerned by the
   * criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processComponentInstance(final String componentInstanceId);

  /**
   * Processes the criterion on the identifiers of albums (Actually, Album = Node).
   * @param albumIds the identifiers of albums concerned by the criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processAlbums(final List<String> albumIds);

  /**
   * Processes the criterion on the creator of the medias.
   * @param creator the user concerned by the criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processCreator(final UserDetail creator);

  /**
   * Processes the criterion on type of media.
   * @param mediaTypes the media types concerned by the criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processMediaTypes(final List<MediaType> mediaTypes);

  /**
   * Processes the criterion on the nb of days before that a media is not visible.
   *
   * @param referenceDate
   * @param nbDaysBeforeThatMediaIsNotVisible the nb of days before that a media is not visible.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processNbDaysBeforeThatMediaIsNotVisible(final Date referenceDate,
      final int nbDaysBeforeThatMediaIsNotVisible);

  /**
   * Processes the criterion on orderings of the medias matching the criteria.
   * @param orderings the result orderings concerned by the criterion.
   * @return the processor itself.
   */
  MediaCriteriaProcessor processOrdering(final List<QUERY_ORDER_BY> orderings);

  /**
   * Gets the result of the processing. Warning, the result can be incomplete if called before the
   * processing ending (triggered with the call of {@link #endProcessing()} method).
   * @param <T> the type of the result.
   * @return the processing result.
   */
  <T> T result();

  /**
   * This method must be called after the media list is entirely loaded.
   * If an ordering was specified and if it was not possible to perform it by SQL clauses, then a
   * logical sort is performed.
   * @param media
   */
  List<Media> orderingResult(List<Media> media);
}
