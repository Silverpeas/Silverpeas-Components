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

import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.stratelia.webactiv.util.DBUtil.isSqlDefined;

/**
 * Class that permits to set order search criteria for order application.
 * @author: Yohann Chastagnier
 */
public class MediaOrderCriteria {

  private String componentInstanceId;
  private final List<String> identifiers = new ArrayList<String>();
  private String ordererId;
  private Date referenceDate = DateUtil.getDate();
  private Integer nbDaysAfterThatDeleteAnOrder;

  private MediaOrderCriteria() {

  }

  /**
   * Initializes order search criteria axed on the given order component instance id.
   * @param componentInstanceId the identifier of the component instance.
   * @return an instance of order criteria based on the specified identifier of component instance.
   */
  public static MediaOrderCriteria fromComponentInstanceId(String componentInstanceId) {
    MediaOrderCriteria criteria = new MediaOrderCriteria();
    criteria.componentInstanceId = componentInstanceId;
    return criteria;
  }

  /**
   * Initializes order search criteria axed on the given order identifier.
   * @return an empty instance of order criteria.
   */
  public static MediaOrderCriteria fromNbDaysAfterThatDeleteAnOrder(int nbDays) {
    MediaOrderCriteria criteria = new MediaOrderCriteria();
    criteria.nbDaysAfterThatDeleteAnOrder = nbDays;
    return criteria;
  }

  /**
   * Sets the orderer identifier.
   * @param ordererId the orderer identifier.
   * @return an instance of order criteria with the orderer criterion filled.
   */
  public MediaOrderCriteria withOrdererId(String ordererId) {
    this.ordererId = ordererId;
    return this;
  }

  /**
   * Sets the identifiers criterion to find the orders with an identifier equals to one of the
   * specified ones.
   * @param identifiers a list of identifiers the orders to find should have.
   * @return the order criteria itself with the new criterion on the order identifiers.
   */
  public MediaOrderCriteria identifierIsOneOf(String... identifiers) {
    CollectionUtil.addAllIgnoreNull(this.identifiers, identifiers);
    return this;
  }

  /**
   * Sets the reference date criterion (the date of the day by default).
   * @param referenceDate the reference date specified.
   * @return the order criteria itself with the new criterion on the reference date.
   */
  public MediaOrderCriteria referenceDateOf(Date referenceDate) {
    if (referenceDate == null) {
      throw new IllegalArgumentException("dateReference parameter must not be null");
    }
    this.referenceDate = referenceDate;
    return this;
  }

  /**
   * Gets the indetifier of order instance.
   * {@link #fromComponentInstanceId(String)}
   * @return the criterion on the order instance to which the orders should belong.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the orderer identifier.
   * {@link #withOrdererId(String)}
   * @return the orderer identifier.
   */
  private String getOrdererId() {
    return ordererId;
  }

  /**
   * Gets the identifiers criteria value.
   * {@link #identifierIsOneOf(String...)}
   * @return the criterion on the identifiers the orders should match.
   */
  private List<String> getIdentifiers() {
    return identifiers;
  }

  /**
   * Gets the reference date.
   * @return the reference date.
   */
  private Date getReferenceDate() {
    return referenceDate;
  }

  /**
   * Gets the number of days after that an order should be deleted.
   * @return
   */
  private Integer getNbDaysAfterThatDeleteAnOrder() {
    return nbDaysAfterThatDeleteAnOrder;
  }

  /**
   * Processes this criteria with the specified processor.
   * It chains in a given order the different criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final MediaOrderCriteriaProcessor processor) {
    processor.startProcessing();
    if (StringUtil.isDefined(getComponentInstanceId())) {
      processor.processComponentInstance(getComponentInstanceId());
    }
    if (!getIdentifiers().isEmpty()) {
      processor.then().processIdentifiers(getIdentifiers());
    }
    if (isSqlDefined(getOrdererId())) {
      processor.then().processOrderer(getOrdererId());
    }
    if (getNbDaysAfterThatDeleteAnOrder() != null) {
      processor.then().processNbDaysAfterThatDeleteAnOrder(getReferenceDate(),
          getNbDaysAfterThatDeleteAnOrder());
    }

    processor.endProcessing();
  }
}
