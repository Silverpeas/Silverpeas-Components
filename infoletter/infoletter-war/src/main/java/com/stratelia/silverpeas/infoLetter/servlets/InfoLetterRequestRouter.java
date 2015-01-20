/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.stratelia.silverpeas.infoLetter.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.DateUtil;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.servlet.HttpRequest;

/**
 * Class declaration
 *
 * @author
 */
public class InfoLetterRequestRouter extends ComponentRequestRouter<InfoLetterSessionController> {

  private static final long serialVersionUID = 5722456216811272025L;

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
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
   * Method declaration
   *
   * @param infoLetterSC
   * @param request
   * @see
   */
  private void setGlobalInfo(InfoLetterSessionController infoLetterSC, HttpServletRequest request) {
    String language = infoLetterSC.getLanguage();
    // the flag is the best user's profile
    String flag = getFlag(infoLetterSC.getUserRoles());
    if (flag.equals("admin")) {
      request.setAttribute("userIsAdmin", "true");
    } else {
      request.setAttribute("userIsAdmin", "false");
    }
    if (infoLetterSC.isPdcUsed()) {
      request.setAttribute("isPdcUsed", "yes");
    } else {
      request.setAttribute("isPdcUsed", "no");
    }
    request.setAttribute("language", language);
    request.setAttribute("browseContext", new String[]{infoLetterSC.getSpaceLabel(),
      infoLetterSC.getComponentLabel(), infoLetterSC.getSpaceId(), infoLetterSC.getComponentId(),
      infoLetterSC.getComponentUrl()});
  }

  /**
   * Method declaration
   *
   * @param infoLetterSC
   * @return
   * @see
   */
  private InfoLetter getCurrentLetter(InfoLetterSessionController infoLetterSC) {
    List<InfoLetter> listLettres = infoLetterSC.getInfoLetters();
    return listLettres.get(0);
  }

  /**
   * Method declaration
   *
   * @param infoLetterSC
   * @param request
   * @return
   * @see
   */
  private String setMainContext(InfoLetterSessionController infoLetterSC, HttpServletRequest request) {
    String destination = "listLetterUser.jsp";
    // the flag is the best user's profile
    String flag = getFlag(infoLetterSC.getUserRoles());
    InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);
    List<InfoLetterPublication> listParutions = infoLetterSC.getInfoLetterPublications(defaultLetter
        .getPK());
    String letterName = defaultLetter.getName();
    boolean showHeader = true;

    if (letterName == null) {
      letterName = "";
    }
    String letterDescription = defaultLetter.getDescription();
    if (letterDescription == null) {
      letterDescription = "";
    }
    String letterFrequence = defaultLetter.getPeriode();
    if (letterFrequence == null) {
      letterFrequence = "";
    }
    if (StringUtil.isDefined(infoLetterSC.getSettings().getString("showHeader")) && StringUtil
        .getBooleanValue(infoLetterSC.getSettings().getString("showHeader"))) {
      showHeader = false;
    }
    request.setAttribute("letterName", letterName);
    request.setAttribute("letterDescription", letterDescription);
    request.setAttribute("letterFrequence", letterFrequence);
    request.setAttribute("listParutions", listParutions);
    request.setAttribute("showHeader", showHeader);
    request.setAttribute("userIsSuscriber", String.valueOf(infoLetterSC.isSuscriber()));
    boolean isTemplateExist = !listParutions.isEmpty();
    if (!listParutions.isEmpty()) {
      InfoLetterPublication pub = listParutions.get(0);
      IdPK publiPK = new IdPK(pub.getPK().getId());
      InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
      isTemplateExist = infoLetterSC.isTemplateExist(ilp);
    }
    request.setAttribute("IsTemplateExist", isTemplateExist);
    if ("publisher".equals(flag) || "admin".equals(flag)) {
      destination = "listLetterAdmin.jsp";
    }
    return destination;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param infoLetterSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function,
      InfoLetterSessionController infoLetterSC, HttpRequest request) {
    String destination;

    SilverTrace.info("infoLetter", "infoLetterRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + infoLetterSC.getUserId()
        + " Function=" + function);

    // the flag is the best user's profile
    String flag = getFlag(infoLetterSC.getUserRoles());

    try {
      setGlobalInfo(infoLetterSC, request);

      if ((function.startsWith("Accueil")) || (function.startsWith("Main"))) {
        destination = setMainContext(infoLetterSC, request);
      } else if ("View".equals(function)) {
        String parution = param(request, "parution");
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());
          destination = "viewLetter.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("portlet")) {
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);
        List<InfoLetterPublication> listParutions = infoLetterSC.getInfoLetterPublications(
            defaultLetter.getPK());
        String letterName = defaultLetter.getName();
        if (letterName == null) {
          letterName = "";
        }
        String letterDescription = defaultLetter.getDescription();

        if (letterDescription == null) {
          letterDescription = "";
        }
        String letterFrequence = defaultLetter.getPeriode();
        if (letterFrequence == null) {
          letterFrequence = "";
        }
        request.setAttribute("letterName", letterName);
        request.setAttribute("letterDescription", letterDescription);
        request.setAttribute("letterFrequence", letterFrequence);
        request.setAttribute("listParutions", listParutions);
        request.setAttribute("userIsSuscriber", String.valueOf(infoLetterSC.isSuscriber()));

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "portletListLetterAdmin.jsp?Profile=" + flag;
        } else {
          destination = "portletListLetterUser.jsp?Profile=" + flag;

        }
      } else if (function.startsWith("Preview")) {
        String parution = param(request, "parution");
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());
          destination = "previewLetter.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("FilesEdit")) {
        String parution = param(request, "parution");
        if (parution.length() <= 0) {
          parution = param(request, "Id");
        }
        if (StringUtil.isDefined(parution)) {
          String url = "/RinfoLetter/" + infoLetterSC.getComponentId() + "/FilesEdit";

          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          request.setAttribute("parution", parution);
          request.setAttribute("url", url);
          request.setAttribute("parutionTitle", ilp.getTitle());
          destination = "attachedFiles.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("EditContent")) {
        String parution = param(request, "parution");
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());
          request.setAttribute("parutionContent", ilp._getContent());
          destination = "editLetter.jsp";
        } else {
          destination = setMainContext(infoLetterSC, request);
        }
      } else if (function.startsWith("ParutionHeaders")) {
        String parution = param(request, "parution");
        String title = "";
        String description = "";
        String browsBarPath = infoLetterSC.getString("infoLetter.newLetterHeader");

        if (!"".equals(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
          title = ilp.getTitle();
          description = ilp.getDescription();
          browsBarPath = title;
        }
        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);
        request.setAttribute("browseBarPath", browsBarPath);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("UpdateTemplateFromHeaders")) {
        String parution = param(request, "parution");
        String title = "";
        String description = "";
        String browsBarPath = infoLetterSC.getString("infoLetter.newLetterHeader");

        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          title = ilp.getTitle();
          description = ilp.getDescription();
          browsBarPath = title;

          // Mise a jour du template
          infoLetterSC.updateTemplate(ilp);
        }
        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);
        request.setAttribute("browseBarPath", browsBarPath);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("ValidateParution")) {
        String parution = param(request, "parution");
        String[] emailErrors = new String[0];
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          ilp.setPublicationState(InfoLetterPublication.PUBLICATION_VALIDEE);
          ilp.setParutionDate(DateUtil.date2SQLDate(new java.util.Date()));
          infoLetterSC.updateInfoLetterPublication(ilp);
          infoLetterSC.createIndex(ilp);
          String server = request.getRequestURL().substring(0,
              request.getRequestURL().toString().indexOf(URLManager.getApplicationURL()));
          infoLetterSC.notifyInternalSuscribers(ilp, server);

          emailErrors = infoLetterSC.sendByMailToExternalSubscribers(ilp, server);
        }
        request.setAttribute("EmailErrors", emailErrors);
        destination = "infoLetterSended.jsp";
      } else if (function.startsWith("ChangeParutionHeaders")) {
        String parution = param(request, "parution");
        String title = param(request, "title");
        String description = param(request, "description");
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
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          ilp.setTitle(title);
          ilp.setDescription(description);
          infoLetterSC.updateInfoLetterPublication(ilp);
        }

        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);
        request.setAttribute("browseBarPath", title);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("DeletePublications")) {
        String[] publis = request.getParameterValues("publis");
        if (publis != null) {
          for (final String publi : publis) {
            IdPK publiPK = new IdPK();
            publiPK.setId(publi);
            infoLetterSC.deleteInfoLetterPublication(publiPK);
          }
        }
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("LetterHeaders")) {
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);
        String letterName = defaultLetter.getName();

        if (letterName == null) {
          letterName = "";
        }
        String letterDescription = defaultLetter.getDescription();

        if (letterDescription == null) {
          letterDescription = "";
        }
        String letterFrequence = defaultLetter.getPeriode();

        if (letterFrequence == null) {
          letterFrequence = "";
        }
        request.setAttribute("letterName", letterName);
        request.setAttribute("letterDescription", letterDescription);
        request.setAttribute("letterFrequence", letterFrequence);
        destination = "modifHeaders.jsp";
      } else if (function.startsWith("ChangeLetterHeaders")) {
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);
        String letterName = param(request, "name");
        String letterDescription = param(request, "description");
        String letterFrequence = param(request, "frequence");

        defaultLetter.setName(letterName);
        defaultLetter.setDescription(letterDescription);
        defaultLetter.setPeriode(letterFrequence);
        infoLetterSC.updateInfoLetter(defaultLetter);

        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("Emails")) {
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("SuscribeMe")) {
        infoLetterSC.suscribeUser();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("UnsuscribeMe")) {
        infoLetterSC.unsuscribeUser();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.startsWith("DeleteEmails")) {
        String[] emails = request.getParameterValues("mails");
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);

        if (emails != null) {
          infoLetterSC.deleteExternalsSuscribers(defaultLetter.getPK(), emails);
        }
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("DeleteAllEmails")) {
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);

        infoLetterSC.deleteAllExternalsSuscribers(defaultLetter.getPK());
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("AddMail")) {
        destination = "addEmail.jsp";
      } else if (function.startsWith("NewMail")) {
        String newmails = param(request, "newmails");
        InfoLetter defaultLetter = getCurrentLetter(infoLetterSC);

        infoLetterSC.addExternalsSuscribers(defaultLetter.getPK(), newmails);
        Set<String> listEmails = infoLetterSC.getEmailsExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("Suscribers")) {
        destination = infoLetterSC.initUserPanel();
      } else if (function.startsWith("RetourPanel")) {
        infoLetterSC.retourUserPanel();
        destination = setMainContext(infoLetterSC, request);
      } else if (function.equals("ViewTemplate")) {
        request.setAttribute("InfoLetter", getCurrentLetter(infoLetterSC));

        destination = "template.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if (type.equals("Lettre")) {
          destination = setMainContext(infoLetterSC, request);
        } else {
          if (id == null) {
            id = "";
          }
          if (!id.isEmpty()) {
            IdPK publiPK = new IdPK();
            publiPK.setId(id);
            InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
            request.setAttribute("parution", id);
            request.setAttribute("parutionTitle", ilp.getTitle());
            destination = "viewLetter.jsp";
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
          request.setAttribute("EmailCsvName", infoLetterSC.getComponentId()
              + InfoLetterSessionController.EXPORT_CSV_NAME);
        }
        destination = "exportEmailsCsv.jsp";
      } else if (function.startsWith("SendLetterToManager")) {
        String parution = param(request, "parution");
        String[] emailErrors = new String[0];
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);

          String server = request.getRequestURL().substring(0, request.getRequestURL().toString()
              .indexOf(URLManager.getApplicationURL()));
          emailErrors = infoLetterSC.notifyManagers(ilp, server);
        }
        request.setAttribute("EmailErrors", emailErrors);
        request.setAttribute("ReturnUrl", request.getParameter("ReturnUrl") + "?parution="
            + parution);

        destination = "infoLetterSended.jsp";
      } else if (function.startsWith("SaveContent")) {
        String parution = param(request, "parution");
        String content = param(request, "Content");
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = infoLetterSC.getInfoLetterPublication(publiPK);
          infoLetterSC.updateContentInfoLetterPublication(content, ilp);
          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());

          destination = "previewLetter.jsp";
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

    SilverTrace.info("infoLetter", "infoLetterRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /* getFlag */
  /**
   * Method declaration
   *
   * @param profiles
   * @return
   * @see
   */
  private String getFlag(String[] profiles) {
    String flag = "user";

    for (final String profile : profiles) {
      // if publisher, return it, we won't find a better profile
      if (profile.equals("admin")) {
        return profile;
      } else if (!profile.equals("user")) {
        flag = profile;
      }
    }
    return flag;
  }

  /* recuperation de parametre non nul */
  /**
   * Method declaration
   *
   * @param request
   * @param name
   * @return
   * @see
   */
  private String param(HttpServletRequest request, String name) {
    String retour = request.getParameter(name);

    if (retour == null) {
      retour = "";
    }
    return retour;
  }
}
