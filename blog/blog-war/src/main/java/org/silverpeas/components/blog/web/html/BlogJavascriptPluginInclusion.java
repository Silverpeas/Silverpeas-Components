/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.components.blog.web.html;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.initialization.Initialization;

import static org.silverpeas.core.html.SupportedWebPlugin.Constants.IMAGETOOL;
import static org.silverpeas.core.html.WebPluginConsumerRegistry.add;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.link;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.script;

/**
 * @author silveryocha
 */
@Bean
public class BlogJavascriptPluginInclusion implements Initialization {

  private static final String JAVASCRIPT_PATH = getApplicationURL() + "/blog/jsp/js/";
  private static final String VUEJS_PATH = JAVASCRIPT_PATH + "vuejs/";
  private static final String VUEJS_COMPONENT_PATH = VUEJS_PATH + "components/";

  /**
   * Silverpeas plugin dedicated to Blog.
   */
  public static final SupportedWebPlugin BLOG = () -> "BLOG";

  @Override
  public void init() {
    add(BLOG, BlogJavascriptPluginInclusion::includeBlog);
  }

  static ElementContainer includeBlog(final ElementContainer xhtml, String language) {
    xhtml.addElement(WebPlugin.get().getHtml(IMAGETOOL, language));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "silverpeas-blog.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-blog.js"));
    return xhtml;
  }
}
