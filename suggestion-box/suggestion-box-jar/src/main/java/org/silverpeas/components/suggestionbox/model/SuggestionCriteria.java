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
package org.silverpeas.components.suggestionbox.model;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.contribution.ContributionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that permits to set suggestion search criteria for suggestion box application.
 * @author: Yohann Chastagnier
 */
public class SuggestionCriteria {

  public enum QUERY_ORDER_BY {

    TITLE_ASC("title", true), LAST_UPDATE_DATE_ASC("lastUpdateDate", true),
    TITLE_DESC("title", false), LAST_UPDATE_DATE_DESC("lastUpdateDate", false),
    VALIDATION_DATE_DESC("validationDate", false);

    private final String propertyName;
    private final boolean asc;

    private QUERY_ORDER_BY(final String propertyName, final boolean asc) {
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private SuggestionBox suggestionBox;
  private UserDetail creator;
  private final List<ContributionStatus> statuses = new ArrayList<ContributionStatus>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<QUERY_ORDER_BY>();
  private final List<String> identifiers = new ArrayList<String>();

  private SuggestionCriteria() {

  }

  /**
   * Initializes suggestion search criteria axed on the given suggestion box.
   * @param suggestionBox the base suggestion box of suggestion criteria.
   * @return an instance of suggestion criteria based on the specified suggestion box.
   */
  public static SuggestionCriteria from(SuggestionBox suggestionBox) {
    SuggestionCriteria criteria = new SuggestionCriteria();
    criteria.suggestionBox = suggestionBox;
    return criteria;
  }

  /**
   * Sets the creator criterion to find suggestion created by the given user.
   * @param user the user that must be the creator of the suggestion(s).
   * @return the suggestion criteria itself with the new criterion on the suggestion creator.
   */
  public SuggestionCriteria createdBy(UserDetail user) {
    this.creator = user;
    return this;
  }

  /**
   * Sets the list of status criterion to find suggestions which have their status equals to one of
   * the given ones.
   * @param statuses the status list that the suggestion statuses must verify.
   * @return the suggestion criteria itself with the new criterion on the suggestion statuses.
   */
  public SuggestionCriteria statusIsOneOf(ContributionStatus... statuses) {
    Collections.addAll(this.statuses, statuses);
    return this;
  }

  /**
   * Configures the order of the suggestion list.
   * @param orderBies the list of order by directives.
   * @return the suggestion criteria itself with the list ordering criterion.
   */
  public SuggestionCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    Collections.addAll(this.orderByList, orderBies);
    return this;
  }

  /**
   * Sets the identifiers criterion to find the suggestions with an identifier equals to one of the
   * specified ones.
   * @param identifiers a list of identifiers the suggestions to find should have.
   * @return the suggestion criteria itself with the new criterion on the suggestion identifiers.
   */
  public SuggestionCriteria identifierIsOneOf(String... identifiers) {
    Collections.addAll(this.identifiers, identifiers);
    return this;
  }

  /**
   * Gets the suggestion box criteria value.
   * {@link #from(SuggestionBox)}
   * @return the criterion on the suggestion box to which the suggestions should belong.
   */
  public SuggestionBox getSuggestionBox() {
    return suggestionBox;
  }

  /**
   * Gets the creator criteria value.
   * {@link #createdBy(com.stratelia.webactiv.beans.admin.UserDetail)}
   * @return the criterion on the creator of the suggestions.
   */
  private UserDetail getCreator() {
    return creator;
  }

  /**
   * Gets the statuses criteria value.
   * {@link #statusIsOneOf(org.silverpeas.contribution.ContributionStatus...)}
   * @return the criterion on the status the suggestions should match.
   */
  private List<ContributionStatus> getStatuses() {
    return statuses;
  }

  /**
   * Gets the indentifiers criteria value.
   * {@link #identifierIsOneOf(java.lang.String...)}
   * @return the criterion on the identifiers the suggestions should match.
   */
  private List<String> getIdentifiers() {
    return identifiers;
  }

  /**
   * Gets the order by directive list.
   * @return the order by directives.
   */
  private List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  /**
   * Processes this criteria with the specified processor.
   * It chains in a given order the different criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final SuggestionCriteriaProcessor processor) {
    processor.startProcessing();
    processor.processSuggestionBox(getSuggestionBox());
    if (!getIdentifiers().isEmpty()) {
      processor.then().processIdentifiers(getIdentifiers());
    }
    if (getCreator() != null) {
      processor.then().processCreator(getCreator());
    }
    if (!getStatuses().isEmpty()) {
      processor.then().processStatus(getStatuses());
    }
    if (!getOrderByList().isEmpty()) {
      processor.then().processOrdering(getOrderByList());
    }
    processor.endProcessing();
  }
}
