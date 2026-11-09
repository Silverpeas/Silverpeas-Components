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
package org.silverpeas.components.delegatednews.web;

import jakarta.ws.rs.core.MediaType;
import org.apache.directory.scim.server.rest.ScimJacksonXmlBindJsonProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Verifies the JSON representation of the delegated news to reorder or to delete can be read and
 * written by the REST API.
 * <p>
 * The JSON provider of the SCIM library is registered for the whole REST API of Silverpeas, so it
 * is asked whether it can read or write any JSON body. It fails with a {@link NullPointerException}
 * when asked about a Java array, whose package is null, and the request is then rejected with a
 * 500 HTTP status: the body of the request and of the response mustn't be a Java array.
 * </p>
 * @author mmoquillon
 */
class ListDelegatedNewsResourceBodyTest {

  private final ScimJacksonXmlBindJsonProvider scimJsonProvider =
      new ScimJacksonXmlBindJsonProvider();
  private Method updateOfDelegatedNews;

  @BeforeEach
  void findTheUpdateOfDelegatedNews() throws NoSuchMethodException {
    updateOfDelegatedNews =
        ListDelegatedNewsResource.class.getMethod("updateDelegatedNews", List.class);
  }

  @Test
  void theDelegatedNewsToUpdateCanBeReadFromJSON() {
    final boolean readBySCIM =
        scimJsonProvider.isReadable(updateOfDelegatedNews.getParameterTypes()[0],
            updateOfDelegatedNews.getGenericParameterTypes()[0], new Annotation[0],
            MediaType.APPLICATION_JSON_TYPE);
    assertThat(readBySCIM, is(false));
  }

  @Test
  void theUpdatedDelegatedNewsCanBeWrittenInJSON() {
    final boolean writtenBySCIM =
        scimJsonProvider.isWriteable(updateOfDelegatedNews.getReturnType(),
            updateOfDelegatedNews.getGenericReturnType(), new Annotation[0],
            MediaType.APPLICATION_JSON_TYPE);
    assertThat(writtenBySCIM, is(false));
  }
}
