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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.util.CollectionUtil;

import javax.enterprise.inject.Vetoed;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that permits to set suggestion search criteria for suggestion box application.
 * @author Yohann Chastagnier
 */
@Vetoed
public class SuggestionCriteria {

  public enum JOIN_DATA_APPLY {
    COMMENT
  }

  public enum QUERY_ORDER_BY {

    TITLE_ASC(true, "title", true),
    LAST_UPDATE_DATE_ASC(true, "lastUpdateDate", true),
    STATUS_ASC(true, "status", true),
    TITLE_DESC(true, "title", false),
    LAST_UPDATE_DATE_DESC(true, "lastUpdateDate", false),
    VALIDATION_DATE_DESC(true, "validation.validationDate", false),
    COMMENT_COUNT_DESC(false, "commentCount", false);

    private final boolean applicableOnJpaQuery;
    private final String propertyName;
    private final boolean asc;

    public static QUERY_ORDER_BY fromPropertyName(String property) {
      QUERY_ORDER_BY type = null;
      if ("commentCount".equals(property)) {
        type = COMMENT_COUNT_DESC;
      } else if ("validation.validationDate".equals(property)) {
        type = VALIDATION_DATE_DESC;
      } else if ("lastUpdateDate".equals(property)) {
        type = LAST_UPDATE_DATE_DESC;
      } else if ("title".equals(property)) {
        type = TITLE_DESC;
      }
      return type;
    }

    private QUERY_ORDER_BY(final boolean applicableOnJpaQuery, final String propertyName,
        final boolean asc) {
      this.applicableOnJpaQuery = applicableOnJpaQuery;
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public boolean isApplicableOnJpaQuery() {
      return applicableOnJpaQuery;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private SuggestionBox suggestionBox;
  private User creator;
  private final List<ContributionStatus> statuses = new ArrayList<ContributionStatus>();
  private final List<JOIN_DATA_APPLY> joinDataApplyList = new ArrayList<JOIN_DATA_APPLY>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<QUERY_ORDER_BY>();
  private final List<String> identifiers = new ArrayList<String>();
  private boolean loadWysiwygContent = false;
  private PaginationPage pagination;

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
  public SuggestionCriteria createdBy(User user) {
    this.creator = user;
    return this;
  }

  /**
   * Sets the pagination criterion on the result of the criteria.
   * @param pagination the pagination to apply on the result.
   * @return the suggestion criteria itself set with the criterion on the pagination.
   */
  public SuggestionCriteria paginatedBy(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Sets the list of status criterion to find suggestions which have their status equals to one
   * of
   * the given ones.
   * @param statuses the status list that the suggestion statuses must verify.
   * @return the suggestion criteria itself with the new criterion on the suggestion statuses.
   */
  public SuggestionCriteria statusIsOneOf(ContributionStatus... statuses) {
    CollectionUtil.addAllIgnoreNull(this.statuses, statuses);
    return this;
  }

  /**
   * Configures the data join to apply on the suggestion list.
   * @param joinDataApplies the list of join by directives.
   * @return the suggestion criteria itself with the join data list criterion.
   */
  public SuggestionCriteria applyJoinOnData(JOIN_DATA_APPLY... joinDataApplies) {
    CollectionUtil.addAllIgnoreNull(this.joinDataApplyList, joinDataApplies);
    return this;
  }

  /**
   * Configures the order of the suggestion list.
   * @param orderBies the list of order by directives.
   * @return the suggestion criteria itself with the list ordering criterion.
   */
  public SuggestionCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  /**
   * Sets the identifiers criterion to find the suggestions with an identifier equals to one of the
   * specified ones.
   * @param identifiers a list of identifiers the suggestions to find should have.
   * @return the suggestion criteria itself with the new criterion on the suggestion identifiers.
   */
  public SuggestionCriteria identifierIsOneOf(String... identifiers) {
    CollectionUtil.addAllIgnoreNull(this.identifiers, identifiers);
    return this;
  }

  /**
   * Indicates that the content of the suggestions must be loaded before returning the result.
   * @return the suggestion criteria itself with the criterion on the WYSIWYG content loading.
   */
  public SuggestionCriteria withWysiwygContent() {
    this.loadWysiwygContent = true;
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
   * {@link #createdBy(User)}
   * @return the criterion on the creator of the suggestions.
   */
  private User getCreator() {
    return creator;
  }

  /**
   * Gets the statuses criteria value.
   * {@link #statusIsOneOf(org.silverpeas.core.contribution.ContributionStatus...)}
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
   * Gets the data join by directive list.
   * @return the data join by directives.
   */
  private List<JOIN_DATA_APPLY> getJoinDataApplyList() {
    return joinDataApplyList;
  }

  /**
   * Gets the order by directive list.
   * @return the order by directives.
   */
  private List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  private PaginationPage getPagination() {
    return this.pagination;
  }

  /**
   * Indicates if suggestion contents must be loaded.
   * @return true if suggestion contents mus be loaded.
   */
  public boolean mustLoadWysiwygContent() {
    return loadWysiwygContent;
  }

  /**
   * Processes this criteria with the specified processor.
   * It chains in a given order the different criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final SuggestionCriteriaProcessor processor) {
    processor.startProcessing();
    processor.processSuggestionBox(getSuggestionBox());
    if (!getJoinDataApplyList().isEmpty()) {
      processor.processJoinDataApply(getJoinDataApplyList());
    }
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
    if (getPagination() != null) {
      processor.then().processPagination(getPagination());
    }

    processor.endProcessing();
  }
}
