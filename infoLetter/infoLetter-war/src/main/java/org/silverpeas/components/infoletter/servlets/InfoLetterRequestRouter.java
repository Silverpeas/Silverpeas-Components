/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.infoletter.control.InfoLetterSessionController;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.core.contribution.content.ddwe.DragAndDropWbeFile;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore;
import org.silverpeas.core.contribution.content.renderer.ContributionContentRenderer;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarElement;
import org.silverpeas.core.webapi.wbe.WbeFileEdition;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.*;
import static org.silverpeas.core.web.ddwe.DragAndDropEditorConfig.withConnectors;

/**
 * Class declaration
 * @author
 */
public class InfoLetterRequestRouter extends ComponentRequestRouter<InfoLetterSessionController> {

  private static final long serialVersionUID = 5722456216811272025L;
  private static final String ADMIN = "admin";
  private static final String PUBLISHER = "publisher";
  private static final String PARUTION = "parution";
  private static final String PARUTION_TITLE = "parutionTitle";
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "description";
  private static final String BROWSE_BAR_PATH = "browseBarPath";
  private static final String LIST_EMAILS = "listEmails";
  private static final String INLINED_CSS_HTML = "inlinedCssHtml";
  private static final String HEADER_LETTER_JSP = "headerLetter.jsp";
  private static final String EMAILS_MANAGER_JSP = "emailsManager.jsp";

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  @Override
  public InfoLetterSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new InfoLetterSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "infoLetter";
  }

  /**
   * @param infoLetterSC
   * @param request
   */
  private void setGlobalInfo(InfoLetterSessionController infoLetterSC, HttpServletRequest request) {
    String language = infoLetterSC.getLanguage();
    // the flag is the best user's profile
    String flag = getFlag(infoLetterSC.getUserRoles());
    request.setAttribute("userIsAdmin", ADMIN.equals(flag));
    request.setAttribute("isPdcUsed", infoLetterSC.isPdcUsed());
    request.setAttribute("language", language);
    request.setAttribute("browseContext",
        new String[]{infoLetterSC.getSpaceLabel(), infoLetterSC.getComponentLabel(),
            infoLetterSC.getSpaceId(), infoLetterSC.getComponentId(),
            infoLetterSC.getComponentUrl()});
  }

  private String setMainContext(InfoLetterSessionController infoLetterSC,
      HttpServletRequest request) {
    // the flag is the best user's profile
    loadCommonHomepageDataAndSetRequestAttributes(infoLetterSC, request);
    final boolean showHeader = infoLetterSC.getSettings().getBoolean("showHeader", false);
    request.setAttribute("showHeader", showHeader);
    final boolean isTemplateExist = infoLetterSC.getInfoLetter().existsTemplateContent();
    request.setAttribute("IsTemplateExist", isTemplateExist);
    final String flag = getFlag(infoLetterSC.getUserRoles());
    return (PUBLISHER.equals(flag) || ADMIN.equals(flag)) ?
        "listLetterAdmin.jsp" :
        "listLetterUser.jsp";
  }

  private void loadCommonHomepageDataAndSetRequestAttributes(
      final InfoLetterSessionController infoLetterSC, final HttpServletRequest request) {
    final InfoLetter defaultLetter = infoLetterSC.getInfoLetter();
    final String letterName = defaultStringIfNotDefined(defaultLetter.getName());
    final String letterDescription = defaultStringIfNotDefined(defaultLetter.getDescription());
    final String letterFrequency = defaultStringIfNotDefined(defaultLetter.getPeriode());
    final List<InfoLetterPublication> listParutions = infoLetterSC.getInfoLetterPublications();
    request.setAttribute("letterName", letterName);
    request.setAttribute("letterDescription", letterDescription);
    request.setAttribute("letterFrequence", letterFrequency);
    request.setAttribute("listParutions", listParutions);
    request.setAttribute("userIsSuscriber", infoLetterSC.isSubscriber());
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param infoLetterSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, InfoLetterSessionController infoLetterSC,
      HttpRequest request) {
    String destination;
    // the flag is the best user's profile
    String flag = getFlag(infoLetterSC.getUserRoles());
    try {
      setGlobalInfo(infoLetterSC, request);
      if ((function.startsWith("Accueil")) || (function.startsWith("Main"))) {
        destination = setMainContext(infoLetterSC, request);
      } else if ("View".equals(function)) {
        String parution = defaultStringIfNotDefined(param(request, PARUTION), param(request, "Id"));
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          request.setAttribute(PARUTION, parution);
          request.setAttribute(PARUTION_TITLE, ilp.getTitle());
          destination = "viewLetter.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("portlet")) {
        loadCommonHomepageDataAndSetRequestAttributes(infoLetterSC, request);
        if (PUBLISHER.equals(flag) || ADMIN.equals(flag)) {
          destination = "portletListLetterAdmin.jsp?Profile=" + flag;
        } else {
          destination = "portletListLetterUser.jsp?Profile=" + flag;

        }
      } else if (function.startsWith("Preview")) {
        String parution = param(request, PARUTION);
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          request.setAttribute("entity", ilp);
          request.setAttribute(PARUTION, parution);
          request.setAttribute(PARUTION_TITLE, ilp.getTitle());
          destination = "previewLetter.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("EditTemplateContent")) {
        final InfoLetter infoLetter = infoLetterSC.getInfoLetter();
        final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(infoLetter.getTemplateIdentifier());
        if (!store.getFile().exists() && infoLetter.existsTemplateContent()) {
          // If the Drag And Drop editor has not been yet used, taking the WYSIWYG content if any.
          // It permits retrieving old content of template edited before the introduction
          // of the Drag And Drop WEB Editor.
          final String content = infoLetter.getTemplateWysiwygContent()
              .map(ContributionContent::getRenderer)
              .map(ContributionContentRenderer::renderEdition)
              .filter(StringUtil::isDefined)
              .orElse(EMPTY);
          store.getFile().getContainer().getOrCreateTmpContent().setValue(content);
          store.getFile().getContainer().getOrCreateContent().setValue(content);
          store.save();
          infoLetter.saveTemplateContent(null);
        }
        return WbeFileEdition.get()
            .initializeWith(request, new DragAndDropWbeFile(store),
                withConnectors("SaveTemplateContent", "Main")
                    .addBrowseBarElement(new BrowseBarElement(infoLetterSC.getString("infoLetter.template"), ""))
                    .build()::applyTo)
            .orElse(null);
      } else if (function.startsWith("SaveTemplateContent")) {
        final String manualContent = param(request, "editor");
        final InfoLetter infoLetter = infoLetterSC.getInfoLetter();
        infoLetter.saveTemplateContent(manualContent);
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("EditContent")) {
        String parution = param(request, PARUTION);
        if (StringUtil.isDefined(parution)) {
          destination = null;
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          if (!request.getParameterAsBoolean("old")) {
            final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(ilp.getIdentifier());
            final boolean firstEdition = !store.getFile().exists();
            final boolean resetWithTemplate = request.getParameterAsBoolean("resetWithTemplate");
            if (firstEdition || resetWithTemplate) {
              // If the Drag And Drop editor has not been yet used, taking the WYSIWYG content if any.
              // It permits retrieving old contents of newsletter edited before the introduction
              // of the Drag And Drop WEB Editor.
              final String content = Optional.of(firstEdition)
                  .filter(Boolean.TRUE::equals)
                  .flatMap(b -> ilp.getWysiwygContent())
                  .map(ContributionContent::getRenderer)
                  .map(ContributionContentRenderer::renderEdition)
                  .filter(StringUtil::isDefined)
                  .orElseGet(() -> {
                    // If no content exists for a newsletter, then initializing it from the template
                    final InfoLetter infoLetter = infoLetterSC.getInfoLetter();
                    return Optional.of(new DragAndDropWebEditorStore(infoLetter.getTemplateIdentifier()))
                        .map(DragAndDropWebEditorStore::getFile)
                        .filter(DragAndDropWebEditorStore.File::exists)
                        .map(DragAndDropWebEditorStore.File::getContainer)
                        .flatMap(DragAndDropWebEditorStore.Container::getContent)
                        .map(DragAndDropWebEditorStore.Content::getValue)
                        // If no store exists for template, then taking the template from WYSIWYG context if any
                        .orElseGet(() -> infoLetter.getTemplateWysiwygContent()
                            .map(ContributionContent::getRenderer)
                            .map(ContributionContentRenderer::renderEdition)
                            .filter(StringUtil::isDefined)
                            .orElse(EMPTY));
                  });
              store.getFile().getContainer().getOrCreateTmpContent().setValue(content);
              store.getFile().getContainer().getOrCreateContent().setValue(content);
              store.save();
              ilp.saveContent(null);
            }
            final String parutionParam = "?parution=" + parution;
            final String validateUrl = "SaveContent" + parutionParam;
            final String cancelUrl = "Preview" + parutionParam;
            destination = WbeFileEdition.get()
                .initializeWith(request, new DragAndDropWbeFile(store),
                    withConnectors(validateUrl, cancelUrl).build()::applyTo)
                .orElse(null);
          }
          if (isNotDefined(destination)) {
            request.setAttribute(PARUTION, parution);
            request.setAttribute(PARUTION_TITLE, ilp.getTitle());
            request.setAttribute("parutionContent", ilp.getWysiwygContent()
                .map(ContributionContent::getRenderer)
                .map(ContributionContentRenderer::renderEdition)
                .orElse(EMPTY));
            destination = "editLetter.jsp";
          } else {
            return destination;
          }
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("ParutionHeaders")) {
        String parution = param(request, PARUTION);
        String title = "";
        String description = "";
        String browsBarPath = infoLetterSC.getString("infoLetter.newLetterHeader");
        if (!"".equals(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          title = ilp.getTitle();
          description = ilp.getDescription();
          browsBarPath = title;
        }
        request.setAttribute(PARUTION, parution);
        request.setAttribute(TITLE, title);
        request.setAttribute(DESCRIPTION, description);
        request.setAttribute(BROWSE_BAR_PATH, browsBarPath);
        destination = HEADER_LETTER_JSP;
      } else if (function.startsWith("UpdateTemplateFromHeaders")) {
        String parution = param(request, PARUTION);
        String title = "";
        String description = "";
        String browsBarPath = infoLetterSC.getString("infoLetter.newLetterHeader");
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          title = ilp.getTitle();
          description = ilp.getDescription();
          browsBarPath = title;
          // Mise a jour du template
          infoLetterSC.updateTemplate(ilp);
        }
        request.setAttribute(PARUTION, parution);
        request.setAttribute(TITLE, title);
        request.setAttribute(DESCRIPTION, description);
        request.setAttribute(BROWSE_BAR_PATH, browsBarPath);
        destination = HEADER_LETTER_JSP;
      } else if (function.startsWith("ValidateParution")) {
        String parution = param(request, PARUTION);
        String[] emailErrors = new String[0];
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          ilp.setPublicationState(InfoLetterPublication.PUBLICATION_VALIDEE);
          ilp.setParutionDate(DateUtil.date2SQLDate(new java.util.Date()));
          infoLetterSC.updateInfoLetterPublication(ilp);
          String server = request.getRequestURL().substring(0,
              request.getRequestURL().toString().indexOf(URLUtil.getApplicationURL()));
          infoLetterSC.notifyInternalSubscribers(ilp, server);
          emailErrors = infoLetterSC.sendByMailToExternalSubscribers(ilp, server);
        }
        request.setAttribute("EmailErrors", emailErrors);
        destination = "infoLetterSended.jsp";
      } else if (function.startsWith("ChangeParutionHeaders")) {
        String parution = param(request, PARUTION);
        String title = param(request, TITLE);
        String description = param(request, DESCRIPTION);
        List<InfoLetter> listLettres = infoLetterSC.getInfoLetters();
        InfoLetter defaultLetter = listLettres.get(0);

        if (parution.isEmpty()) {
          InfoLetterPublicationPdC ilp = new InfoLetterPublicationPdC();
          ilp.setInstanceId(infoLetterSC.getComponentId());
          ilp.setTitle(title);
          ilp.setDescription(description);
          ilp.setPublicationState(InfoLetterPublication.PUBLICATION_EN_REDACTION);
          ilp.setLetterId(defaultLetter.getPK().getId());
          // Classify content
          String positions = request.getParameter("Positions");
          ilp.setPositions(positions);
          infoLetterSC.createInfoLetterPublication(ilp);
          parution = ilp.getPK().getId();
        } else {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          ilp.setTitle(title);
          ilp.setDescription(description);
          infoLetterSC.updateInfoLetterPublication(ilp);
        }
        request.setAttribute(PARUTION, parution);
        request.setAttribute(TITLE, title);
        request.setAttribute(DESCRIPTION, description);
        request.setAttribute(BROWSE_BAR_PATH, title);
        destination = HEADER_LETTER_JSP;
      } else if (function.equals("DeletePublication")) {
        final String id = request.getParameter("id");
        if (isDefined(id)) {
          infoLetterSC.deleteInfoLetterPublication(id);
        }
        destination = setMainContext(infoLetterSC, request);
      } else if (function.equals("DeletePublications")) {
        request.getParameterAsList("selectedIds")
            .forEach(infoLetterSC::deleteInfoLetterPublication);
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("LetterHeaders")) {
        InfoLetter defaultLetter = infoLetterSC.getInfoLetter();
        String letterName = defaultStringIfNotDefined(defaultLetter.getName());
        String letterDescription = defaultStringIfNotDefined(defaultLetter.getDescription());
        String letterFrequence = defaultStringIfNotDefined(defaultLetter.getPeriode());
        request.setAttribute("letterName", letterName);
        request.setAttribute("letterDescription", letterDescription);
        request.setAttribute("letterFrequence", letterFrequence);
        destination = "modifHeaders.jsp";
      } else if (function.startsWith("ChangeLetterHeaders")) {
        InfoLetter defaultLetter = infoLetterSC.getInfoLetter();
        String letterName = param(request, "name");
        String letterDescription = param(request, DESCRIPTION);
        String letterFrequence = param(request, "frequence");
        defaultLetter.setName(letterName);
        defaultLetter.setDescription(letterDescription);
        defaultLetter.setPeriode(letterFrequence);
        infoLetterSC.updateInfoLetter(defaultLetter);
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("Emails")) {
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSubscribers();
        request.setAttribute(LIST_EMAILS, listEmails);
        destination = EMAILS_MANAGER_JSP;
      } else if (function.startsWith("SuscribeMe")) {
        infoLetterSC.subscribeUser();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("UnsuscribeMe")) {
        infoLetterSC.unsubscribeUser();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("DeleteEmails")) {
        String[] emails = request.getParameterValues("mails");
        if (emails != null) {
          infoLetterSC.deleteExternalsSubscribers(emails);
        }
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSubscribers();
        request.setAttribute(LIST_EMAILS, listEmails);
        destination = EMAILS_MANAGER_JSP;
      } else if (function.startsWith("DeleteAllEmails")) {
        infoLetterSC.deleteAllExternalsSubscribers();
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSubscribers();
        request.setAttribute(LIST_EMAILS, listEmails);
        destination = EMAILS_MANAGER_JSP;
      } else if (function.startsWith("AddMail")) {
        destination = "addEmail.jsp";
      } else if (function.startsWith("NewMail")) {
        String newmails = param(request, "newmails");
        infoLetterSC.addExternalsSubscribers(newmails);
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSubscribers();
        request.setAttribute(LIST_EMAILS, listEmails);
        destination = EMAILS_MANAGER_JSP;
      } else if (function.startsWith("Suscribers")) {
        destination = infoLetterSC.initUserPanel();
      } else if (function.startsWith("RetourPanel")) {
        infoLetterSC.retourUserPanel();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.equals("ViewInlinedCssHtml")) {
        final String id = request.getParameter("id");
        final InfoLetterPublicationPdC pub = infoLetterSC.getInfoLetterPublication(id);
        request.setAttribute(INLINED_CSS_HTML, pub.getWysiwygContent()
            .map(ContributionContent::getRenderer)
            .map(r -> r.renderView(true))
            .orElse(EMPTY));
        destination = "inlinedCssHtml.jsp";
      } else if (function.equals("ViewTemplate")) {
        final InfoLetter currentLetter = infoLetterSC.getInfoLetter();
        request.setAttribute(INLINED_CSS_HTML, currentLetter.getTemplateWysiwygContent()
            .map(ContributionContent::getRenderer)
            .map(r -> r.renderView(true))
            .orElse(EMPTY));
        destination = "inlinedCssHtml.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = defaultStringIfNotDefined(request.getParameter("Id"));
        String type = request.getParameter("Type");
        if (InfoLetter.TYPE.equals(type)) {
          destination = setMainContext(infoLetterSC, request);
        } else {
          if (!id.isEmpty()) {
            return getDestination("View", infoLetterSC, request);
          } else {
            destination = setMainContext(infoLetterSC, request);
          }
        }
      } else if (function.startsWith("ImportEmailsCsv")) {
        FileItem fileItem = request.getSingleFile();
        infoLetterSC.importCsvEmails(fileItem);
        destination = "importEmailsCsv.jsp?Result=OK";
      } else if (function.equals("ExportEmailsCsv")) {
        boolean exportOk = infoLetterSC.exportCsvEmails();
        request.setAttribute("ExportOk", Boolean.toString(exportOk));
        if (exportOk) {
          request.setAttribute("EmailCsvName",
              infoLetterSC.getComponentId() + InfoLetterSessionController.EXPORT_CSV_NAME);
        }
        destination = "exportEmailsCsv.jsp";
      } else if (function.startsWith("SendLetterToManager")) {
        String parution = param(request, PARUTION);
        String[] emailErrors = new String[0];
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          String server = request.getRequestURL().substring(0,
              request.getRequestURL().toString().indexOf(URLUtil.getApplicationURL()));
          emailErrors = infoLetterSC.notifyManagers(ilp, server);
        }
        request.setAttribute("EmailErrors", emailErrors);
        final String returnUrl = defaultStringIfNotDefined(request.getParameter("ReturnUrl"), "Preview");
        request.setAttribute("ReturnUrl", returnUrl + "?parution=" + parution);
        destination = "infoLetterSended.jsp";
      } else if (function.startsWith("SaveContent")) {
        String parution = param(request, PARUTION);
        String manualContent = param(request, "editor");
        if (StringUtil.isDefined(parution)) {
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(parution);
          ilp.saveContent(manualContent);
          return getDestination("Preview", infoLetterSC, request);
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else {
        destination = function;
      }
      if (!function.startsWith("Suscribers")) {
        destination = "/infoLetter/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String getFlag(String[] profiles) {
    String flag = "user";
    for (final String profile : profiles) {
      // if publisher, return it, we won't find a better profile
      if (profile.equals(ADMIN)) {
        return profile;
      } else if (!profile.equals("user")) {
        flag = profile;
      }
    }
    return flag;
  }

  private String param(HttpServletRequest request, String name) {
    return defaultStringIfNotDefined(request.getParameter(name));
  }
}
