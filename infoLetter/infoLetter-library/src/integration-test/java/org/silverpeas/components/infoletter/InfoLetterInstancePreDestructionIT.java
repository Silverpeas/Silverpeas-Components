/*
 * Copyright (C) 2000 - 2022 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter;

import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.test.WarBuilder4InfoLetter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the cleaning up of all the resources related to an InfoLetter instance that
 * is being deleted.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class InfoLetterInstancePreDestructionIT {

  private static final String TABLE_CREATION_SCRIPT = "create-database.sql";
  private static final String DATASET_SCRIPT = "infoLetter-dataset.sql";

  private static final String COMPONENT_INSTANCE_ID = "infoLetter36";
  private static final String OTHER_COMPONENT_INSTANCE_ID = "infoLetter37";

  @Inject
  private InfoLetterService infoLetterService;

  @Inject
  private InfoLetterInstancePreDestruction destruction;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4InfoLetter.onWarForTestClass(
        InfoLetterInstancePreDestructionIT.class).build();
  }

  @Before
  public void beforeAnyTests() {
    assertThat(infoLetterService, notNullValue());
    assertThat(destruction, notNullValue());
  }

  @Test
  public void deleteAllToDosForAnExistingComponentInstance() {
    List<InfoLetter> infoLetters = infoLetterService.getInfoLetters(COMPONENT_INSTANCE_ID);
    List<InfoLetter> otherInfoLetters = infoLetterService.getInfoLetters(OTHER_COMPONENT_INSTANCE_ID);
    List<InfoLetterPublication> publications = infoLetters.stream()
        .flatMap(
            infoLetter -> infoLetterService.getInfoLetterPublications(infoLetter.getPK()).stream())
        .collect(Collectors.toList());
    Set<String> subscribers = infoLetters.stream()
        .flatMap(infoLetter -> infoLetterService.getEmailsExternalsSuscribers(infoLetter.getPK())
            .stream())
        .collect(Collectors.toSet());
    assertThat(infoLetters.isEmpty(), is(false));
    assertThat(publications.isEmpty(), is(false));
    assertThat(subscribers.isEmpty(), is(false));
    assertThat(otherInfoLetters.isEmpty(), is(false));

    destruction.preDestroy(COMPONENT_INSTANCE_ID);

    infoLetters = infoLetterService.getInfoLetters(COMPONENT_INSTANCE_ID);
    publications = infoLetters.stream()
        .flatMap(
            infoLetter -> infoLetterService.getInfoLetterPublications(infoLetter.getPK()).stream())
        .collect(Collectors.toList());
    subscribers = infoLetters.stream()
        .flatMap(infoLetter -> infoLetterService.getEmailsExternalsSuscribers(infoLetter.getPK())
            .stream())
        .collect(Collectors.toSet());
    assertThat(infoLetters.isEmpty(), is(true));
    assertThat(publications.isEmpty(), is(true));
    assertThat(subscribers.isEmpty(), is(true));
    assertThat(otherInfoLetters.isEmpty(), is(false));
  }

  @Test
  public void deleteAllToDosForANonExistingComponentInstance() {
    List<InfoLetter> infoLetters = infoLetterService.getInfoLetters("toto123");
    assertThat(infoLetters.isEmpty(), is(true));

    destruction.preDestroy(COMPONENT_INSTANCE_ID);

    infoLetters = infoLetterService.getInfoLetters("toto123");
    assertThat(infoLetters.isEmpty(), is(true));
  }
}
