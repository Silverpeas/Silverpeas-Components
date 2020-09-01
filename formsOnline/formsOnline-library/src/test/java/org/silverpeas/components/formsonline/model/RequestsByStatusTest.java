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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.*;
import static org.silverpeas.components.formsonline.model.RequestValidationCriteria.withValidatorId;
import static org.silverpeas.core.util.CollectionUtil.asSet;


/**
 * @author silveryocha
 */
class RequestsByStatusTest {

  private static final BiConsumer<Pair<Set<FormInstanceValidationType>,
      Set<FormInstanceValidationType>>, RequestValidationCriteria> CONFIGURER = RequestsByStatus
      .unvalidatedValidationCriteriaConfigurer();

  @Test
  void configureValidationCriteriaWhenNoFormValidation() {
    final Set<FormInstanceValidationType> noValidation = possibleFormValidationTypes();
    RequestValidationCriteria criteria = configureWith(noValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(noValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenFinalFormValidation() {
    final Set<FormInstanceValidationType> finalValidation = possibleFormValidationTypes(FINAL);
    RequestValidationCriteria criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(finalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenIntermediateFormValidation() {
    final Set<FormInstanceValidationType> intermediateValidation = possibleFormValidationTypes(INTERMEDIATE);
    RequestValidationCriteria criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalValidation = possibleFormValidationTypes(HIERARCHICAL);
    RequestValidationCriteria criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());
  }

  @Test
  void configureValidationCriteriaWhenIntermediateAndFinalFormValidation() {
    final Set<FormInstanceValidationType> intermediateAndFinalValidation =
        possibleFormValidationTypes(INTERMEDIATE, FINAL);
    RequestValidationCriteria criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(intermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(true));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndFinalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndFinalValidation =
        possibleFormValidationTypes(HIERARCHICAL, FINAL);
    RequestValidationCriteria criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndIntermediateFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndIntermediateValidation =
        possibleFormValidationTypes(HIERARCHICAL, INTERMEDIATE);
    RequestValidationCriteria criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndIntermediateValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));
  }

  @Test
  void configureValidationCriteriaWhenHierarchicalAndIntermediateAndFinalFormValidation() {
    final Set<FormInstanceValidationType> hierarchicalAndIntermediateAndFinalValidation =
        possibleFormValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL);
    RequestValidationCriteria criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes());
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), empty());

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(INTERMEDIATE));

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL, INTERMEDIATE));

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(HIERARCHICAL, INTERMEDIATE));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(true));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL));

    criteria = configureWith(hierarchicalAndIntermediateAndFinalValidation,
        possibleValidatorValidationTypes(INTERMEDIATE, FINAL));
    assertThat(criteria.isAsHierarchicalValidatorId(), is(false));
    assertThat(criteria.isNoValidator(), is(false));
    assertThat(criteria.getLastValidationType(), contains(HIERARCHICAL, INTERMEDIATE));
  }

  private RequestValidationCriteria configureWith(
      final Set<FormInstanceValidationType> possibleFormValidationTypes,
      final Set<FormInstanceValidationType> possibleValidatorValidationTypes) {
    final RequestValidationCriteria validationCriteria = withValidatorId("1", null);
    CONFIGURER.accept(Pair.of(new TreeSet<>(possibleFormValidationTypes),
        new TreeSet<>(possibleValidatorValidationTypes)), validationCriteria);
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