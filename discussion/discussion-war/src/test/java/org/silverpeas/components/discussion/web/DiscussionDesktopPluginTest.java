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

import org.apache.ecs.ElementContainer;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionDesktopPluginTest {
  @Test
  void globallyIncludesTheDesktopListenerWhilePreservingNativePlugins() {
    var delegate = mock(WebPlugin.class);
    var original = new ElementContainer();
    when(delegate.getHtml(anyString(), anyString())).thenReturn(original);
    try (var scripts = mockStatic(JavascriptPluginInclusion.class)) {
      scripts.when(() -> JavascriptPluginInclusion.script(anyString())).thenReturn(new org.apache.ecs.xhtml.script());
      var plugin = new DiscussionDesktopPlugin(delegate);
      assertSame(original, plugin.getHtml(SupportedWebPlugin.Constants.USERNOTIFICATION, "fr"));
      scripts.verify(() -> JavascriptPluginInclusion.script(contains("discussion-desktop.js")));
      scripts.clearInvocations();
      assertSame(original, plugin.getHtml("JQUERY", "fr"));
      scripts.verifyNoInteractions();
    }
  }
}
