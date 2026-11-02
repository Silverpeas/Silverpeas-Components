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
package org.silverpeas.components.discussion.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.apache.taglibs.standard.tag.common.core.UrlSupport;
import org.apache.taglibs.standard.tag.rt.core.UrlTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the actual JSTL URL tag under a non-root deployment context. */
class DiscussionThreadUrlTest {
  @Test
  void notificationThreadUrlAddsApplicationContextOnlyOnce() throws Exception {
    var request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/silverpeas");
    var response = mock(HttpServletResponse.class);
    when(response.getCharacterEncoding()).thenReturn("UTF-8");
    when(response.encodeURL(anyString())).thenAnswer(call -> call.getArgument(0));
    var page = mock(PageContext.class);
    when(page.getRequest()).thenReturn(request);
    when(page.getResponse()).thenReturn(response);
    String componentUri = "/Rdiscussion/discussion1/";
    String base = UrlSupport.resolveUrl(componentUri, null, page);
    assertEquals("/silverpeas/Rdiscussion/discussion1/", base);
    String jsp = Files.readString(Path.of("src/main/webapp/discussion/jsp/discussion.jsp"));
    var expression = Pattern.compile("<c:url var=\"initialThreadUrl\" value=\"([^\"]+)\"").matcher(jsp);
    assertTrue(expression.find());
    String value = expression.group(1).replace("${base}", base)
        .replace("${requestScope.componentUriBase}", componentUri)
        .replace("${initialThreadRootId}", "root-id");
    var tag = new UrlTag();
    tag.setPageContext(page);
    tag.setVar("initialThreadUrl");
    tag.setValue(value);
    tag.doStartTag();
    tag.addParameter("message", "reply-id");
    tag.doEndTag();
    verify(page).setAttribute("initialThreadUrl",
        "/silverpeas/Rdiscussion/discussion1/threads/root-id?message=reply-id", PageContext.PAGE_SCOPE);
  }
}
