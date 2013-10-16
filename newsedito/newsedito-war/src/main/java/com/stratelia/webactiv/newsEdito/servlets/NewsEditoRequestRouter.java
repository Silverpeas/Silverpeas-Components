/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.newsEdito.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.newsEdito.NewsEditoException;
import com.stratelia.webactiv.newsEdito.control.NewsEditoSessionController;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationSelection;
import org.apache.commons.fileupload.FileItem;

import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class NewsEditoRequestRouter extends ComponentRequestRouter<NewsEditoSessionController> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method creates a NewsEditoSessionController instance
   *
   * @param mainSessionCtrl The MainSessionController instance
   * @param context         Context of current component instance
   * @return a NewsEditoSessionController instance
   */
  @Override
  public NewsEditoSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new NewsEditoSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "newsEdito";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function  The entering request function (ex : "Main.jsp")
   * @param newsEdito The component Session Controller, build and initialised.
   * @param request   The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, NewsEditoSessionController newsEdito,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/newsEdito/jsp/";
    SilverTrace.debug("NewsEdito", "NewsEditoRequestRooter.getDestination",
        "NewsEdito.EX_ENTER", "function = " + function);

    if (function.startsWith("portlet")) {
      destination = rootDest + "portlet.jsp";
    } else if ((function.startsWith("Main")) || (function.startsWith("newsEdito"))) {
      // the flag is the best user's profile
      String flag = newsEdito.getUserRoleLevel();

      // initialisation de la variable indiquant que l'utilisateur est en mode
      // consultation
      newsEdito.setIsConsulting(true);
      destination = rootDest + "newsEdito.jsp?flag=" + flag;
    } else if (function.startsWith("manageNews")) {
      String flag = newsEdito.getUserRoleLevel();

      destination = rootDest + "manageNews.jsp?flag=" + flag;
    } else if (function.startsWith("manageArticles")) {

      String flag = newsEdito.getUserRoleLevel();
      // initialisation de la variable indiquant que l'utilisateur est en mode
      // manage
      newsEdito.setIsConsulting(false);
      destination = rootDest + "manageArticles.jsp?flag=" + flag;
    } else if (function.startsWith("publicationEdit")) {
      String flag = newsEdito.getUserRoleLevel();

      // initialisation de isConsulting inutile
      destination = rootDest + "publicationEdit.jsp?flag=" + flag;
    } else if (function.equals("UpdatePublication")) {
      String flag = newsEdito.getUserRoleLevel();

      try {
        // pour le formulaire XML
        setXMLForm(request, newsEdito, null);
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }
      destination = rootDest + "publicationEdit.jsp?flag=" + flag;
    } else if (function.startsWith("publication")) {
      String flag = newsEdito.getUserRoleLevel();
      PublicationDetail pubDetail;

      try {
        // récupérer l'id
        String pubId = request.getParameter("PublicationId");
        if (StringUtil.isDefined(pubId)) {
          pubDetail = newsEdito.getPublicationDetail(pubId);
        } else {
          pubDetail = newsEdito.getCompletePublication().getPublicationDetail();
        }
        // pour le formulaire XML
        putXMLDisplayerIntoRequest(pubDetail, newsEdito, request);
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }

      destination = rootDest + "publication.jsp?flag=" + flag;
    } else if (function.startsWith("pdfCompile")) {
      String flag = newsEdito.getUserRoleLevel();

      // initialisation de isConsulting inutile
      destination = rootDest + "pdfCompile.jsp?flag=" + flag;
    } else if (function.startsWith("publishNews")) {
      String flag = newsEdito.getUserRoleLevel();

      // initialisation de isConsulting inutile
      destination = rootDest + "publishNews.jsp?flag=" + flag;
    } else if (function.equals("ListModels")) {
      try {
        List<PublicationTemplate> listModels =
            getPublicationTemplateManager().getPublicationTemplates();
        request.setAttribute("ListModels", listModels);
      } catch (PublicationTemplateException e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }
      destination = rootDest + "listModels.jsp";
    } else if (function.equals("SelectModel")) {
      try {
        String xmlFormName = request.getParameter("Name");
        setXMLForm(request, newsEdito, xmlFormName);

        // put current publication
        request.setAttribute("CurrentPublicationDetail", newsEdito
            .getCompletePublication().getPublicationDetail());
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }
      destination = rootDest + "model.jsp";
    } else if (function.equals("ReallyUpdatePublication")) {
      try {
        // mise à jour de l'entête de la publication
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String name = FileUploadUtil.getParameter(items, "Name");
        String description = FileUploadUtil.getParameter(items, "Description");
        newsEdito.updatePublication(name, description);

        // mise à jour du formulaire
        updateXmlForm(items, newsEdito);
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }
      destination = getDestination("publication", newsEdito, request);
    } else if (function.equals("UpdateXMLForm")) {
      try {
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        updateXmlForm(items, newsEdito);
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_LIST_ERROR");
      }

      destination = getDestination("publication", newsEdito, request);

    } else if (function.startsWith("searchResult")) {
      String flag = newsEdito.getUserRoleLevel();
      String id = request.getParameter("Id");
      String type = request.getParameter("Type");
      if (type.equals("Publication")) {
        // newsEdito.initNavigationForPublication(id);
        destination = rootDest
            + "newsEdito.jsp?Action=SelectPublication&PublicationId=" + id
            + "&flag=" + flag;
      } else // if (type.equals("Node"))
      {
        try {
          newsEdito.initNavigationForNode(id);
        } catch (NewsEditoException e) {
          throw new EJBException(e);
        }
        if (newsEdito.getTitleId() != null) {
          destination = rootDest + "newsEdito.jsp?Action=SelectTitle&TitleId="
              + id + "&flag=" + flag;
        } else {
          destination = rootDest
              + "newsEdito.jsp?Action=SelectArchive&ArchiveId=" + id + "&flag="
              + flag;
        }
      }
    }
    // clipboard
    /*
     * ------ COMMENTED BY LBE, WAITING TO BE RESOLVED ------
     */
    else if (function.startsWith("multicopy")) {
      try {
        String Ids[] = request.getParameterValues("publicationIds");

        for (int i = 0; i < Ids.length; i++) {
          if (Ids[i] != null) {
            CompletePublication pub = ((NewsEditoSessionController) newsEdito)
                .getCompletePublication(Ids[i]);
            PublicationSelection pubSelect = new PublicationSelection(pub);
            newsEdito.addClipboardSelection((ClipboardSelection) pubSelect);
          }
        }
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_COPY_ERROR");
      }

      destination = rootDest + URLManager.getURL(URLManager.CMP_CLIPBOARD)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
      return destination;
    } else if (function.startsWith("paste")) {
      try {
        NewsEditoSessionController news = (NewsEditoSessionController) newsEdito;
        String titleId = news.getTitleId();
        Collection<ClipboardSelection> clipObjects = news.getClipboardSelectedObjects();
        Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();

        while (clipObjectIterator.hasNext()) {
          ClipboardSelection clipObject = clipObjectIterator
              .next();
          if (clipObject != null) {
            if (clipObject
                .isDataFlavorSupported(PublicationSelection.CompletePublicationFlavor)) {
              CompletePublication pub;

              pub = (CompletePublication) clipObject
                  .getTransferData(PublicationSelection.CompletePublicationFlavor);
              news.createPublication(pub.getPublicationDetail().getName(), pub
                  .getPublicationDetail().getDescription());
            } else if (clipObject
                .isDataFlavorSupported(PublicationSelection.PublicationDetailFlavor)) {
              PublicationDetail pub;

              pub = (PublicationDetail) clipObject
                  .getTransferData(PublicationSelection.PublicationDetailFlavor);
              news.createPublication(pub.getName(), pub.getDescription());
            }
          }
        }
        news.setTitleId(titleId);
        news.clipboardPasteDone();
      } catch (Exception e) {
        SilverTrace.warn("NewsEdito", "NewsEditoRequestRooter.getDestination",
            "NewsEdito.EX_PAST_ERROR");
      }
      destination = URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp";
      return rootDest + destination;
    }

    /*------ COMMENTED BY LBE, WAITING TO BE RESOLVED ------
     */
    else {
      destination = rootDest + function;
    }
    SilverTrace.info("newsEdito", "NewsEditoRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);

    return destination;
  }

  private void updateXmlForm(List<FileItem> items, NewsEditoSessionController newsEdito)
      throws NewsEditoException, RemoteException,
      PublicationTemplateException, FormException {
    PublicationDetail pubDetail = newsEdito.getCompletePublication().getPublicationDetail();

    String xmlFormShortName = null;

    // Is it the creation of the content or an update ?
    String infoId = pubDetail.getInfoId();
    if (infoId == null || "0".equals(infoId)) {
      String xmlFormName = FileUploadUtil.getParameter(items, "XmlFormName");

      // The publication have no content
      // We have to register xmlForm to publication
      xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      pubDetail.setInfoId(xmlFormShortName);
      newsEdito.updatePublication(pubDetail);
    } else {
      xmlFormShortName = pubDetail.getInfoId();
    }

    String pubId = pubDetail.getPK().getId();

    PublicationTemplate pub = getPublicationTemplateManager()
        .getPublicationTemplate(newsEdito.getComponentId() + ":"
            + xmlFormShortName);

    RecordSet set = pub.getRecordSet();
    Form form = pub.getUpdateForm();

    String language = newsEdito.getLanguage();

    DataRecord data = set.getRecord(pubId, language);
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(pubId);
      data.setLanguage(language);
    }

    PagesContext context =
        new PagesContext("myForm", "3", newsEdito.getLanguage(), false, newsEdito.getComponentId(),
            newsEdito.getUserId());
    context.setObjectId(pubId);

    form.update(items, data, context);
    set.save(data);
  }

  private void setXMLForm(HttpServletRequest request, NewsEditoSessionController newsEdito, String xmlFormName)
      throws PublicationTemplateException, FormException, NewsEditoException {
    PublicationDetail pubDetail = newsEdito.getCompletePublication().getPublicationDetail();
    String pubId = pubDetail.getPK().getId();

    String xmlFormShortName = null;
    if (!StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName = pubDetail.getInfoId();
      xmlFormName = null;
    } else {
      xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
          xmlFormName.indexOf("."));

      // register xmlForm to publication
      getPublicationTemplateManager().addDynamicPublicationTemplate(newsEdito
          .getComponentId()
          + ":" + xmlFormShortName, xmlFormName);
    }

    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
        .getPublicationTemplate(newsEdito.getComponentId() + ":"
            + xmlFormShortName, xmlFormName);
    Form formUpdate = pubTemplate.getUpdateForm();
    RecordSet recordSet = pubTemplate.getRecordSet();

    // get displayed language
    String language = newsEdito.getLanguage();

    DataRecord data = recordSet.getRecord(pubId, language);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(pubId);
    }

    request.setAttribute("Form", formUpdate);
    request.setAttribute("Data", data);
    request.setAttribute("XMLFormName", xmlFormName);
  }

  private void putXMLDisplayerIntoRequest(PublicationDetail pubDetail,
      NewsEditoSessionController newsEdito, HttpServletRequest request)
      throws PublicationTemplateException, FormException {
    String infoId = pubDetail.getInfoId();
    String pubId = pubDetail.getPK().getId();
    if (StringUtil.isDefined(infoId)) {
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager()
              .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":"
                  + infoId);

      // RecordTemplate recordTemplate = pubTemplate.getRecordTemplate();
      Form formView = pubTemplate.getViewForm();

      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(pubId, newsEdito.getLanguage());
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(pubId);
      }

      request.setAttribute("XMLForm", formView);
      request.setAttribute("XMLData", data);
    }
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}