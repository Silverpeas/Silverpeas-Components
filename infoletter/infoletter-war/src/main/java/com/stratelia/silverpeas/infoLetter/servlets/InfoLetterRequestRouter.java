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
package com.stratelia.silverpeas.infoLetter.servlets;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Class declaration
 * @author
 */
public class InfoLetterRequestRouter extends ComponentRequestRouter {

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new InfoLetterSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "infoLetter";
  }

  /**
   * Method declaration
   * @param scc
   * @param request
   * @see
   */
  private void setGlobalInfo(InfoLetterSessionController scc,
      HttpServletRequest request) {
    String language = scc.getLanguage();

    // the flag is the best user's profile
    String flag = getFlag(scc.getUserRoles());

    if (flag.equals("admin"))
      request.setAttribute("userIsAdmin", "true");
    else
      request.setAttribute("userIsAdmin", "false");

    if (scc.isPdcUsed())
      request.setAttribute("isPdcUsed", "yes");
    else
      request.setAttribute("isPdcUsed", "no");

    request.setAttribute("language", language);
    request.setAttribute("browseContext", new String[] { scc.getSpaceLabel(),
        scc.getComponentLabel(), scc.getSpaceId(), scc.getComponentId(),
        scc.getComponentUrl() });
  }

  /**
   * Method declaration
   * @param scc
   * @return
   * @see
   */
  private InfoLetter getCurrentLetter(InfoLetterSessionController scc) {
    Vector listLettres = scc.getInfoLetters();

    return (InfoLetter) listLettres.elementAt(0);
  }

  /**
   * Method declaration
   * @param scc
   * @param request
   * @return
   * @see
   */
  private String setMainContext(InfoLetterSessionController scc,
      HttpServletRequest request) {
    String destination = "listLetterUser.jsp";

    // the flag is the best user's profile
    String flag = getFlag(scc.getUserRoles());

    InfoLetter defaultLetter = getCurrentLetter(scc);
    Vector listParutions = scc.getInfoLetterPublications(defaultLetter.getPK());
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

    if ((scc.getSettings().getString("showHeader") != null)
        && (scc.getSettings().getString("showHeader").equals("false")))
      showHeader = false;

    request.setAttribute("letterName", letterName);
    request.setAttribute("letterDescription", letterDescription);
    request.setAttribute("letterFrequence", letterFrequence);
    request.setAttribute("listParutions", listParutions);
    request.setAttribute("showHeader", new Boolean(showHeader));
    request.setAttribute("userIsSuscriber", String.valueOf(scc
        .isSuscriber(defaultLetter.getPK())));
    InfoLetterPublication pub;
    String parution;
    IdPK publiPK;
    InfoLetterPublicationPdC ilp;
    boolean isTemplateExist;
    if (listParutions.isEmpty()) {
      ilp = null;
      isTemplateExist = false;
    } else {
      pub = (InfoLetterPublication) listParutions.elementAt(0);
      parution = pub.getPK().getId();
      publiPK = new IdPK();
      publiPK.setId(parution);
      ilp = scc.getInfoLetterPublication(publiPK);
      isTemplateExist = scc.isTemplateExist(ilp);
    }
    request.setAttribute("IsTemplateExist", new Boolean(isTemplateExist));
    if (flag.equals("publisher") || flag.equals("admin")) {
      destination = "listLetterAdmin.jsp";
    }
    return destination;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    InfoLetterSessionController infoLetterSC = (InfoLetterSessionController) componentSC;

    SilverTrace.info("infoLetter", "infoLetterRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + infoLetterSC.getUserId()
        + " Function=" + function);
    InfoLetterSessionController scc = (InfoLetterSessionController) componentSC;

    // the flag is the best user's profile
    String flag = getFlag(componentSC.getUserRoles());

    try {
      setGlobalInfo(scc, request);

      if ((function.startsWith("Accueil")) || (function.startsWith("Main"))) {
        destination = setMainContext(scc, request);
      } else if (function.equals("View")) {
        String parution = param(request, "parution");

        if (!parution.equals("")) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());
          destination = "viewLetter.jsp";
        } else {
          destination = setMainContext(scc, request);
        }
      } else if (function.startsWith("portlet")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);
        Vector listParutions = scc.getInfoLetterPublications(defaultLetter
            .getPK());
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
        request.setAttribute("userIsSuscriber", String.valueOf(scc
            .isSuscriber(defaultLetter.getPK())));

        // System.out.println("portlet flag="+flag);

        if ("publisher".equals(flag) || flag.equals("admin")) {
          destination = "portletListLetterAdmin.jsp?Profile=" + flag;
        } else {
          destination = "portletListLetterUser.jsp?Profile=" + flag;

        }
        // System.out.println("destination=="+destination);
      } else if (function.startsWith("Preview")) {
        String parution = param(request, "parution");

        if (!parution.equals("")) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);
          String m_context = GeneralPropertiesManager
              .getGeneralResourceLocator().getString("ApplicationURL");

          request.setAttribute("parution", parution);
          request.setAttribute("parutionTitle", ilp.getTitle());
          request.setAttribute("SpaceId", scc.getSpaceId());
          request.setAttribute("SpaceName", scc.getSpaceLabel());
          request.setAttribute("ComponentId", scc.getComponentId());
          request.setAttribute("ComponentName", scc.getComponentLabel());
          request.setAttribute("BrowseInfo", "Editeur de parution");
          request.setAttribute("ObjectId", parution);
          request.setAttribute("Language", scc.getLanguage());
          request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
              + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);
          destination = "previewLetter.jsp";
        } else {
          destination = setMainContext(scc, request);
        }
      } else if (function.startsWith("FilesEdit")) {
        String parution = param(request, "parution");

        if (parution.length() <= 0) {
          String theId = param(request, "Id");
          parution = theId;
        }
        if (!parution.equals("")) {
          String m_context = GeneralPropertiesManager
              .getGeneralResourceLocator().getString("ApplicationURL");
          String url = "/RinfoLetter/" + scc.getComponentId() + "/FilesEdit";

          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          request.setAttribute("parution", parution);
          request.setAttribute("url", url);
          request.setAttribute("parutionTitle", ilp.getTitle());
          request.setAttribute("SpaceId", scc.getSpaceId());
          request.setAttribute("SpaceName", scc.getSpaceLabel());
          request.setAttribute("ComponentId", scc.getComponentId());
          request.setAttribute("ComponentName", scc.getComponentLabel());
          request.setAttribute("BrowseInfo", "Editeur de parution");
          request.setAttribute("ObjectId", parution);
          request.setAttribute("Language", scc.getLanguage());
          // NEWD DLE
          // request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/" +
          // scc.getSpaceId() + "_" + scc.getComponentId() +
          // "/ParutionHeaders?parution=" + parution);
          request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
              + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);
          // NEWF DLE
          destination = "attachedFiles.jsp";
        } else {
          destination = setMainContext(scc, request);
        }
      } else if (function.startsWith("Edit")) {
        String parution = param(request, "parution");
        String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("ApplicationURL");

        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("BrowseInfo", "Editeur de parution");
        request.setAttribute("ObjectId", parution);
        request.setAttribute("Language", scc.getLanguage());
        request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
            + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);
        destination = "editLetter.jsp";
      } else if (function.startsWith("ParutionHeaders")) {
        String parution = param(request, "parution");
        String title = "";
        String description = "";

        if (!parution.equals("")) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          title = ilp.getTitle();
          description = ilp.getDescription();
        }
        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);

        String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("ApplicationURL");

        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("BrowseInfo", "Editeur de parution");
        request.setAttribute("ObjectId", parution);
        request.setAttribute("Language", scc.getLanguage());
        request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
            + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("UpdateTemplateFromHeaders")) {
        String parution = param(request, "parution");
        String title = "";
        String description = "";

        if (!parution.equals("")) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          title = ilp.getTitle();
          description = ilp.getDescription();

          // Mise a jour du template
          scc.updateTemplate(ilp);
        }
        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);

        String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("ApplicationURL");

        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("BrowseInfo", "Editeur de parution");
        request.setAttribute("ObjectId", parution);
        request.setAttribute("Language", scc.getLanguage());
        request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
            + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("ValidateParution")) {
        String parution = param(request, "parution");
        String[] emailErrors = new String[0];
        if (!parution.equals("")) {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          ilp.setPublicationState(InfoLetterPublication.PUBLICATION_VALIDEE);
          ilp.setParutionDate(DateUtil.date2SQLDate(new java.util.Date()));
          scc.updateInfoLetterPublication(ilp);
          scc.createIndex(ilp);
          scc.notifySuscribers(ilp);
          String server = request.getRequestURL().substring(
              0,
              request.getRequestURL().toString().indexOf(
              URLManager.getApplicationURL()));
          emailErrors = scc.notifyExternals(ilp, server);
        }
        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("EmailErrors", emailErrors);
        destination = "infoLetterSended.jsp";
      } else if (function.startsWith("ChangeParutionHeaders")) {
        String parution = param(request, "parution");
        String title = param(request, "title");
        String description = param(request, "description");
        Vector listLettres = scc.getInfoLetters();
        InfoLetter defaultLetter = (InfoLetter) listLettres.elementAt(0);

        if (parution.equals("")) {
          InfoLetterPublicationPdC ilp = new InfoLetterPublicationPdC();

          ilp.setTitle(title);
          ilp.setDescription(description);
          ilp
              .setPublicationState(InfoLetterPublication.PUBLICATION_EN_REDACTION);
          ilp.setLetterId(defaultLetter.getPK().getId());
          scc.createInfoLetterPublication(ilp);
          parution = ilp.getPK().getId();
        } else {
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          ilp.setTitle(title);
          ilp.setDescription(description);
          scc.updateInfoLetterPublication(ilp);
        }

        request.setAttribute("parution", parution);
        request.setAttribute("title", title);
        request.setAttribute("description", description);

        String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("ApplicationURL");

        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("BrowseInfo", "Editeur de parution");
        request.setAttribute("ObjectId", parution);
        request.setAttribute("Language", scc.getLanguage());
        request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
            + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);

        destination = "headerLetter.jsp";
      } else if (function.startsWith("DeletePublications")) {
        String[] publis = request.getParameterValues("publis");

        if (publis != null) {
          int i = 0;

          for (i = 0; i < publis.length; i++) {
            IdPK publiPK = new IdPK();

            publiPK.setId(publis[i]);
            scc.deleteInfoLetterPublication(publiPK);
          }
        }
        destination = setMainContext(scc, request);
      } else if (function.startsWith("LetterHeaders")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);
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
        InfoLetter defaultLetter = getCurrentLetter(scc);
        String letterName = param(request, "name");
        String letterDescription = param(request, "description");
        String letterFrequence = param(request, "frequence");

        defaultLetter.setName(letterName);
        defaultLetter.setDescription(letterDescription);
        defaultLetter.setPeriode(letterFrequence);
        scc.updateInfoLetter(defaultLetter);

        destination = setMainContext(scc, request);
      } else if (function.startsWith("Emails")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);
        Vector listEmails = scc.getExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("SuscribeMe")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);

        scc.suscribeUser(defaultLetter.getPK());

        destination = setMainContext(scc, request);
      } else if (function.startsWith("UnsuscribeMe")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);

        scc.unsuscribeUser(defaultLetter.getPK());

        destination = setMainContext(scc, request);
      } else if (function.startsWith("DeleteEmails")) {
        String[] emails = request.getParameterValues("mails");
        InfoLetter defaultLetter = getCurrentLetter(scc);

        if (emails != null) {
          scc.deleteExternalsSuscribers(defaultLetter.getPK(), emails);
        }
        Vector listEmails = scc.getExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("DeleteAllEmails")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);

        scc.deleteAllExternalsSuscribers(defaultLetter.getPK());
        Vector listEmails = scc.getExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("AddMail")) {
        destination = "addEmail.jsp";
      } else if (function.startsWith("NewMail")) {
        String newmails = param(request, "newmails");
        InfoLetter defaultLetter = getCurrentLetter(scc);

        scc.addExternalsSuscribers(defaultLetter.getPK(), newmails);
        Vector listEmails = scc.getExternalsSuscribers(defaultLetter.getPK());

        request.setAttribute("listEmails", listEmails);
        destination = "emailsManager.jsp";
      } else if (function.startsWith("Suscribers")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);

        destination = scc.initUserPanel(defaultLetter.getPK());
      } else if (function.startsWith("RetourPanel")) {
        InfoLetter defaultLetter = getCurrentLetter(scc);

        scc.retourUserPanel(defaultLetter.getPK());
        destination = setMainContext(scc, request);
      } else if (function.equals("ViewTemplate")) {
        request.setAttribute("InfoLetter", getCurrentLetter(scc));

        destination = "template.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if (type.equals("Lettre")) {
          destination = setMainContext(scc, request);
        } else {
          if (id == null) {
            id = "";
          }
          if (!id.equals("")) {
            IdPK publiPK = new IdPK();

            publiPK.setId(id);
            InfoLetterPublicationPdC ilp = scc
                .getInfoLetterPublication(publiPK);

            request.setAttribute("parution", id);
            request.setAttribute("parutionTitle", ilp.getTitle());
            destination = "viewLetter.jsp";
          } else {
            destination = setMainContext(scc, request);
          }
        }
      }
      // PdC classification
      else if (function.startsWith("pdcPositions.jsp")) {
        String parution = param(request, "parution");

        if (parution == null || parution.equals("")) {
          parution = request.getParameter("PubId");
        }
        if (parution != null && !parution.equals("")) {
          String m_context = GeneralPropertiesManager
              .getGeneralResourceLocator().getString("ApplicationURL");
          String url = "/RinfoLetter/" + scc.getComponentId()
              + "/FilesEdit?parution=" + parution;
          IdPK publiPK = new IdPK();

          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          request.setAttribute("parution", parution);
          request.setAttribute("url", java.net.URLEncoder.encode(url, "UTF-8"));
          request.setAttribute("parutionTitle", ilp.getTitle());
          request.setAttribute("SpaceId", scc.getSpaceId());
          request.setAttribute("SpaceName", scc.getSpaceLabel());
          request.setAttribute("ComponentId", scc.getComponentId());
          request.setAttribute("ComponentName", scc.getComponentLabel());
          request.setAttribute("BrowseInfo", "Editeur de parution");
          request.setAttribute("ObjectId", parution);
          request.setAttribute("Language", scc.getLanguage());
          request.setAttribute("ReturnUrl", m_context + "/RinfoLetter/"
              + scc.getComponentId() + "/ParutionHeaders?parution=" + parution);
        }
        request.setAttribute("silverObjectId", String.valueOf(scc
            .getCurrentSilverObjectId()));

        destination = "pdcPositions.jsp";
      } else if (function.startsWith("ImportEmailsCsv")) {
        FileItem fileItem = FileUploadUtil.getFile(request);

        infoLetterSC.importCsvEmails(fileItem);

        destination = "importEmailsCsv.jsp?Result=OK";
      } else if (function.startsWith("ExportEmailsCsv")) {
        boolean exportOk = infoLetterSC.exportCsvEmails();
        request.setAttribute("ExportOk", new Boolean(exportOk).toString());
        if (exportOk)
          request.setAttribute("EmailCsvName", getCurrentLetter(infoLetterSC)
              .getName()
              + InfoLetterSessionController.EXPORT_CSV_NAME);

        destination = "exportEmailsCsv.jsp";
      } else if (function.startsWith("SendLetterToManager")) {
        String parution = param(request, "parution");
        String[] emailErrors = new String[0];
        if (StringUtil.isDefined(parution)) {
          IdPK publiPK = new IdPK();
          publiPK.setId(parution);
          InfoLetterPublicationPdC ilp = scc.getInfoLetterPublication(publiPK);

          String server = request.getRequestURL().substring(
              0,
              request.getRequestURL().toString().indexOf(
              URLManager.getApplicationURL()));
          emailErrors = scc.notifyManagers(ilp, server);
        }
        request.setAttribute("SpaceId", scc.getSpaceId());
        request.setAttribute("SpaceName", scc.getSpaceLabel());
        request.setAttribute("ComponentId", scc.getComponentId());
        request.setAttribute("ComponentName", scc.getComponentLabel());
        request.setAttribute("EmailErrors", emailErrors);
        request.setAttribute("ReturnUrl", request.getParameter("ReturnUrl")
            + "?parution=" + parution);

        destination = "infoLetterSended.jsp";
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
   * @param profiles
   * @return
   * @see
   */
  private String getFlag(String[] profiles) {
    String flag = "user";

    for (int i = 0; i < profiles.length; i++) {
      // if publisher, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      } else if (!profiles[i].equals("user")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  /* recuperation de parametre non nul */

  /**
   * Method declaration
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
