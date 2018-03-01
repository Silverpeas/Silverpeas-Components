/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.ui;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.web.search.AbstractResultDisplayer;
import org.silverpeas.core.web.search.SearchResultContentVO;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.Properties;

/**
 * <pre>
 * This class implements a ResultDisplayer in order to customize search result display. It uses
 * "Named" annotation to inject dependency.
 * Be careful to not modify this name that uses the following rules : componentName + POSTFIX_BEAN_NAME
 * POSTFIX_BEAN_NAME = ResultDisplayer
 * </pre>
 */
@Named("kmeliaResultDisplayer")
public class ResultSearchRenderer extends AbstractResultDisplayer {

  private static final Properties templateConfig = new Properties();
  private static final String TEMPLATE_FILENAME = "publication_result_template";

  /**
   * Load template configuration
   */
  static {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings
        .getString("templatePath"));
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings
        .getString("customersTemplatePath"));
  }
  /**
   * Attribute loaded with dependency injection
   */
  @Inject
  private PublicationService publicationService;

  @Override
  public String getResultContent(SearchResultContentVO searchResult) {
    String result = "";

    // Retrieve the event detail from silverResult
    GlobalSilverResult silverResult = searchResult.getGsr();
    PublicationPK pubPK = new PublicationPK(silverResult.getId());
    PublicationDetail pubDetail = null;
    try {
      pubDetail = getPublicationService().getDetail(pubPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("Unable to load publication {0}: {1}", pubPK.toString(), e.getMessage());
    }
    // Create a SilverpeasTemplate
    SilverpeasTemplate template = getNewTemplate();
    this.setCommonAttributes(searchResult, template);

    if (pubDetail != null) {
      setSpecificAttributes(searchResult, silverResult, pubDetail, template);
      result = template.applyFileTemplate(TEMPLATE_FILENAME + '_' + DisplayI18NHelper.
          getDefaultLanguage());
    }
    return result;
  }

  /**
   *
   * @param searchResult the SearchResultContentVO data from search engine
   * @param silverResult the current GlobalSilverResult object
   * @param pubDetail the current PublicationDetail object
   * @param template the current SilverpeasTemplate to set
   */
  private void setSpecificAttributes(SearchResultContentVO searchResult,
      GlobalSilverResult silverResult, PublicationDetail pubDetail, SilverpeasTemplate template) {
    template.setAttribute("pubDetail", pubDetail);

    if (StringUtil.isDefined(pubDetail.getAuthor())) {
      template.setAttribute("pubAuthor", pubDetail.getAuthor());
    }

    if (StringUtil.isDefined(pubDetail.getCreatorName())) {
      template.setAttribute("pubCreatorName", pubDetail.getCreatorName());
    }

    if (StringUtil.isDefined(pubDetail.getKeywords())) {
      template.setAttribute("pubKeywords", pubDetail.getKeywords());
    }

    if (StringUtil.isDefined(pubDetail.getContent())) {
      template.setAttribute("pubContent", pubDetail.getContent());
    }
    String componentId = silverResult.getInstanceId();
    String id = silverResult.getId();
    // get user language
    String language = getUserPreferences(searchResult.getUserId()).getLanguage();
    if (WysiwygController.haveGotWysiwyg(componentId, id, language)) {

      // WYSIWYG content to add inside template
      String content = WysiwygController.load(componentId, id, language);

      // if content not found in specified language, check other ones
      if (!StringUtil.isDefined(content)) {
        Iterator<String> languages = I18NHelper.getLanguages();
        while (languages.hasNext() && !StringUtil.isDefined(content)) {
          language = languages.next();
          content = WysiwygController.load(componentId, id, language);
        }
      }

      // Add to template only if defined
      if (StringUtil.isDefined(content)) {
        template.setAttribute("wysiwygContent", content);
      }
    } else {
      String infoId = pubDetail.getInfoId();
      String pubId = pubDetail.getPK().getId();
      if (!StringUtil.isInteger(infoId)) {
        try {
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getInstance().
              getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + infoId);

          // RecordTemplate recordTemplate = pubTemplate.getRecordTemplate();
          Form xmlForm = pubTemplate.getSearchResultForm();

          RecordSet recordSet = pubTemplate.getRecordSet();
          DataRecord data = recordSet.getRecord(pubId, language);
          if (data == null) {
            data = recordSet.getEmptyRecord();
            data.setId(pubId);
          }

          if (xmlForm != null) {
            PagesContext xmlContext = new PagesContext("myForm", "0", language,
                false, componentId, searchResult.getUserId());
            xmlContext.setObjectId(id);
            xmlContext.setBorderPrinted(false);
            xmlContext.setContentLanguage(language);

            String content = xmlForm.toString(xmlContext, data);
            // Add to template only if defined
            if (StringUtil.isDefined(content)) {
              template.setAttribute("xmlFormContent", content);
            }
          }
        } catch (PublicationTemplateException e) {
          SilverLogger.getLogger(this).error("Failed to load publication template", e);
        } catch (FormException e) {
          SilverLogger.getLogger(this).error("Failed to load publication form", e);
        }
      }
    }
  }

  /**
   * @return a new Silverpeas Template
   */
  protected SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfig);
  }

  /**
   * @return a publication service
   */
  public PublicationService getPublicationService() {
    return publicationService;
  }
}
