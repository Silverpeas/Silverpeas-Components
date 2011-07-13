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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.kmelia.ui;

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
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * <pre>
 * This class implements a ResultDisplayer in order to customize search result display. It uses
 * "Named" annotation to inject dependency. 
 * Be careful to not modify this name that uses the following rules : componentName + POSTFIX_BEAN_NAME
 * POSTFIX_BEAN_NAME = ResultDisplayer
 * </pre>
 */
@Named("kmeliaResultDisplayer")
public class ResultSearchRenderer extends AbstractResultDisplayer implements ResultDisplayer {

  private static final Properties templateConfig = new Properties();
  private static final String TEMPLATE_FILENAME = "publication_result_template";

  /**
   * Load template configuration
   */
  static {
    ResourceLocator settings =
        new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings
        .getString("templatePath"));
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings
        .getString("customersTemplatePath"));
  }

  /**
   * Attribute loaded with dependency injection (Spring)
   */
  @Inject
  private PublicationBm publicationBm;

  @Override
  public String getResultContent(SearchResultContentVO searchResult) {
    String result = "";

    // Retrieve the event detail from silverResult
    GlobalSilverResult silverResult = searchResult.getGsr();
    PublicationPK pubPK = new PublicationPK(silverResult.getId());
    PublicationDetail pubDetail = null;
    try {
      pubDetail = getPublicationBm().getDetail(pubPK);
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", ResultSearchRenderer.class.getName() +
          ".getResultContent publicationId=" + pubPK.getId(),
          "Problem to load publication from EJB", e);
    }
    // Create a SilverpeasTemplate
    SilverpeasTemplate template = getNewTemplate();
    this.setCommonAttribute(searchResult, template);

    if (pubDetail != null) {
      template.setAttribute("pubDetail", pubDetail);
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
   * @return a publicationBm (EJB)
   */
  public PublicationBm getPublicationBm() {
    return publicationBm;
  }

  /**
   * @param publicationBm the publicationBm to set
   */
  public void setPublicationBm(PublicationBm publicationBm) {
    this.publicationBm = publicationBm;
  }

}
