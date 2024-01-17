/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.components.community.web.html;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.JSONCodec;

import static org.silverpeas.components.community.CommunityComponentSettings.COMPONENT_NAME;
import static org.silverpeas.components.community.CommunityComponentSettings.getLeaveReasons;
import static org.silverpeas.core.html.SupportedWebPlugin.Constants.ADMIN_SPACE_HOMEPAGE;
import static org.silverpeas.core.html.WebPluginConsumerRegistry.add;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptSettingProducer.settingVariableName;

/**
 * @author silveryocha
 */
@Bean
public class JavascriptPluginInclusion implements Initialization {

  private static final String JAVASCRIPT_PATH = getApplicationURL() + "/" + COMPONENT_NAME + "/jsp/javaScript/";
  private static final String SERVICE_PATH = JAVASCRIPT_PATH + "services/";
  private static final String VUEJS_PATH = JAVASCRIPT_PATH + "vuejs/";
  private static final String VUEJS_COMPONENT_PATH = VUEJS_PATH + "components/";

  /**
   * Silverpeas plugin dedicated to subscription of a community.
   */
  public static final SupportedWebPlugin COMMUNITY_SUBSCRIPTION = () -> "COMMUNITYSUBSCRIPTION";

  /**
   * Silverpeas plugin dedicated to management of a community.
   */
  public static final SupportedWebPlugin COMMUNITY_MANAGEMENT = () -> "COMMUNITYMANAGEMENT";

  @Override
  public void init() {
    add(COMMUNITY_SUBSCRIPTION, JavascriptPluginInclusion::includeCommunitySubscription);
    add(COMMUNITY_MANAGEMENT, JavascriptPluginInclusion::includeCommunityManagement);
  }

  static ElementContainer includeCommunitySubscription(final ElementContainer xhtml, String language) {
    final String cssUrl = VUEJS_COMPONENT_PATH + "silverpeas-community-membership.css";
    xhtml.addElement(scriptContent(settingVariableName("CommunityMembershipSettings")
        .add("c.m.s.u", normalizeWebResourceUrl(cssUrl))
        .add("c.m.l.r", JSONCodec.encode(getLeaveReasons(language)))
        .produce()));
    xhtml.addElement(script(SERVICE_PATH + "silverpeas-community-service.js"));
    xhtml.addElement(script(SERVICE_PATH + "silverpeas-community-membership-service.js"));
    xhtml.addElement(link(cssUrl));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-community-membership.js"));
    return xhtml;
  }

  static ElementContainer includeCommunityManagement(final ElementContainer xhtml, String language) {
    xhtml.addElement(WebPlugin.get().getHtml(ADMIN_SPACE_HOMEPAGE, language));
    includeCommunitySubscription(xhtml, language);
    xhtml.addElement(script(SERVICE_PATH + "silverpeas-community-service.js"));
    xhtml.addElement(link(VUEJS_COMPONENT_PATH + "silverpeas-community.css"));
    xhtml.addElement(script(VUEJS_COMPONENT_PATH + "silverpeas-community.js"));
    return xhtml;
  }
}
