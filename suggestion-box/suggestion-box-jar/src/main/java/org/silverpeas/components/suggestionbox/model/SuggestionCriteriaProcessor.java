/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY;
import org.silverpeas.contribution.ContributionStatus;

import java.util.List;

/**
 * A processor of a suggestion criteria. The aim of a such processor is to process each
 * criterion of the criteria in the order expected by the caller in order to perform some specific
 * works.
 * @author mmoquillon
 */
public interface SuggestionCriteriaProcessor {

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
  SuggestionCriteriaProcessor then();

  /**
   * Processes the criterion on the suggestion box.
   * @param box the suggestion box concerned by the criterion.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processSuggestionBox(final SuggestionBox box);

  /**
   * Processes the criterion on the creator of the suggestions.
   * @param creator the user concerned by the criterion.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processCreator(final UserDetail creator);

  /**
   * Processes the criterion on suggestion status.
   * @param status the suggestion status concerned by the criterion.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processStatus(final List<ContributionStatus> status);

  /**
   * Processes the criterion on orderings of the suggestions matching the criteria.
   * @param orderings the result orderings concerned by the criterion.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processOrdering(final List<QUERY_ORDER_BY> orderings);

  /**
   * Processes the criterion on the suggestion identifiers.
   * @param identifiers the suggestion identifiers concerned by the criterion.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processIdentifiers(final List<String> identifiers);

  /**
   * Processes the criterion on the pagination to apply on the suggestions to return.
   * @param pagination a pagination definition.
   * @return the processor itself.
   */
  SuggestionCriteriaProcessor processPagination(final PaginationPage pagination);

  /**
   * Gets the result of the processing. Warning, the result can be incomplete if called before the
   * processing ending (triggered with the call of {@link #endProcessing()} method).
   * @param <T> the type of the result.
   * @return the processing result.
   */
  <T> T result();
}
