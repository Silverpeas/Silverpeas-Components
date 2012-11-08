/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.ui;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.search.AbstractResultDisplayer;
import com.silverpeas.search.ResultDisplayer;
import com.silverpeas.search.SearchResultContentVO;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.silverpeas.wysiwyg.dynamicvalue.control.DynamicValueReplacement;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
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
      SilverTrace.warn("kmelia", ResultSearchRenderer.class.getName() + ".getResultContent",
          "Unable to load publication " + pubPK.getId() + " from EJB", e);
    }
    // Create a SilverpeasTemplate
    SilverpeasTemplate template = getNewTemplate();
    this.setCommonAttributes(searchResult, template);

    if (pubDetail != null) {
      setSpecificAttributes(searchResult, silverResult, pubDetail, template);
      result =
          template.applyFileTemplate(TEMPLATE_FILENAME + '_' +
              DisplayI18NHelper.getDefaultLanguage());
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

    String spaceId = silverResult.getSpaceId();
    String componentId = silverResult.getInstanceId();
    String id = silverResult.getId();
    // get user language
    String language = getUserPreferences(searchResult.getUserId()).getLanguage();

    if (WysiwygController.haveGotWysiwyg(spaceId, componentId, id)) {
      // WYSIWYG content to add inside template

      String content = "";
      try {
        content = WysiwygController.load(componentId, id, language);

        // if content not found in specified language, check other ones
        if (!StringUtil.isDefined(content)) {
          Iterator<String> languages = I18NHelper.getLanguages();
          if (languages != null) {
            while (languages.hasNext() && !StringUtil.isDefined(content)) {
              language = languages.next();
              content = WysiwygController.load(componentId, id, language);
            }
          }
        }
      } catch (WysiwygException e) {
        SilverTrace.error("kmelia", ResultSearchRenderer.class.getName() + ".getResultContent()",
            "Impossible to load WYSIWYG content", e);
      }
      // dynamic value functionnality : check if active and try to replace the keys by their
      // values
      if (DynamicValueReplacement.isActivate()) {
        DynamicValueReplacement replacement = new DynamicValueReplacement();
        content = replacement.replaceKeyByValue(content);
      }

      // Add to template only if defined
      if (StringUtil.isDefined(content)) {
        template.setAttribute("wysiwygContent", content);
      }
    } else {
      // 

      String infoId = pubDetail.getInfoId();
      String pubId = pubDetail.getPK().getId();
      if (!StringUtil.isInteger(infoId)) {
        try {
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getInstance()
                  .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + infoId);

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
            // if (kmeliaMode) {
            // xmlContext.setNodeId(kmeliaScc.getSessionTopic().getNodeDetail().getNodePK().getId());
            // }
            xmlContext.setBorderPrinted(false);
            xmlContext.setContentLanguage(language);
            
            String content = xmlForm.toString(xmlContext, data);
            // Add to template only if defined
            if (StringUtil.isDefined(content)) {
              template.setAttribute("xmlFormContent", content);
            }
          }
        } catch (PublicationTemplateException e) {
          SilverTrace.error("kmelia", ResultSearchRenderer.class.getName() +
              ".getResultContent()", "Impossible to load Publication Template", e);
        } catch (FormException e) {
          SilverTrace.error("kmelia", ResultSearchRenderer.class.getName() +
              ".getResultContent()", "Impossible to load publication form", e);
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
