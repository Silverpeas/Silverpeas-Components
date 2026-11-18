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
package org.silverpeas.components.directoryviewer.servlets;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectoryViewerRequestRouterTest {

  @Test
  void routesOtherFunctionsWithoutParameters() {
    assertEquals("/Rdirectory/jsp/Other",
        DirectoryViewerRequestRouter.destination("Other", name -> "ignored"));
  }

  @Test
  void routesMainWithoutConfiguredParameters() {
    assertEquals("/Rdirectory/jsp/Main",
        DirectoryViewerRequestRouter.destination("Main", name -> ""));
  }

  @Test
  void selectsFirstConfiguredScope() {
    Map<String, String> values = Map.of("GroupIds", "12,34", "SpaceId", "7",
        "DomainIds", "3", "ComponentId", "contacts42");
    assertEquals("/Rdirectory/jsp/Main?GroupIds=12%2C34",
        DirectoryViewerRequestRouter.destination("Main", values::get));
  }

  @Test
  void fallsBackToNextScope() {
    Map<String, String> values = Map.of("GroupIds", " ", "SpaceId", " 7 ");
    assertEquals("/Rdirectory/jsp/Main?SpaceId=7",
        DirectoryViewerRequestRouter.destination("Main", values::get));
  }

  @Test
  void encodesSpecialCharacters() {
    Map<String, String> values = Map.of("GroupIds", "a&b c/é");
    assertEquals("/Rdirectory/jsp/Main?GroupIds=a%26b+c%2F%C3%A9",
        DirectoryViewerRequestRouter.destination("Main", values::get));
  }

  @Test
  void forwardsConfiguredDisplayOptions() {
    Map<String, String> values = Map.of("View", "connected", "Sort", "ALPHA",
        "InWholeSpaceTree", "yes", "DoNotUseContacts", "no",
        "DoNotUseExtraForm", "yes");
    assertEquals("/Rdirectory/jsp/Main?InWholeSpaceTree=yes&Sort=ALPHA&View=connected" +
            "&DoNotUseContacts=no&DoNotUseExtraForm=yes",
        DirectoryViewerRequestRouter.destination("Main", values::get));
  }

  @Test
  void routesPortletToMain() {
    assertEquals("/Rdirectory/jsp/Main?DomainIds=1",
        DirectoryViewerRequestRouter.destination("portlet", name ->
            "DomainIds".equals(name) ? "1" : null));
  }
}
