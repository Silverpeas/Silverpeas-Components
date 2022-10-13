/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.util.MemoizedSupplier;

import java.util.Collection;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

/**
 * Class that permits to set request search criteria oriented on validation.
 * @author silveryocha
 */
public class RequestValidationCriteria {

  private final String validatorId;
  private final MemoizedSupplier<Set<String>> managedDomainUsersSupplier;
  private boolean skipValidationFiltering = false;
  private boolean avoidValidatedByValidator = false;
  private boolean stillNeedValidation = false;
  private boolean invert = false;
  private boolean orNoValidator = false;
  private boolean orValidatorIsHierarchicalOne = false;
  private final Set<FormInstanceValidationType> orLastValidationType = new TreeSet<>();

  public RequestValidationCriteria(final String validatorId,
      final MemoizedSupplier<Set<String>> managedDomainUsersSupplier) {
    this.validatorId = validatorId;
    this.managedDomainUsersSupplier = managedDomainUsersSupplier;
  }

  /**
   * Initializes the criteria with validator id.
   * @return an instance of criteria.
   */
  public static RequestValidationCriteria withValidatorId(final String validatorId,
      final MemoizedSupplier<Set<String>> managedDomainUsersSupplier) {
    return new RequestValidationCriteria(validatorId, managedDomainUsersSupplier);
  }

  /**
   * Configures the criteria of lastValidationType.
   * @param states form states.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria orLastValidationType(final FormInstanceValidationType... states) {
    return orLastValidationType(Stream.of(states).collect(Collectors.toList()));
  }

  /**
   * Configures the criteria of last validation types.
   * @param validationTypes validation types.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria orLastValidationType(final Collection<FormInstanceValidationType> validationTypes) {
    this.orLastValidationType.addAll(validationTypes);
    return this;
  }

  /**
   * Configures the criteria of hierarchical validator search.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria orValidatorIsHierarchicalOne() {
    this.orValidatorIsHierarchicalOne = true;
    return this;
  }

  /**
   * Configures the criteria of no validator.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria orNoValidator() {
    this.orNoValidator = true;
    return this;
  }

  /**
   * Configures the criteria of still need validation.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria andStillNeedValidation() {
    this.stillNeedValidation = true;
    return this;
  }

  /**
   * Configures the criteria of inversion of all clauses.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria invert() {
    this.invert = true;
    return this;
  }

  /**
   * Configures the criteria of avoiding the validated by validator.
   * @return an instance of criteria.
   */
  public RequestValidationCriteria andAvoidValidatedByValidator() {
    this.avoidValidatedByValidator = true;
    return this;
  }

  /**
   * If called, validation filtering is not performed.
   */
  public void skipValidationFiltering() {
    this.skipValidationFiltering = true;
  }

  String getValidatorId() {
    return validatorId;
  }

  boolean isOrNoValidator() {
    return orNoValidator;
  }

  boolean isOrValidatorIsHierarchicalOne() {
    return orValidatorIsHierarchicalOne;
  }

  Set<String> getManagedDomainUsers() {
    if (!orValidatorIsHierarchicalOne) {
      return emptySet();
    }
    return managedDomainUsersSupplier.get();
  }

  Set<FormInstanceValidationType> getOrLastValidationType() {
    return orLastValidationType;
  }

  boolean isStillNeedValidation() {
    return stillNeedValidation;
  }

  boolean isInvert() {
    return invert;
  }

  boolean isAvoidValidatedByValidator() {
    return avoidValidatedByValidator;
  }

  boolean isSkipValidationFiltering() {
    return skipValidationFiltering;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RequestValidationCriteria.class.getSimpleName() + "[", "]")
        .add("validatorId='" + validatorId + "'")
        .add("skipValidationFiltering=" + skipValidationFiltering)
        .add("avoidValidatedByValidator=" + avoidValidatedByValidator)
        .add("stillNeedValidation=" + stillNeedValidation).add("invert=" + invert)
        .add("orNoValidator=" + orNoValidator)
        .add("orValidatorIsHierarchicalOne=" + orValidatorIsHierarchicalOne)
        .add("orLastValidationType=" + orLastValidationType).toString();
  }
}
