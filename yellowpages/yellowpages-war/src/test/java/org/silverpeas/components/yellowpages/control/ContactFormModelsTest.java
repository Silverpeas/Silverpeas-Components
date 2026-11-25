/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.yellowpages.control;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the resolution of the XML form model with which the data of a contact are
 * exported.
 * @author mmoquillon
 */
class ContactFormModelsTest {

  private static final String INSTANCE_ID = "yellowpages12";

  private final List<String> nodesFetched = new ArrayList<>();
  private final Map<String, String> nodeModels =
      Map.of("3", "contact.xml", "4", "contact.xml", "5", "");

  private final ContactFormModels formModels = new ContactFormModels(nodeId -> {
    nodesFetched.add(nodeId);
    return nodeModels.get(nodeId);
  });

  @Test
  void aUserOfAGroupHasNoFormModelAndNoNodeIsFetchedForIt() {
    ContactFatherDetail user = userOfGroup("7");

    Optional<String> model = formModels.getFormModelOf(user);

    assertThat(model.isPresent(), is(false));
    assertThat(nodesFetched, is(empty()));
  }

  @Test
  void aContactHasTheXmlFormModelOfItsFolder() {
    Optional<String> model = formModels.getFormModelOf(contactIn("3"));

    assertThat(model.orElseThrow(), is("contact.xml"));
  }

  @Test
  void aContactInAFolderWithoutModelHasNoFormModel() {
    assertThat(formModels.getFormModelOf(contactIn("5")).isPresent(), is(false));
  }

  @Test
  void aContactInAFolderWithANonXmlModelHasNoFormModel() {
    ContactFormModels models = new ContactFormModels(nodeId -> "1");
    assertThat(models.getFormModelOf(contactIn("3")).isPresent(), is(false));
  }

  @Test
  void theModelOfAFolderIsFetchedOnlyOnce() {
    formModels.getFormModelOf(contactIn("3"));
    formModels.getFormModelOf(contactIn("4"));
    formModels.getFormModelOf(contactIn("3"));
    formModels.getFormModelOf(contactIn("5"));
    formModels.getFormModelOf(contactIn("5"));

    assertThat(nodesFetched, contains("3", "4", "5"));
  }

  private static ContactFatherDetail contactIn(String nodeId) {
    ContactDetail contact =
        new ContactDetail(new ContactPK("42", INSTANCE_ID), "John", "Doe", "john@doe.org", "", "",
            null, null, null);
    return new ContactFatherDetail(contact, nodeId, "Folder " + nodeId);
  }

  private static ContactFatherDetail userOfGroup(String groupId) {
    ContactDetail contact =
        new ContactDetail(new ContactPK("fromGroup", INSTANCE_ID), "Jane", "Doe", "jane@doe.org",
            "", "", "2", null, null);
    return new ContactFatherDetail(contact, "group_" + groupId, "Group " + groupId);
  }
}
