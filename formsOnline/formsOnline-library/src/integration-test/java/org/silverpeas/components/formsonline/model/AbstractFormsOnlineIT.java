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

import org.junit.Rule;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;
import org.silverpeas.core.test.integration.rule.TestStatisticRule;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.stream.Stream.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.*;
import static org.silverpeas.core.contribution.ContributionStatus.REFUSED;
import static org.silverpeas.core.contribution.ContributionStatus.VALIDATED;

/**
 * @author silveryocha
 */
public abstract class AbstractFormsOnlineIT {

  @Rule
  public TestStatisticRule testStatisticRule = new TestStatisticRule();

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "forms-dataset.xml");

  static final String DYNAMIC_DATA_INSTANCE_ID = "formsOnline26";
  static final String DEFAULT_INSTANCE_ID = "formsOnline100";

  static final String VALIDATOR_ID_29 = "29";
  static final String VALIDATOR_ID_30 = "30";
  static final String VALIDATOR_ID_31 = "31";

  static final String H_DATE = "2020-01-01 06:54:23.26";
  static final String I_DATE = "2020-02-02 11:55:34.38";
  static final String F_DATE = "2020-03-03 15:08:01.07";
  static final String[] DEFAULT_CREATOR_IDS = new String[] {
      "1", "2"
  };
  static final Integer[] DEFAULT_FORM_STATES = new Integer[] {
      FormInstance.STATE_UNREAD, FormInstance.STATE_READ
  };
  // validationBy, validationType, status, follower, validationDate, validationComment
  static final Object[] DEFAULT_VALIDATION_CYCLE = new Object[] {
      null,
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, false, H_DATE, "OK"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, INTERMEDIATE, VALIDATED, true, I_DATE, "Bon d'accord"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, FINAL, VALIDATED, false, F_DATE, "Validé"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, REFUSED, false, H_DATE, "Ha non"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, INTERMEDIATE, REFUSED, false, I_DATE, "non non non"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, FINAL, REFUSED, false, F_DATE, ":-("}
      },
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, false, H_DATE, "OK"},
          {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, false, I_DATE, "OK aussi"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, true, H_DATE, "OK"},
          {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, true, I_DATE, "OK aussi"},
          {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "Bon ben d'accord alors"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, false, H_DATE, "OK"},
          {VALIDATOR_ID_31, FINAL, REFUSED, false, F_DATE, "En fait non !"}
      },
      new Object[][] {
          {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, false, I_DATE, "OK"},
          {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "OK !"}
      },
      new Object[][] {
          {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, true, H_DATE, "OK"},
          {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, true, I_DATE, "OK aussi"},
          {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "Bon ben d'accord alors"}
      }
  };

  /**
   * Creates 10 forms with 50000 requests
   * Validations: DEFAULT_VALIDATION_CYCLE.
   * @return the created form instance ids.
   * @param nbForms nb forms.
   * @param nbRequests nb requests by form.
   */
  List<String> createDefaultDynamicContextOfData(final int nbForms, final int nbRequests) {
    return createDynamicContextOfData(nbForms, nbRequests, DEFAULT_CREATOR_IDS,
        DEFAULT_FORM_STATES, DEFAULT_VALIDATION_CYCLE);
  }

  /**
   * Creates 10 forms with 50000 requests
   * Validations: DEFAULT_VALIDATION_CYCLE.
   * @return the created form instance ids.
   * @param nbForms nb forms.
   * @param nbRequests nb requests by form.
   * @param creators the cycle of creator ids to use.
   * @param formStates the cycle of states to use.
   * @param validations the cycle of validation to use.
   */
  List<String> createDynamicContextOfData(final int nbForms, final int nbRequests,
      final String[] creators, final Integer[] formStates, final Object[] validations) {
    final int idOffset = 100001;
    testStatisticRule.log("creating " + nbForms + " forms with " + nbRequests + " requests per form...");
    final long start = System.currentTimeMillis();
    final List<String> requestIds = new ArrayList<>(nbForms);
    final StringBuilder forms = new StringBuilder();
    final StringBuilder requests = new StringBuilder();
    final StringBuilder requestValidations = new StringBuilder();
    final Mutable<Integer> formId = Mutable.of(idOffset);
    final Mutable<Integer>  requestId = Mutable.of(idOffset);
    final Mutable<Integer>  validationId = Mutable.of(idOffset);
    IntStream.rangeClosed(1, nbForms).forEach(f -> {
      of(forms).filter(b -> b.length() > 0).forEach(b -> b.append(","));
      final String formIdAsString = formId.map(String::valueOf).get();
      formId.set(formId.get() + 1);
      forms.append(format(
          "({0}, ''descriptif_salle.xml'', ''Référencement des salles'', " +
              "''Formulaire de description d'une salle'', ''2020-01-01 12:30:45.78'', 1, " +
              "''formsOnline26'', 1 , ''1'', ''Titre de mon formulaire en ligne'')",
          formIdAsString));
      IntStream.rangeClosed(1, nbRequests).forEach(r -> {
        of(requests).filter(b -> b.length() > 0).forEach(b -> b.append(","));
        final String requestIdAsString = requestId.map(String::valueOf).get();
        requestId.set(requestId.get() + 1);
        requestIds.add(requestIdAsString);
        final int formStateIndex = (r - 1) % formStates.length;
        final int creatorIdIndex = (r - 1) % creators.length;
        requests.append(format("({0}, {1}, {2}, ''{3}'', ''2020-01-01 08:00:00.0'', ''formsOnline26'')",
            requestIdAsString, formIdAsString, formStates[formStateIndex], creators[creatorIdIndex]));
        final int validationIdIndex = (r - 1) % validations.length;
        final Object[][] currentValidations = (Object[][]) validations[validationIdIndex];
        if (currentValidations != null) {
          for (final Object[] v : currentValidations) {
            of(requestValidations).filter(b -> b.length() > 0).forEach(b -> b.append(","));
            final String validationIdAsString = validationId.map(String::valueOf).get();
            validationId.set(validationId.get() + 1);
            requestValidations.append(format("({0}, {1}, ''{2}'', ''{3}'', ''{4}'', {5}, ''{6}'', ''{7}'')",
                validationIdAsString, requestIdAsString, v[0], v[1], v[2], v[3], v[4], v[5].toString().replace("'", "''")));
          }
        }
      });
      if (requestId.get() % 10000 == 0) {
        performQueries(forms, requests, requestValidations);
      }
    });
    performQueries(forms, requests, requestValidations);
    testStatisticRule
        .log("ending the creation of " + nbForms + " forms with " + nbRequests + " requests per form", start,
            System.currentTimeMillis());
    return requestIds;
  }

  private void performQueries(final StringBuilder forms, final StringBuilder requests,
      final StringBuilder validations) {
    if (forms.length() > 0) {
      forms.insert(0, "id, xmlFormName, name, description, creationDate, state, instanceId, alreadyUsed, creatorId, title) VALUES ");
      forms.insert(0, "INSERT INTO SC_FormsOnline_Forms (");
    }
    if (requests.length() > 0) {
      requests.insert(0,
          "INSERT INTO sc_formsonline_forminstances (id, formId, state, creatorId, creationDate, instanceId) VALUES ");
    }
    if (validations.length() > 0) {
      validations.insert(0,
          "INSERT INTO sc_formsonline_forminstvali (id, formInstId, validationBy, validationType, status, follower, validationDate, validationComment) VALUES ");
    }
    executePlainQueries(forms.toString(), requests.toString(), validations.toString());
    of(forms, requests, validations).forEach(b -> b.setLength(0));
  }

  private void executePlainQueries(final String... plainQueries) {
    try (final Connection connection = DbSetupRule.getSafeConnection()) {
      for(final String query : plainQueries) {
        if (StringUtil.isNotDefined(query)) {
          continue;
        }
        try (final PreparedStatement ps = connection.prepareStatement(query)) {
          testStatisticRule.log("registering " + ps.executeUpdate() + " lines");
        }
      }
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  void assertContainsIdsWithOffsetAutomaticallyApplied(final List<FormInstance> requests, Integer... ids) {
    assertThat(requests.stream().map(FormInstance::getId).collect(Collectors.toList()),
        contains(Stream.of(ids).map(i -> String.valueOf(i + 100000)).toArray(String[]::new)));
  }
}
