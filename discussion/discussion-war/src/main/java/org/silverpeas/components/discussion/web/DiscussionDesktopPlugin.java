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

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import org.apache.ecs.ElementContainer;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;

/** Adds the desktop listener to the existing global notification plugin without replacing it. */
@Decorator
@Priority(Interceptor.Priority.APPLICATION + 100)
public class DiscussionDesktopPlugin implements WebPlugin {
  @Inject
  @Delegate
  @Any
  private WebPlugin delegate;

  public DiscussionDesktopPlugin() { }
  DiscussionDesktopPlugin(WebPlugin delegate) { this.delegate = delegate; }

  @Override
  public ElementContainer getHtml(SupportedWebPlugin plugin, String language) {
    return getHtml(plugin.getName(), language);
  }

  @Override
  public ElementContainer getHtml(String plugin, String language) {
    var html = delegate.getHtml(plugin, language);
    if (SupportedWebPlugin.Constants.USERNOTIFICATION.getName().equalsIgnoreCase(plugin)) {
      html.addElement(JavascriptPluginInclusion.script(URLUtil.getApplicationURL()
          + "/discussion/jsp/javaScript/discussion-desktop.js"));
    }
    return html;
  }
}
