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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.Pair;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.*;
import static org.silverpeas.components.formsonline.model.RequestValidationCriteria.withValidatorId;
import static org.silverpeas.components.formsonline.model.RequestsByStatus.toValidateCriteriaConfigurer;
import static org.silverpeas.core.util.CollectionUtil.asSet;


/**
 * @author silveryocha
 */
class RequestsByStatusTest {

  private static final BiConsumer<Pair<Set<FormInstanceValidationType>,
        Set<FormInstanceValidationType>>, RequestValidationCriteria> TO_VALIDATE_CONFIGURER = toValidateCriteriaConfigurer;

  @Test
  void configureValidationCriteriaWhenNoFormValidation() {
    final Set<FormInstanceValidationType> noValidation = possibleFormValidationTypes();
    RequestValidationCriteria criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(noValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenFinalFormValidation() {
    final Set<FormInstanceValidationType> finalValidation = possibleFormValidationTypes(FINAL);
    RequestValidationCriteria criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(finalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenIntermediateFormValidation() {
    final Set<FormInstanceValidationType> intermediateValidation = possibleFormValidationTypes(INTERMEDIATE);
    RequestValidationCriteria criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalValidation = possibleFormValidationTypes(HIERARCHICAL);
    RequestValidationCriteria criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenIntermediateAndFinalFormValidation() {
    final Set<FormInstanceValidationType> intermediateAndFinalValidation =
        possibleFormValidationTypes(INTERMEDIATE, FINAL);
    RequestValidationCriteria criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(true));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndFinalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndFinalValidation =
        possibleFormValidationTypes(HIERARCHICAL, FINAL);
    RequestValidationCriteria criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndIntermediateFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndIntermediateValidation =
        possibleFormValidationTypes(HIERARCHICAL, INTERMEDIATE);
    RequestValidationCriteria criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndIntermediateAndFinalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndIntermediateAndFinalValidation =
        possibleFormValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL);
    RequestValidationCriteria criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), empty());

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(INTERMEDIATE));

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL, INTERMEDIATE));

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(true));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL));

    criteria = configureToValidateWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isOrValidatorIsHierarchicalOne(), is(false));
    assertThat(criteria.isOrNoValidator(), is(false));
    assertThat(criteria.getOrLastValidationType(), contains(HIERARCHICAL, INTERMEDIATE));
  }

  private RequestValidationCriteria configureToValidateWith(
      final Set<FormInstanceValidationType> possibleFormValidationTypes,
      final Set<FormInstanceValidationType> possibleValidatorValidationTypes) {
    final RequestValidationCriteria validationCriteria = withValidatorId("1", null);
    TO_VALIDATE_CONFIGURER.accept(Pair.of(new TreeSet<>(possibleFormValidationTypes),
        new TreeSet<>(possibleValidatorValidationTypes)), validationCriteria);
    assertThat(validationCriteria.getValidatorId(), is("1"));
    assertThat(validationCriteria.isAvoidValidatedByValidator(), is(true));
    assertThat(validationCriteria.isStillNeedValidation(), is(false));
    return validationCriteria;
  }

  private Set<FormInstanceValidationType> possibleFormValidationTypes(
      final FormInstanceValidationType... types) {
    return asSet(types);
  }

  private Set<FormInstanceValidationType> possibleValidatorValidationTypes(
      final FormInstanceValidationType... types) {
    return asSet(types);
  }
}