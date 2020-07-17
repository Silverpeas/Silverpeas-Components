/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * Class that permits to set request search criteria for FormsOnline services.
 * @author silveryocha
 */
public class RequestCriteria {

  public enum QUERY_ORDER_BY {

    CREATION_DATE_ASC("creationDate", true), CREATION_DATE_DESC("creationDate", false),
    ID_ASC("id", true), ID_DESC("id", false);

    private final String propertyName;
    private final boolean asc;

    QUERY_ORDER_BY(final String propertyName, final boolean asc) {
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

  private final List<String> componentInstanceIds = new ArrayList<>();
  private final List<String> ids = new ArrayList<>();
  private final List<String> formIds = new ArrayList<>();
  private final List<Integer> states = new ArrayList<>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<>();
  private String creatorId = EMPTY;
  private String validatorId = EMPTY;
  private boolean noValidator = false;
  private PaginationPage pagination;

  /**
   * Initializes the criteria with component instance ids.
   * <p>
   * By security, if no ids are given, the service using the criteria will return directly an
   * empty list instead of performing the sql query.
   * </p>
   * @param componentInstanceIds identifiers of component instances.
   * @return an instance of criteria.
   */
  public static RequestCriteria onComponentInstanceIds(final String... componentInstanceIds) {
    return onComponentInstanceIds(Stream.of(componentInstanceIds).collect(Collectors.toList()));
  }

  /**
   * Initializes the criteria with component instance ids.
   * <p>
   * By security, if no ids are given, the service using the criteria will return directly an
   * empty list instead of performing the sql query.
   * </p>
   * @param componentInstanceIds identifiers of component instances.
   * @return an instance of criteria.
   */
  public static RequestCriteria onComponentInstanceIds(
      final Collection<String> componentInstanceIds) {
    final RequestCriteria criteria = new RequestCriteria();
    criteria.componentInstanceIds.addAll(componentInstanceIds);
    return criteria;
  }

  /**
   * Configures the criteria of form instance ids.
   * @param ids identifiers of form instance.
   * @return an instance of criteria.
   */
  public RequestCriteria andIds(final String... ids) {
    return andIds(Stream.of(ids).collect(Collectors.toList()));
  }

  /**
   * Configures the criteria of form instance ids.
   * @param ids identifiers of form instance.
   * @return an instance of criteria.
   */
  public RequestCriteria andIds(final Collection<String> ids) {
    if (ids != null) {
      this.ids.addAll(ids);
    }
    return this;
  }

  /**
   * Configures the criteria of form ids.
   * @param formIds identifiers of forms.
   * @return an instance of criteria.
   */
  public RequestCriteria andFormIds(final String... formIds) {
    return andFormIds(Stream.of(formIds).collect(Collectors.toList()));
  }

  /**
   * Configures the criteria of form ids.
   * @param formIds identifiers of forms.
   * @return an instance of criteria.
   */
  public RequestCriteria andFormIds(final Collection<String> formIds) {
    if (formIds != null) {
      this.formIds.addAll(formIds);
    }
    return this;
  }

  /**
   * Configures the criteria of states.
   * @param states form states.
   * @return an instance of criteria.
   */
  public RequestCriteria andStates(final Integer... states) {
    return andStates(Stream.of(states).collect(Collectors.toList()));
  }

  /**
   * Configures the criteria of states.
   * @param states form states.
   * @return an instance of criteria.
   */
  public RequestCriteria andStates(final Collection<Integer> states) {
    if (states != null) {
      this.states.addAll(states);
    }
    return this;
  }

  /**
   * Configures the criteria of creator id.
   * @param creatorId identifier of a creator of a form.
   * @return an instance of criteria.
   */
  public RequestCriteria andCreatorId(final String creatorId) {
    this.creatorId = creatorId;
    return this;
  }

  /**
   * Configures the criteria of validator id or no validator.
   * @param validatorId identifier of a validator of a form or no validator.
   * @return an instance of criteria.
   */
  public RequestCriteria andValidatorIdOrNoValidator(final String validatorId) {
    this.validatorId = validatorId;
    this.noValidator = true;
    return this;
  }

  /**
   * Configures the criteria of validator id.
   * @param validatorId identifier of a validator of a form.
   * @return an instance of criteria.
   */
  public RequestCriteria andValidatorId(final String validatorId) {
    this.validatorId = validatorId;
    this.noValidator = false;
    return this;
  }

  /**
   * Configures the criteria of no validator.
   * @return an instance of criteria.
   */
  public RequestCriteria andNoValidator() {
    this.validatorId = null;
    this.noValidator = true;
    return this;
  }

  /**
   * Configures the order by clause.
   * @param orderBies the list of order by directives.
   * @return itself.
   */
  public RequestCriteria orderBy(QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  /**
   * Sets the criteria of pagination.
   * @param pagination the pagination.
   * @return itself.
   */
  public RequestCriteria paginateBy(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  boolean emptyResultWhenNoFilteringOnComponentInstances() {
    return componentInstanceIds.isEmpty();
  }

  List<String> getComponentInstanceIds() {
    return componentInstanceIds;
  }

  List<String> getIds() {
    return ids;
  }

  List<String> getFormIds() {
    return formIds;
  }

  List<Integer> getStates() {
    return states;
  }

  String getCreatorId() {
    return creatorId;
  }

  String getValidatorId() {
    return validatorId;
  }

  public boolean isNoValidator() {
    return noValidator;
  }

  PaginationPage getPagination() {
    return pagination;
  }

  List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RequestCriteria.class.getSimpleName() + "[", "]")
        .add("componentInstanceIds=" + componentInstanceIds).add("formIds=" + formIds)
        .add("states=" + states).add("orderByList=" + orderByList)
        .add("creatorId='" + creatorId + "'").add("validatorId='" + validatorId + "'")
        .add("noValidator=" + noValidator).add("pagination=" + pagination).toString();
  }
}
