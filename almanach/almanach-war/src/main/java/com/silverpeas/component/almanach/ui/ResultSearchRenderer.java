/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.component.almanach.ui;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.search.AbstractResultDisplayer;
import com.silverpeas.search.ResultDisplayer;
import com.silverpeas.search.SearchResultContentVO;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * <pre>
 * This class implements a ResultDisplayer in order to customize search result display. It uses
 * "Named" annotation to inject dependency. 
 * Be careful to not modify this name that uses the following rules : componentName + POSTFIX_BEAN_NAME
 * POSTFIX_BEAN_NAME = ResultDisplayer
 * </pre>
 */
@Named("almanachResultDisplayer")
public class ResultSearchRenderer extends AbstractResultDisplayer implements ResultDisplayer {

  private static final Properties templateConfig = new Properties();
  private static final String TEMPLATE_FILENAME = "event_result_template";

  /**
   * Load template configuration
   */
  static {
    ResourceLocator settings =
        new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings", "");
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings
        .getString("templatePath"));
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings
        .getString("customersTemplatePath"));
  }

  /**
   * Attribute loaded with dependency injection (Spring)
   */
  @Inject
  private AlmanachBm almanachBm;

  @Override
  public String getResultContent(SearchResultContentVO searchResult) {
    String result = "";

    // Retrieve the event detail from silverResult
    GlobalSilverResult silverResult = searchResult.getGsr();
    EventPK eventPK = new EventPK(silverResult.getId());
    EventDetail event = null;
    try {
      event = getAlmanachBm().getEventDetail(eventPK);
    } catch (RemoteException e) {
      SilverTrace.error("almanach", ResultSearchRenderer.class.getName() +
          ".getResultContent eventId=" + eventPK.getId(), "Problem to load event from EJB", e);
    }
    // Create a SilverpeasTemplate
    SilverpeasTemplate template = getNewTemplate();
    this.setCommonAttribute(searchResult, template);

    if (event != null) {
      template.setAttribute("eventDetail", event);
      if (template != null) {
        result =
            template.applyFileTemplate(TEMPLATE_FILENAME + '_' +
                DisplayI18NHelper.getDefaultLanguage());
      }
    }
    return result;
  }

  /**
   * @return a new Silverpeas Template
   */
  protected SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfig);
  }

  /**
   * @return the almanachBm
   */
  public AlmanachBm getAlmanachBm() {
    return almanachBm;
  }

  /**
   * @param almanachBm the almanachBm to set
   */
  public void setAlmanachBm(AlmanachBm almanachBm) {
    this.almanachBm = almanachBm;
  }

}
