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


import java.util.Date;
import java.util.List;


/**
 * A processor of a order criteria. The aim of a such processor is to process each
 * criterion of the criteria in the order expected by the caller in order to perform some specific
 * works.
 * @author mmoquillon
 */
public interface MediaOrderCriteriaProcessor {

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
  MediaOrderCriteriaProcessor then();

  /**
   * Processes the criterion on the order identifiers.
   * @param identifiers the order identifiers concerned by the criterion.
   * @return the processor itself.
   */
  MediaOrderCriteriaProcessor processIdentifiers(final List<String> identifiers);

  /**
   * Processes the criterion on the component instance identifier.
   * @param componentInstanceId the identifier of the component instance concerned by the
   * criterion.
   * @return the processor itself.
   */
  MediaOrderCriteriaProcessor processComponentInstance(final String componentInstanceId);

  /**
   * Processes the criterion on the orderer identifier of the orders.
   * @param ordererId the orderer user concerned by the criterion.
   * @return the processor itself.
   */
  MediaOrderCriteriaProcessor processOrderer(final String ordererId);

  /**
   * Processes the criterion on the nb of days after that an order should be deleted.
   * @param referenceDate
   * @param nbDaysAfterThatDeleteAnOrder the nb of days after that an order should be deleted.
   * @return the processor itself.
   */
  MediaOrderCriteriaProcessor processNbDaysAfterThatDeleteAnOrder(final Date referenceDate, final int nbDaysAfterThatDeleteAnOrder);

  /**
   * Gets the result of the processing. Warning, the result can be incomplete if called before the
   * processing ending (triggered with the call of {@link #endProcessing()} method).
   * @param <T> the type of the result.
   * @return the processing result.
   */
  <T> T result();

  /**
   * This method must be called after the order list is entirely loaded.
   * If an ordering was specified and if it was not possible to perform it by SQL clauses, then a
   * logical sort is performed.
   * @param order
   */
  List<Order> orderingResult(List<Order> order);
}
