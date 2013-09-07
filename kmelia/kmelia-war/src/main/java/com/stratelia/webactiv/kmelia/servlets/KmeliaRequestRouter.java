/*
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
package com.stratelia.webactiv.kmelia.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.kmelia.SearchContext;
import com.silverpeas.kmelia.updatechainhelpers.UpdateChainHelper;
import com.silverpeas.kmelia.updatechainhelpers.UpdateChainHelperContext;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.ThumbnailRuntimeException;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.FileFolder;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.updatechain.FieldUpdateChainDescriptor;
import com.stratelia.webactiv.kmelia.model.updatechain.Fields;
import com.stratelia.webactiv.kmelia.servlets.handlers.StatisticRequestHandler;
import com.stratelia.webactiv.util.ClientBrowserUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.importExport.versioning.DocumentVersion;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.xml.sax.SAXException;

public class KmeliaRequestRouter extends ComponentRequestRouter<KmeliaSessionController> {

  private static final long serialVersionUID = 1L;
  private static final StatisticRequestHandler statisticRequestHandler =
      new StatisticRequestHandler();

  /**
   * This method creates a KmeliaSessionController instance
   *
   * @param mainSessionCtrl The MainSessionController instance
   * @param context Context of current component instance
   * @return a KmeliaSessionController instance
   */
  @Override
  public KmeliaSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new KmeliaSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "kmelia";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function ( : "Main.jsp")
   * @param kmelia The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, KmeliaSessionController kmelia,
      HttpServletRequest request) {
    SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "function = " + function);
    String destination = "";
    String rootDestination = "/kmelia/jsp/";
    boolean profileError = false;
    boolean kmaxMode = false;
    boolean toolboxMode = false;
    try {
      SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "getComponentRootName() = " + kmelia.getComponentRootName());
      if ("kmax".equals(kmelia.getComponentRootName())) {
        kmaxMode = true;
        kmelia.isKmaxMode = true;
      }
      request.setAttribute("KmaxMode", kmaxMode);

      toolboxMode = KmeliaHelper.isToolbox(kmelia.getComponentId());

      // Set language choosen by the user
      setLanguage(request, kmelia);

      if (function.startsWith("Main")) {
        resetWizard(kmelia);
        if (kmaxMode) {
          destination = getDestination("KmaxMain", kmelia, request);
          kmelia.setSessionTopic(null);
          kmelia.setSessionPath("");
        } else {
          destination = getDestination("GoToTopic", kmelia, request);
        }
      } else if (function.startsWith("validateClassification")) {
        String[] publicationIds = request.getParameterValues("pubid");
        Collection<KmeliaPublication> publications = kmelia.getPublications(asPks(kmelia.
            getComponentId(), publicationIds));
        request.setAttribute("Context", URLManager.getApplicationURL());
        request.setAttribute("PublicationsDetails", publications);
        destination = rootDestination + "validateImportedFilesClassification.jsp";
      } else if (function.startsWith("portlet")) {
        kmelia.setSessionPublication(null);
        String flag = kmelia.getUserRoleLevel();
        if (kmaxMode) {
          destination = rootDestination + "kmax_portlet.jsp?Profile=" + flag;
        } else {
          destination = rootDestination + "portlet.jsp?Profile=user";
        }
      } else if (function.equals("FlushTrashCan")) {
        kmelia.flushTrashCan();
        if (kmaxMode) {
          destination = getDestination("KmaxMain", kmelia, request);
        } else {
          destination = getDestination("GoToCurrentTopic", kmelia, request);
        }
      } else if (function.equals("GoToDirectory")) {
        String topicId = request.getParameter("Id");

        String path = null;
        if (StringUtil.isDefined(topicId)) {
          NodeDetail topic = kmelia.getNodeHeader(topicId);
          path = topic.getPath();
        } else {
          path = request.getParameter("Path");
        }

        FileFolder folder = new FileFolder(path);
        request.setAttribute("Directory", folder);
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());

        destination = rootDestination + "repository.jsp";
      } else if ("GoToTopic".equals(function)) {
        String topicId = (String) request.getAttribute("Id");
        if (!StringUtil.isDefined(topicId)) {
          topicId = request.getParameter("Id");
          if (!StringUtil.isDefined(topicId)) {
            topicId = NodePK.ROOT_NODE_ID;
          }
        }
        kmelia.setCurrentFolderId(topicId, true);
        resetWizard(kmelia);
        request.setAttribute("CurrentFolderId", topicId);
        request.setAttribute("DisplayNBPublis", kmelia.displayNbPublis());
        request.setAttribute("DisplaySearch", kmelia.isSearchOnTopicsEnabled());

        // rechercher si le theme a un descripteur
        request.setAttribute("HaveDescriptor", kmelia.isTopicHaveUpdateChainDescriptor());

        request.setAttribute("Profile", kmelia.getUserTopicProfile(topicId));
        request.setAttribute("IsGuest", kmelia.getUserDetail().isAccessGuest());
        request.setAttribute("RightsOnTopicsEnabled", kmelia.isRightsOnTopicsEnabled());
        request.setAttribute("WysiwygDescription", kmelia.getWysiwygOnTopic());
        request.setAttribute("PageIndex", kmelia.getIndexOfFirstPubToDisplay());

        if (kmelia.isTreeviewUsed()) {
          destination = rootDestination + "treeview.jsp";
        } else if (kmelia.isTreeStructure()) {
          destination = rootDestination + "oneLevel.jsp";
        } else {
          destination = rootDestination + "simpleListOfPublications.jsp";
        }
      } else if ("GoToCurrentTopic".equals(function)) {
        if (!NodePK.ROOT_NODE_ID.equals(kmelia.getCurrentFolderId())) {
          request.setAttribute("Id", kmelia.getCurrentFolderId());
          destination = getDestination("GoToTopic", kmelia, request);
        } else {
          destination = getDestination("Main", kmelia, request);
        }
      } else if (function.equals("GoToBasket")) {
        destination = rootDestination + "basket.jsp";
      } else if (function.equals("ViewPublicationsToValidate")) {
        destination = rootDestination + "publicationsToValidate.jsp";
      } else if ("GoBackToResults".equals(function)) {
        request.setAttribute("SearchContext", kmelia.getSearchContext());
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.startsWith("searchResult")) {
        resetWizard(kmelia);
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        String fileAlreadyOpened = request.getParameter("FileOpened");
        String from = request.getParameter("From");
        if ("Search".equals(from)) {
          // identify clearly access from global search
          // because same URL is used from portlet, permalink...
          request.setAttribute("SearchScope", SearchContext.GLOBAL);
        }

        if (type != null && ("Publication".equals(type)
            || "com.stratelia.webactiv.calendar.backbone.TodoDetail".equals(type)
            || "Attachment".equals(type) || "Document".equals(type)
            || type.startsWith("Comment"))) {
          KmeliaSecurity security = new KmeliaSecurity(kmelia.getOrganisationController());
          try {
            boolean accessAuthorized = security.isAccessAuthorized(kmelia.getComponentId(), kmelia
                .getUserId(), id, "Publication");
            if (accessAuthorized) {
              processPath(kmelia, id);
              if ("Attachment".equals(type)) {
                String attachmentId = request.getParameter("AttachmentId");
                request.setAttribute("AttachmentId", attachmentId);
                destination = getDestination("ViewPublication", kmelia, request);
              } else if ("Document".equals(type)) {
                String documentId = request.getParameter("DocumentId");
                request.setAttribute("DocumentId", documentId);
                destination = getDestination("ViewPublication", kmelia, request);
              } else {
                if (kmaxMode) {
                  request.setAttribute("FileAlreadyOpened", fileAlreadyOpened);
                  destination = getDestination("ViewPublication", kmelia, request);
                } else if (toolboxMode) {
                  // we have to find which page contains the right publication
                  List<KmeliaPublication> publications = kmelia.getSessionPublicationsList();
                  int pubIndex = -1;
                  for (int p = 0; p < publications.size() && pubIndex == -1; p++) {
                    KmeliaPublication publication = publications.get(p);
                    if (id.equals(publication.getDetail().getPK().getId())) {
                      pubIndex = p;
                    }
                  }
                  int nbPubliPerPage = kmelia.getNbPublicationsPerPage();
                  if (nbPubliPerPage == 0) {
                    nbPubliPerPage = pubIndex;
                  }
                  int ipage = pubIndex / nbPubliPerPage;
                  kmelia.setIndexOfFirstPubToDisplay(Integer.toString(ipage * nbPubliPerPage));
                  request.setAttribute("PubIdToHighlight", id);
                  request.setAttribute("Id", kmelia.getCurrentFolderId());
                  destination = getDestination("GoToTopic", kmelia, request);
                } else {
                  request.setAttribute("FileAlreadyOpened", fileAlreadyOpened);
                  destination = getDestination("ViewPublication", kmelia, request);
                }
              }
            } else {
              destination = "/admin/jsp/accessForbidden.jsp";
            }
          } catch (Exception e) {
            SilverTrace.error("kmelia", "KmeliaRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "Document Not Found = " + e.getMessage(), e);
            destination = getDocumentNotFoundDestination(kmelia, request);
          }
        } else if ("Node".equals(type)) {
          if (kmaxMode) {
            // Simuler l'action d'un utilisateur ayant sélectionné la valeur id d'un axe
            // SearchCombination est un chemin /0/4/i
            NodeDetail node = kmelia.getNodeHeader(id);
            String path = node.getPath() + id;
            request.setAttribute("SearchCombination", path);
            destination = getDestination("KmaxSearch", kmelia, request);
          } else {
            try {
              request.setAttribute("Id", id);
              destination = getDestination("GoToTopic", kmelia, request);
            } catch (Exception e) {
              SilverTrace.error("kmelia", "KmeliaRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "Document Not Found = " + e.getMessage(), e);
              destination = getDocumentNotFoundDestination(kmelia, request);
            }
          }
        } else if ("Wysiwyg".equals(type)) {
          if (id.startsWith("Node")) {
            id = id.substring("Node_".length(), id.length());
            request.setAttribute("Id", id);
            destination = getDestination("GoToTopic", kmelia, request);
          } else {
            destination = getDestination("ViewPublication", kmelia, request);
          }
        } else {
          request.setAttribute("Id", NodePK.ROOT_NODE_ID);
          destination = getDestination("GoToTopic", kmelia, request);
        }
      } else if (function.startsWith("GoToFilesTab")) {
        String id = request.getParameter("Id");
        try {
          processPath(kmelia, id);
          if (toolboxMode) {
            KmeliaPublication kmeliaPublication = kmelia.getPublication(id);
            kmelia.setSessionPublication(kmeliaPublication);
            kmelia.setSessionOwner(true);
            destination = getDestination("ViewAttachments", kmelia, request);
          } else {
            destination = getDestination("ViewPublication", kmelia, request);
          }
        } catch (Exception e) {
          SilverTrace.error("kmelia", "KmeliaRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "Document Not Found = " + e.getMessage(), e);
          destination = getDocumentNotFoundDestination(kmelia, request);
        }
      } else if ("ToUpdatePublicationHeader".equals(function)) {
        request.setAttribute("Action", "UpdateView");
        request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
        request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());
        request.setAttribute("Publication", kmelia.getSessionPubliOrClone());
        
        destination = getDestination("publicationManager.jsp", kmelia, request);
      } else if ("publicationManager.jsp".equals(function)) {
        
        request.setAttribute("Wizard", kmelia.getWizard());
        request.setAttribute("Path", kmelia.getTopicPath(kmelia.getCurrentFolderId()));
        request.setAttribute("Profile", kmelia.getProfile());

        destination = rootDestination + "publicationManager.jsp";
        // thumbnail error for front explication
        if (request.getParameter("errorThumbnail") != null) {
          destination = destination + "&resultThumbnail=" + request.getParameter("errorThumbnail");
        }
      } else if (function.equals("ToAddTopic")) {
        String topicId = request.getParameter("Id");
        if (!SilverpeasRole.admin.isInRole(kmelia.getUserTopicProfile(topicId))) {
          destination = "/admin/jsp/accessForbidden.jsp";
        } else {
          String isLink = request.getParameter("IsLink");
          if (StringUtil.isDefined(isLink)) {
            request.setAttribute("IsLink", Boolean.TRUE);
          }

          List<NodeDetail> path = kmelia.getTopicPath(topicId);
          request.setAttribute("Path", kmelia.displayPath(path, false, 3));
          request.setAttribute("PathLinked", kmelia.displayPath(path, true, 3));
          request.setAttribute("Translation", kmelia.getCurrentLanguage());
          request.setAttribute("NotificationAllowed", kmelia.isNotificationAllowed());
          request.setAttribute("Parent", kmelia.getNodeHeader(topicId));

          if (kmelia.isRightsOnTopicsEnabled()) {
            request.setAttribute("Profiles", kmelia.getTopicProfiles());

            // Rights of the component
            request.setAttribute("RightsDependsOn", "ThisComponent");
          }

          destination = rootDestination + "addTopic.jsp";
        }
      } else if ("ToUpdateTopic".equals(function)) {
        String id = request.getParameter("Id");
        NodeDetail node = kmelia.getSubTopicDetail(id);
        if (!SilverpeasRole.admin.isInRole(kmelia.getUserTopicProfile(id))
            && !SilverpeasRole.admin.isInRole(kmelia.getUserTopicProfile(node.getFatherPK().
            getId()))) {
          destination = "/admin/jsp/accessForbidden.jsp";
        } else {
          request.setAttribute("NodeDetail", node);

          List<NodeDetail> path = kmelia.getTopicPath(id);
          request.setAttribute("Path", kmelia.displayPath(path, false, 3));
          request.setAttribute("PathLinked", kmelia.displayPath(path, true, 3));
          request.setAttribute("Translation", kmelia.getCurrentLanguage());
          request.setAttribute("NotificationAllowed", kmelia.isNotificationAllowed());

          if (kmelia.isRightsOnTopicsEnabled()) {
            request.setAttribute("Profiles", kmelia.getTopicProfiles(id));

            if (node.haveInheritedRights()) {
              request.setAttribute("RightsDependsOn", "AnotherTopic");
            } else if (node.haveLocalRights()) {
              request.setAttribute("RightsDependsOn", "ThisTopic");
            } else {
              // Rights of the component
              request.setAttribute("RightsDependsOn", "ThisComponent");
            }
          }

          destination = rootDestination + "updateTopicNew.jsp";
        }
      } else if (function.equals("AddTopic")) {
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        String alertType = request.getParameter("AlertType");
        if (!StringUtil.isDefined(alertType)) {
          alertType = "None";
        }
        String rightsUsed = request.getParameter("RightsUsed");
        String path = request.getParameter("Path");
        String parentId = request.getParameter("ParentId");

        NodeDetail topic = new NodeDetail("-1", name, description, null, null, null, "0", "X");
        I18NHelper.setI18NInfo(topic, request);

        if (StringUtil.isDefined(path)) {
          topic.setType(NodeDetail.FILE_LINK_TYPE);
          topic.setPath(path);
        }

        int rightsDependsOn = -1;
        if (StringUtil.isDefined(rightsUsed)) {
          if ("father".equalsIgnoreCase(rightsUsed)) {
            NodeDetail father = kmelia.getCurrentFolder();
            rightsDependsOn = father.getRightsDependsOn();
          } else {
            rightsDependsOn = 0;
          }
          topic.setRightsDependsOn(rightsDependsOn);
        }
        NodePK nodePK = kmelia.addSubTopic(topic, alertType, parentId);
        if (kmelia.isRightsOnTopicsEnabled()) {
          if (rightsDependsOn == 0) {
            request.setAttribute("NodeId", nodePK.getId());
            destination = getDestination("ViewTopicProfiles", kmelia, request);
          } else {
            destination = getDestination("GoToCurrentTopic", kmelia, request);
          }
        } else {
          destination = getDestination("GoToCurrentTopic", kmelia, request);
        }
      } else if ("UpdateTopic".equals(function)) {
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        String alertType = request.getParameter("AlertType");
        if (!StringUtil.isDefined(alertType)) {
          alertType = "None";
        }
        String id = request.getParameter("ChildId");
        String path = request.getParameter("Path");
        NodeDetail topic = new NodeDetail(id, name, description, null, null, null, "0", "X");
        I18NHelper.setI18NInfo(topic, request);
        if (StringUtil.isDefined(path)) {
          topic.setType(NodeDetail.FILE_LINK_TYPE);
          topic.setPath(path);
        }
        kmelia.updateTopicHeader(topic, alertType);
        if (kmelia.isRightsOnTopicsEnabled()) {
          int rightsUsed = Integer.parseInt(request.getParameter("RightsUsed"));
          topic = kmelia.getNodeHeader(id);
          if (topic.getRightsDependsOn() != rightsUsed) {
            // rights dependency have changed
            if (rightsUsed == -1) {
              kmelia.updateTopicDependency(topic, false);
              destination = getDestination("GoToCurrentTopic", kmelia, request);
            } else {
              kmelia.updateTopicDependency(topic, true);
              request.setAttribute("NodeId", id);
              destination = getDestination("ViewTopicProfiles", kmelia, request);
            }
          } else {
            destination = getDestination("GoToCurrentTopic", kmelia, request);
          }
        } else {
          destination = getDestination("GoToCurrentTopic", kmelia, request);
        }
      } else if (function.equals("DeleteTopic")) {
        String id = request.getParameter("Id");
        kmelia.deleteTopic(id);
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.equals("ViewClone")) {
        PublicationDetail pubDetail = kmelia.getSessionPublication().getDetail();

        // Reload clone and put it into session
        String cloneId = pubDetail.getCloneId();
        KmeliaPublication kmeliaPublication = kmelia.getPublication(cloneId);
        kmelia.setSessionClone(kmeliaPublication);

        request.setAttribute("Publication", kmeliaPublication);
        request.setAttribute("Profile", kmelia.getProfile());
        request.setAttribute("VisiblePublicationId", pubDetail.getPK().getId());
        request.setAttribute("UserCanValidate", kmelia.isUserCanValidatePublication());
        request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
        request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());

        putXMLDisplayerIntoRequest(kmeliaPublication.getDetail(), kmelia,
            request);

        // Attachments area must be displayed or not ?
        request.setAttribute("AttachmentsEnabled", kmelia.isAttachmentsEnabled());

        destination = rootDestination + "clone.jsp";
      } else if ("ViewPublication".equals(function)) {
        String id = request.getParameter("PubId");
        if (!StringUtil.isDefined(id)) {
          id = request.getParameter("Id");
          if (!StringUtil.isDefined(id)) {
            id = (String) request.getAttribute("PubId");
          }
        }

        if (!kmaxMode) {
          boolean checkPath = StringUtil.getBooleanValue(request.getParameter("CheckPath"));
          if (checkPath || KmeliaHelper.isToValidateFolder(kmelia.getCurrentFolderId())) {
            processPath(kmelia, id);
          } else {
            processPath(kmelia, null);
          }
        }

        // view publication from global search ?
        Integer searchScope = (Integer) request.getAttribute("SearchScope");
        if (searchScope == null) {
          if (kmelia.getSearchContext() != null) {
            request.setAttribute("SearchScope", SearchContext.LOCAL);
          } else {
            request.setAttribute("SearchScope", SearchContext.NONE);
          }
        }

        KmeliaPublication kmeliaPublication;
        if (StringUtil.isDefined(id)) {
          kmeliaPublication = kmelia.getPublication(id, true);
          kmelia.setSessionPublication(kmeliaPublication);

          PublicationDetail pubDetail = kmeliaPublication.getDetail();
          if (pubDetail.haveGotClone()) {
            KmeliaPublication clone = kmelia.getPublication(pubDetail.getCloneId());
            kmelia.setSessionClone(clone);
          }
        } else {
          kmeliaPublication = kmelia.getSessionPublication();
          id = kmeliaPublication.getDetail().getPK().getId();
        }
        if (toolboxMode) {
          request.setAttribute("PubId", id);
          destination = getDestination("publicationManager.jsp", kmelia, request);
        } else {
          List<String> publicationLanguages = kmelia.getPublicationLanguages(); // languages of
          // publication
          // header and attachments
          if (publicationLanguages.contains(kmelia.getCurrentLanguage())) {
            request.setAttribute("ContentLanguage", kmelia.getCurrentLanguage());
          } else {
            request.setAttribute("ContentLanguage", checkLanguage(kmelia, kmeliaPublication.
                getDetail()));
          }
          request.setAttribute("Languages", publicationLanguages);

          // see also management
          Collection<ForeignPK> links = kmeliaPublication.getCompleteDetail().getLinkList();
          HashSet<String> linkedList = new HashSet<String>(links.size());
          for (ForeignPK link : links) {
            linkedList.add(link.getId() + "/" + link.getInstanceId());
          }
          // put into session the current list of selected publications (see also)
          request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, linkedList);

          request.setAttribute("Publication", kmeliaPublication);
          request.setAttribute("PubId", id);
          request.setAttribute("UserCanValidate",
              kmelia.isUserCanValidatePublication() && kmelia.getSessionClone() == null);
          request.setAttribute("ValidationStep", kmelia.getValidationStep());
          request.setAttribute("ValidationType", kmelia.getValidationType());

          // check if user is writer with approval right (versioning case)
          request.setAttribute("WriterApproval", kmelia.isWriterApproval(id));
          request.setAttribute("NotificationAllowed", kmelia.isNotificationAllowed());

          // check is requested publication is an alias
          checkAlias(kmelia, kmeliaPublication);

          if (kmeliaPublication.isAlias()) {
            request.setAttribute("Profile", "user");
            request.setAttribute("TaxonomyOK", false);
            request.setAttribute("ValidatorsOK", false);
            request.setAttribute("IsAlias", "1");
          } else {
            request.setAttribute("Profile", kmelia.getProfile());
            request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
            request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());
          }

          request.setAttribute("Wizard", kmelia.getWizard());

          request.setAttribute("Rang", kmelia.getRang());
          if (kmelia.getSessionPublicationsList() != null) {
            request.setAttribute("NbPublis", kmelia.getSessionPublicationsList().size());
          } else {
            request.setAttribute("NbPublis", 1);
          }
          putXMLDisplayerIntoRequest(kmeliaPublication.getDetail(), kmelia, request);
          String fileAlreadyOpened = (String) request.getAttribute("FileAlreadyOpened");
          boolean alreadyOpened = "1".equals(fileAlreadyOpened);
          String attachmentId = (String) request.getAttribute("AttachmentId");
          String documentId = (String) request.getAttribute("DocumentId");
          if (!alreadyOpened && kmelia.openSingleAttachmentAutomatically()
              && !kmelia.isCurrentPublicationHaveContent()) {
            request.setAttribute("SingleAttachmentURL", kmelia.
                getFirstAttachmentURLOfCurrentPublication());
          } else if (!alreadyOpened && attachmentId != null) {
            request.setAttribute("SingleAttachmentURL", kmelia.getAttachmentURL(attachmentId));
          } else if (!alreadyOpened && documentId != null) {
            request.setAttribute("SingleAttachmentURL", kmelia.getAttachmentURL(documentId));
          }

          // Attachments area must be displayed or not ?
          request.setAttribute("AttachmentsEnabled", kmelia.isAttachmentsEnabled());

          // option Actualités décentralisées
          request.setAttribute("NewsManage", kmelia.isNewsManage());
          if (kmelia.isNewsManage()) {
            request.setAttribute("DelegatedNews", kmelia.getDelegatedNews(id));
            request.setAttribute("IsBasket", NodePK.BIN_NODE_ID.equals(kmelia.getCurrentFolderId()));
          }

          destination = rootDestination + "publication.jsp";
        }
      } else if (function.equals("PreviousPublication")) {
        // récupération de la publication précédente
        String pubId = kmelia.getPrevious();
        request.setAttribute("PubId", pubId);
        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("NextPublication")) {
        // récupération de la publication suivante
        String pubId = kmelia.getNext();
        request.setAttribute("PubId", pubId);
        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.startsWith("copy")) {
        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType)) {
          kmelia.copyTopic(objectId);
        } else {
          kmelia.copyPublication(objectId);
        }

        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("cut")) {
        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType)) {
          kmelia.cutTopic(objectId);
        } else {
          kmelia.cutPublication(objectId);
        }

        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        kmelia.paste();
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null) + "Idle.jsp";
      } else if (function.startsWith("ToAlertUserAttachment")) { // utilisation de alertUser et
        // alertUserPeas
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUserAttachment: function = " + function
            + " spaceId=" + kmelia.getSpaceId() + " componentId=" + kmelia.getComponentId());
        try {
          String attachmentId = request.getParameter("AttachmentOrDocumentId");
          destination = kmelia.initAlertUserAttachment(attachmentId);
        } catch (Exception e) {
          SilverTrace.warn("kmelia", "KmeliaRequestRooter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUserAttachment: function = " + function
            + "=> destination="
            + destination);
      } else if (function.startsWith("ToAlertUserDocument")) { // utilisation de alertUser et
        // alertUserPeas
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUserDocument: function = " + function
            + " spaceId="
            + kmelia.getSpaceId() + " componentId=" + kmelia.getComponentId());
        try {
          String documentId = request.getParameter("AttachmentOrDocumentId");
          destination = kmelia.initAlertUserAttachment(documentId);
        } catch (Exception e) {
          SilverTrace.warn("kmelia", "KmeliaRequestRooter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUserDocument: function = " + function
            + "=> destination="
            + destination);
      } else if (function.startsWith("ToAlertUser")) { // utilisation de alertUser et alertUserPeas
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUser: function = " + function + " spaceId="
            + kmelia.getSpaceId() + " componentId=" + kmelia.getComponentId());
        try {
          destination = kmelia.initAlertUser();
        } catch (Exception e) {
          SilverTrace.warn("kmelia", "KmeliaRequestRooter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToAlertUser: function = " + function
            + "=> destination="
            + destination);
      } else if (function.equals("ReadingControl")) {
        PublicationDetail publication = kmelia.getSessionPublication().getDetail();
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        request.setAttribute("Publication", publication);
        request.setAttribute("UserIds", kmelia.getUserIdsOfTopic());

        // paramètre du wizard
        request.setAttribute("Wizard", kmelia.getWizard());

        destination = rootDestination + "readingControlManager.jsp";
      } else if (function.startsWith("ViewAttachments")) {
        String flag = kmelia.getProfile();

        // Versioning is out of "Always visible publication" mode
        if (kmelia.isCloneNeeded() && !kmelia.isVersionControlled()) {
          kmelia.clonePublication();
        }

        // put current publication
        if (!kmelia.isVersionControlled()) {
          request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPubliOrClone().
              getDetail());
        } else {
          request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPublication().
              getDetail());
        }
        // Paramètres du wizard
        setWizardParams(request, kmelia);

        // Paramètres de i18n
        List<String> attachmentLanguages = kmelia.getAttachmentLanguages();
        if (attachmentLanguages.contains(kmelia.getCurrentLanguage())) {
          request.setAttribute("Language", kmelia.getCurrentLanguage());
        } else {
          request.setAttribute("Language", checkLanguage(kmelia));
        }
        request.setAttribute("Languages", attachmentLanguages);

        request.setAttribute("XmlFormForFiles", kmelia.getXmlFormForFiles());

        destination = rootDestination + "attachmentManager.jsp?profile=" + flag;
      } else if (function.equals("DeletePublication")) {
        String pubId = request.getParameter("PubId");
        kmelia.deletePublication(pubId);

        if (kmaxMode) {
          destination = getDestination("Main", kmelia, request);
        } else {
          destination = getDestination("GoToCurrentTopic", kmelia, request);
        }
      } else if (function.equals("DeleteClone")) {
        kmelia.deleteClone();

        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("ViewValidationSteps")) {
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        request.setAttribute("Publication", kmelia.getSessionPubliOrClone().getDetail());
        request.setAttribute("ValidationSteps", kmelia.getValidationSteps());

        request.setAttribute("Role", kmelia.getProfile());

        destination = rootDestination + "validationSteps.jsp";
      } else if ("ValidatePublication".equals(function)) {
        String pubId = kmelia.getSessionPublication().getDetail().getPK().getId();

        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "function = " + function + " pubId=" + pubId);

        boolean validationComplete = kmelia.validatePublication(pubId);
        if (validationComplete) {
          request.setAttribute("Action", "ValidationComplete");
          destination = getDestination("ViewPublication", kmelia, request);
        } else {
          request.setAttribute("Action", "ValidationInProgress");
          if (kmelia.getSessionClone() != null) {
            destination = getDestination("ViewClone", kmelia, request);
          } else {
            destination = getDestination("ViewPublication", kmelia, request);
          }
        }
      } else if (function.equals("ForceValidatePublication")) {
        String pubId = kmelia.getSessionPublication().getDetail().getPK().getId();
        kmelia.forcePublicationValidation(pubId);
        request.setAttribute("Action", "ValidationComplete");

        request.setAttribute("PubId", pubId);
        destination = getDestination("ViewPublication", kmelia, request);
      } else if ("Unvalidate".equals(function)) {
        String motive = request.getParameter("Motive");
        String pubId = kmelia.getSessionPublication().getDetail().getPK().getId();

        SilverTrace.debug("kmelia", "KmeliaRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "function = " + function + " pubId=" + pubId);

        kmelia.unvalidatePublication(pubId, motive);

        request.setAttribute("Action", "Unvalidate");

        if (kmelia.getSessionClone() != null) {
          destination = getDestination("ViewClone", kmelia, request);
        } else {
          destination = getDestination("ViewPublication", kmelia, request);
        }
      } else if (function.equals("WantToSuspendPubli")) {
        String pubId = request.getParameter("PubId");

        PublicationDetail pubDetail = kmelia.getPublicationDetail(pubId);

        request.setAttribute("PublicationToSuspend", pubDetail);

        destination = rootDestination + "defermentMotive.jsp";
      } else if (function.equals("SuspendPublication")) {
        String motive = request.getParameter("Motive");
        String pubId = request.getParameter("PubId");

        kmelia.suspendPublication(pubId, motive);

        request.setAttribute("Action", "Suspend");

        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("DraftIn")) {
        kmelia.draftInPublication();
        if (kmelia.getSessionClone() != null) {
          // draft have generate a clone
          destination = getDestination("ViewClone", kmelia, request);
        } else {
          String pubId = kmelia.getSessionPubliOrClone().getDetail().getPK().getId();
          String from = request.getParameter("From");
          if (StringUtil.isDefined(from)) {
            destination = getDestination(from, kmelia, request);
          } else {
            request.setAttribute("PubId", pubId);
            destination = getDestination("publicationManager.jsp", kmelia, request);
          }
        }
      } else if (function.equals("DraftOut")) {
        kmelia.draftOutPublication();

        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("ToTopicWysiwyg")) {
        String topicId = request.getParameter("Id");
        String subTopicId = request.getParameter("ChildId");
        String flag = kmelia.getProfile();

        NodeDetail topic = kmelia.getSubTopicDetail(subTopicId);

        destination = request.getScheme() + "://" + kmelia.getServerNameAndPort()
            + URLManager.getApplicationURL() + "/wysiwyg/jsp/htmlEditor.jsp?";
        destination += "SpaceId=" + kmelia.getSpaceId();
        destination += "&SpaceName=" + URLEncoder.encode(kmelia.getSpaceLabel(), CharEncoding.UTF_8);
        destination += "&ComponentId=" + kmelia.getComponentId();
        destination += "&ComponentName=" + URLEncoder.encode(kmelia.getComponentLabel(),
            CharEncoding.UTF_8);
        destination += "&BrowseInfo=" + URLEncoder.encode(kmelia.getSessionPathString() + " > "
            + topic.getName() + " > " + kmelia.getString("TopicWysiwyg"), CharEncoding.UTF_8);

        destination += "&ObjectId=Node_" + subTopicId;
        destination += "&Language=fr";
        destination += "&ReturnUrl=" + URLEncoder.encode(URLManager.getApplicationURL()
            + URLManager.getURL(kmelia.getSpaceId(), kmelia.getComponentId())
            + "FromTopicWysiwyg?Action=Search&Id=" + topicId + "&ChildId=" + subTopicId
            + "&Profile=" + flag, "UTF-8");
      } else if (function.equals("FromTopicWysiwyg")) {
        String subTopicId = request.getParameter("ChildId");

        kmelia.processTopicWysiwyg(subTopicId);

        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.equals("ChangeTopicStatus")) {
        String subTopicId = request.getParameter("ChildId");
        String newStatus = request.getParameter("Status");
        String recursive = request.getParameter("Recursive");

        if (recursive != null && recursive.equals("1")) {
          kmelia.changeTopicStatus(newStatus, subTopicId, true);
        } else {
          kmelia.changeTopicStatus(newStatus, subTopicId, false);
        }

        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.equals("ViewOnly")) {
        String id = request.getParameter("documentId");
        destination = rootDestination + "publicationViewOnly.jsp?Id=" + id;
      } else if (function.equals("SeeAlso")) {
        String action = request.getParameter("Action");
        if (!StringUtil.isDefined(action)) {
          action = "LinkAuthorView";
        }

        request.setAttribute("Action", action);

        // check if requested publication is an alias
        KmeliaPublication kmeliaPublication = kmelia.getSessionPublication();
        checkAlias(kmelia, kmeliaPublication);

        if (kmeliaPublication.isAlias()) {
          request.setAttribute("Profile", "user");
          request.setAttribute("IsAlias", "1");
        } else {
          request.setAttribute("Profile", kmelia.getProfile());
        }

        // paramètres du wizard
        request.setAttribute("Wizard", kmelia.getWizard());

        destination = rootDestination + "seeAlso.jsp";
      } else if (function.equals("DeleteSeeAlso")) {
        String[] pubIds = request.getParameterValues("PubIds");

        List<ForeignPK> infoLinks = new ArrayList<ForeignPK>();
        StringTokenizer tokens = null;
        for (String pubId : pubIds) {
          tokens = new StringTokenizer(pubId, "/");
          infoLinks.add(new ForeignPK(tokens.nextToken(), tokens.nextToken()));
        }

        if (infoLinks.size() > 0) {
          kmelia.deleteInfoLinks(kmelia.getSessionPublication().getId(), infoLinks);
        }

        destination = getDestination("SeeAlso", kmelia, request);
      } else if (function.equals("ImportFileUpload")) {
        destination = processFormUpload(kmelia, request, rootDestination, false);
      } else if (function.equals("ImportFilesUpload")) {
        destination = processFormUpload(kmelia, request, rootDestination, true);
      } else if (function.equals("ExportAttachementsToPDF")) {
        String topicId = request.getParameter("TopicId");
        // build an exploitable list by importExportPeas
        SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
            "root.MSG_PARAM_VALUE", "topicId =" + topicId);
        List<WAAttributeValuePair> publicationsIds = kmelia
            .getAllVisiblePublicationsByTopic(topicId);
        request.setAttribute("selectedResultsWa", publicationsIds);
        request.setAttribute("RootId", topicId);
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportPDF";
      } else if (function.equals("NewPublication")) {
        request.setAttribute("Action", "New");
        request.setAttribute("TaxonomyOK", true);
        request.setAttribute("ValidatorsOK", true);
        destination = getDestination("publicationManager.jsp", kmelia, request);
      } else if (function.equals("ManageSubscriptions")) {
        destination = kmelia.manageSubscriptions();
      } else if (function.equals("AddPublication")) {
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);

        // create publication
        String positions = FileUploadUtil.getParameter(parameters, "Positions");
        PdcClassificationEntity withClassification = PdcClassificationEntity
            .undefinedClassification();
        if (StringUtil.isDefined(positions)) {
          withClassification = PdcClassificationEntity.fromJSON(positions);
        }
        PublicationDetail pubDetail = getPublicationDetail(parameters, kmelia);
        String newPubId = kmelia.createPublication(pubDetail, withClassification);
        // create vignette if exists
        processVignette(parameters, kmelia, pubDetail);
        request.setAttribute("PubId", newPubId);
        processPath(kmelia, newPubId);
        String wizard = kmelia.getWizard();
        if ("progress".equals(wizard)) {
          KmeliaPublication kmeliaPublication = kmelia.getPublication(newPubId);
          kmelia.setSessionPublication(kmeliaPublication);
          String position = FileUploadUtil.getParameter(parameters, "Position");
          setWizardParams(request, kmelia);
          request.setAttribute("Position", position);
          request.setAttribute("Publication", kmeliaPublication);
          request.setAttribute("Profile", kmelia.getProfile());
          destination = getDestination("WizardNext", kmelia, request);
        } else {
          StringBuffer requestURI = request.getRequestURL();
          destination = requestURI.substring(0, requestURI.indexOf("AddPublication"))
              + "ViewPublication?PubId=" + newPubId;
        }
      } else if ("UpdatePublication".equals(function)) {
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);

        PublicationDetail pubDetail = getPublicationDetail(parameters, kmelia);
        kmelia.updatePublication(pubDetail);

        String id = pubDetail.getPK().getId();
        processVignette(parameters, kmelia, pubDetail);

        String wizard = kmelia.getWizard();
        if (wizard.equals("progress")) {
          KmeliaPublication kmeliaPublication = kmelia.getPublication(id);
          String position = FileUploadUtil.getParameter(parameters, "Position");
          setWizardParams(request, kmelia);
          request.setAttribute("Position", position);
          request.setAttribute("Publication", kmeliaPublication);
          request.setAttribute("Profile", kmelia.getProfile());
          destination = getDestination("WizardNext", kmelia, request);
        } else {
          if (kmelia.getSessionClone() != null) {
            destination = getDestination("ViewClone", kmelia, request);
          } else {
            request.setAttribute("PubId", id);
            request.setAttribute("CheckPath", "1");
            destination = getDestination("ViewPublication", kmelia, request);
          }
        }
      } else if (function.equals("SelectValidator")) {
        destination = kmelia.initUPToSelectValidator("");
      } else if (function.equals("Comments")) {
        String id = request.getParameter("PubId");
        String flag = kmelia.getProfile();
        if (!kmaxMode) {
          processPath(kmelia, id);
        }
        // paramètre du wizard
        request.setAttribute("Wizard", kmelia.getWizard());
        destination = rootDestination + "comments.jsp?PubId=" + id + "&Profile=" + flag;
      } else if (function.equals("PublicationPaths")) {
        // paramètre du wizard
        request.setAttribute("Wizard", kmelia.getWizard());

        PublicationDetail publication = kmelia.getSessionPublication().getDetail();
        String pubId = publication.getPK().getId();
        request.setAttribute("Publication", publication);
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        request.setAttribute("PathList", kmelia.getPublicationFathers(pubId));

        if (toolboxMode) {
          request.setAttribute("Topics", kmelia.getAllTopics());
        } else {
          List<Alias> aliases = kmelia.getAliases();
          request.setAttribute("Aliases", aliases);
          request.setAttribute("Components", kmelia.getComponents(aliases));
        }

        destination = rootDestination + "publicationPaths.jsp";
      } else if (function.equals("SetPath")) {
        String[] topics = request.getParameterValues("topicChoice");
        String loadedComponentIds = request.getParameter("LoadedComponentIds");

        Alias alias = null;
        List<Alias> aliases = new ArrayList<Alias>();
        for (int i = 0; topics != null && i < topics.length; i++) {
          String topicId = topics[i];
          SilverTrace.debug("kmelia", "KmeliaRequestRouter.setPath()", "root.MSG_GEN_PARAM_VALUE",
              "topicId = " + topicId);
          StringTokenizer tokenizer = new StringTokenizer(topicId, ",");
          String nodeId = tokenizer.nextToken();
          String instanceId = tokenizer.nextToken();

          alias = new Alias(nodeId, instanceId);
          alias.setUserId(kmelia.getUserId());
          aliases.add(alias);
        }

        // Tous les composants ayant un alias n'ont pas forcément été chargés
        List<Alias> oldAliases = kmelia.getAliases();
        for (Alias oldAlias : oldAliases) {
          if (!loadedComponentIds.contains(oldAlias.getInstanceId())) {
            // le composant de l'alias n'a pas été chargé
            aliases.add(oldAlias);
          }
        }

        kmelia.setAliases(aliases);

        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("ShowAliasTree")) {
        String componentId = request.getParameter("ComponentId");

        request.setAttribute("Tree", kmelia.getAliasTreeview(componentId));
        request.setAttribute("Aliases", kmelia.getAliases());

        destination = rootDestination + "treeview4PublicationPaths.jsp";
      } else if (function.equals("AddLinksToPublication")) {
        String id = request.getParameter("PubId");
        String topicId = request.getParameter("TopicId");
        HashSet<String> list = (HashSet<String>) request.getSession().getAttribute(
            KmeliaConstants.PUB_TO_LINK_SESSION_KEY);

        int nb = kmelia.addPublicationsToLink(id, list);

        request.setAttribute("NbLinks", Integer.toString(nb));

        destination = rootDestination + "publicationLinksManager.jsp?Action=Add&Id=" + topicId;
      } else if (function.equals("ExportComponent")) {
        if (kmaxMode) {
          destination = getDestination("KmaxExportComponent", kmelia, request);
        } else {
          // build an exploitable list by importExportPeas
          List<WAAttributeValuePair> publicationsIds = kmelia.getAllVisiblePublications();
          request.setAttribute("selectedResultsWa", publicationsIds);
          request.setAttribute("RootId", "0");
          // Go to importExportPeas
          destination = "/RimportExportPeas/jsp/ExportItems";
        }
      } else if (function.equals("ExportTopic")) {
        if (kmaxMode) {
          destination = getDestination("KmaxExportPublications", kmelia, request);
        } else {
          // récupération du topicId
          String topicId = request.getParameter("TopicId");
          // build an exploitable list by importExportPeas
          SilverTrace.info("kmelia", "KmeliaSessionController.getAllVisiblePublicationsByTopic()",
              "root.MSG_PARAM_VALUE", "topicId =" + topicId);
          List<WAAttributeValuePair> publicationsIds = kmelia.getAllVisiblePublicationsByTopic(
              topicId);
          request.setAttribute("selectedResultsWa", publicationsIds);
          request.setAttribute("RootId", topicId);
          // Go to importExportPeas
          destination = "/RimportExportPeas/jsp/ExportItems";
        }
      } else if (function.equals("ExportPublications")) {
        String selectedIds = request.getParameter("SelectedIds");
        String notSelectedIds = request.getParameter("NotSelectedIds");
        List<String> ids = kmelia.processSelectedPublicationIds(selectedIds, notSelectedIds);

        List<WAAttributeValuePair> publicationIds = new ArrayList<WAAttributeValuePair>();
        for (String id : ids) {
          publicationIds.add(new WAAttributeValuePair(id, kmelia.getComponentId()));
        }
        request.setAttribute("selectedResultsWa", publicationIds);
        kmelia.resetSelectedPublicationIds();
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/SelectExportMode";
      } else if (function.equals("ToPubliContent")) {
        CompletePublication completePublication = kmelia.getSessionPubliOrClone()
            .getCompleteDetail();
        if (completePublication.getModelDetail() != null) {
          destination = getDestination("ToDBModel", kmelia, request);
        } else if (WysiwygController.haveGotWysiwyg(kmelia.getComponentId(),
            completePublication.getPublicationDetail().getPK().getId(),
            kmelia.getCurrentLanguage())) {

          destination = getDestination("ToWysiwyg", kmelia, request);
        } else {
          String infoId = completePublication.getPublicationDetail().getInfoId();
          if (infoId == null || "0".equals(infoId)) {
            List<String> usedModels = (List<String>) kmelia.getModelUsed();
            if (usedModels.size() == 1) {
              String modelId = usedModels.get(0);
              if ("WYSIWYG".equals(modelId)) {
                // Wysiwyg content
                destination = getDestination("ToWysiwyg", kmelia, request);
              } else if (StringUtil.isInteger(modelId)) {
                // DB template
                ModelDetail model = kmelia.getModelDetail(modelId);
                request.setAttribute("ModelDetail", model);
                destination = getDestination("ToDBModel", kmelia, request);
              } else {
                // XML template
                request.setAttribute("Name", modelId);
                destination = getDestination("GoToXMLForm", kmelia, request);
              }
            } else {
              destination = getDestination("ListModels", kmelia, request);
            }
          } else {
            destination = getDestination("GoToXMLForm", kmelia, request);
          }
        }
      } else if (function.equals("ListModels")) {
        setTemplatesUsedIntoRequest(kmelia, request);

        // put current publication
        request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPublication().
            getDetail());

        // Paramètres du wizard
        setWizardParams(request, kmelia);
        destination = rootDestination + "modelsList.jsp";
      } else if (function.equals("ModelUsed")) {
        request.setAttribute("XMLForms", kmelia.getForms());
        // put dbForms
        Collection<ModelDetail> dbForms = kmelia.getAllModels();
        request.setAttribute("DBForms", dbForms);

        Collection<String> modelUsed = kmelia.getModelUsed();
        request.setAttribute("ModelUsed", modelUsed);

        destination = rootDestination + "modelUsedList.jsp";
      } else if (function.equals("SelectModel")) {
        Object o = request.getParameterValues("modelChoice");
        if (o != null) {
          kmelia.addModelUsed((String[]) o);
        }
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if ("ChangeTemplate".equals(function)) {
        kmelia.removePublicationContent();
        destination = getDestination("ToPubliContent", kmelia, request);
      } else if ("ToWysiwyg".equals(function)) {
        if (kmelia.isCloneNeeded()) {
          kmelia.clonePublication();
        }
        // put current publication
        request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPubliOrClone().
            getDetail());

        // Parametres du Wizard
        setWizardParams(request, kmelia);
        request.setAttribute("Language", kmelia.getContentLanguage());
        request.setAttribute("CurrentLanguage", kmelia.getContentLanguage());

        destination = rootDestination + "toWysiwyg.jsp";
      } else if ("FromWysiwyg".equals(function)) {
        String id = request.getParameter("PubId");

        // Parametres du Wizard
        String wizard = kmelia.getWizard();
        setWizardParams(request, kmelia);
        if (wizard.equals("progress")) {
          request.setAttribute("Position", "Content");
          destination = getDestination("WizardNext", kmelia, request);
        } else {
          if (kmelia.getSessionClone() != null
              && id.equals(kmelia.getSessionClone().getDetail().getPK().
              getId())) {
            destination = getDestination("ViewClone", kmelia, request);
          } else {
            destination = getDestination("ViewPublication", kmelia, request);
          }
        }
      } else if (function.equals("ToDBModel")) {
        String modelId = request.getParameter("ModelId");
        if (StringUtil.isDefined(modelId)) {
          ModelDetail model = kmelia.getModelDetail(modelId);
          request.setAttribute("ModelDetail", model);
        }

        // put current publication
        request.setAttribute("CompletePublication", kmelia.getSessionPubliOrClone().
            getCompleteDetail());
        request.setAttribute("NotificationAllowed", kmelia.isNotificationAllowed());

        // Paramètres du wizard
        setWizardParams(request, kmelia);

        destination = rootDestination + "modelManager.jsp";
      } else if (function.equals("UpdateDBModelContent")) {
        ResourceLocator publicationSettings = kmelia.getPublicationSettings();

        List<InfoTextDetail> textDetails = new ArrayList<InfoTextDetail>();
        List<InfoImageDetail> imageDetails = new ArrayList<InfoImageDetail>();
        int imageOrder = 0;
        boolean imageTrouble = false;
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);
        String modelId = FileUploadUtil.getParameter(parameters, "ModelId");
        // Parametres du Wizard
        setWizardParams(request, kmelia);

        for (FileItem item : parameters) {
          if (item.isFormField() && item.getFieldName().startsWith("WATXTVAR")) {
            String theText = item.getString();
            int textOrder = Integer.parseInt(item.getFieldName().substring(8,
                item.getFieldName().length()));
            textDetails.add(new InfoTextDetail(null, Integer.toString(textOrder), null, theText));
          } else if (!item.isFormField()) {
            String logicalName = item.getName();
            if (logicalName != null && logicalName.length() > 0) {
              if (!FileUtil.isWindows()) {
                logicalName = logicalName.replace('\\', File.separatorChar);
                SilverTrace.info("kmelia", "KmeliaRequestRouter.UpdateDBModelContent",
                    "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + logicalName);
              }
              logicalName = FilenameUtils.getName(logicalName);
              String physicalName = Long.toString(System.currentTimeMillis()) + "." + FilenameUtils
                  .getExtension(logicalName);
              String mimeType = item.getContentType();
              long size = item.getSize();

              File dir = new File(FileRepositoryManager.getAbsolutePath(kmelia.getComponentId())
                  + publicationSettings.getString("imagesSubDirectory") + File.separatorChar
                  + physicalName);
              if (FileUtil.isImage(logicalName)) {
                item.write(dir);
                imageOrder++;
                if (size > 0L) {
                  imageDetails.add(new InfoImageDetail(null, Integer.toString(imageOrder),
                      null, physicalName, logicalName, "", mimeType, size));
                  imageTrouble = false;
                } else {
                  imageTrouble = true;
                }
              } else {
                imageTrouble = true;
              }
            } else {
              // the field did not contain a file
            }
          }
        }

        InfoDetail infos = new InfoDetail(null, textDetails, imageDetails, null, "");
        CompletePublication completePub = kmelia.getSessionPubliOrClone().getCompleteDetail();
        if (completePub.getModelDetail() == null) {
          kmelia.createInfoModelDetail("useless", modelId, infos);
        } else {
          kmelia.updateInfoDetail("useless", infos);
        }
        if (imageTrouble) {
          request.setAttribute("ImageTrouble", Boolean.TRUE);
        }

        String wizard = kmelia.getWizard();
        if (wizard.equals("progress")) {
          request.setAttribute("Position", "Content");
          destination = getDestination("WizardNext", kmelia, request);
        } else {
          destination = getDestination("ToDBModel", kmelia, request);
        }
      } else if (function.equals("GoToXMLForm")) {
        String xmlFormName = request.getParameter("Name");
        if (!StringUtil.isDefined(xmlFormName)) {
          xmlFormName = (String) request.getAttribute("Name");
        }
        setXMLForm(request, kmelia, xmlFormName);
        // put current publication
        request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPubliOrClone().
            getDetail());
        // Parametres du Wizard
        setWizardParams(request, kmelia);
        // template can be changed only if current topic is using at least two templates
        setTemplatesUsedIntoRequest(kmelia, request);
        @SuppressWarnings("unchecked")
        Collection<PublicationTemplate> templates = (Collection<PublicationTemplate>) request
            .getAttribute("XMLForms");
        boolean wysiwygUsable = (Boolean) request.getAttribute("WysiwygValid");
        request.setAttribute("IsChangingTemplateAllowed",
            templates.size() >= 2 || (!templates.isEmpty() && wysiwygUsable));

        destination = rootDestination + "xmlForm.jsp";
      } else if (function.equals("UpdateXMLForm")) {
        if (kmelia.isCloneNeeded()) {
          kmelia.clonePublication();
        }

        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        String encoding = request.getCharacterEncoding();
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        PublicationDetail pubDetail = kmelia.getSessionPubliOrClone().getDetail();

        String xmlFormShortName;

        // Is it the creation of the content or an update ?
        String infoId = pubDetail.getInfoId();
        if (infoId == null || "0".equals(infoId)) {
          String xmlFormName = FileUploadUtil.getParameter(items, "Name", null, encoding);

          // The publication have no content
          // We have to register xmlForm to publication
          xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName
              .indexOf('.'));
          pubDetail.setInfoId(xmlFormShortName);
          kmelia.updatePublication(pubDetail);
        } else {
          xmlFormShortName = pubDetail.getInfoId();
        }
        String pubId = pubDetail.getPK().getId();
        PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(
            kmelia.getComponentId() + ":" + xmlFormShortName);
        RecordSet set = pub.getRecordSet();
        Form form = pub.getUpdateForm();
        String language = checkLanguage(kmelia, pubDetail);
        DataRecord data = set.getRecord(pubId, language);
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(pubId);
          data.setLanguage(language);
        }
        PagesContext context = new PagesContext("myForm", "3", kmelia.getLanguage(), false, kmelia
            .getComponentId(), kmelia.getUserId());
        context.setEncoding(CharEncoding.UTF_8);
        if (!kmaxMode) {
          context.setNodeId(kmelia.getCurrentFolderId());
        }
        context.setObjectId(pubId);
        context.setContentLanguage(kmelia.getCurrentLanguage());

        form.update(items, data, context);
        set.save(data);

        // update publication to change updateDate and updaterId
        kmelia.updatePublication(pubDetail);

        // Parametres du Wizard
        setWizardParams(request, kmelia);

        if (kmelia.getWizard().equals("progress")) {
          // on est en mode Wizard
          request.setAttribute("Position", "Content");
          destination = getDestination("WizardNext", kmelia, request);
        } else {
          if (kmelia.getSessionClone() != null) {
            destination = getDestination("ViewClone", kmelia, request);
          } else if (kmaxMode) {
            destination = getDestination("ViewAttachments", kmelia, request);
          } else {
            destination = getDestination("ViewPublication", kmelia, request);
          }
        }
      } else if (function.startsWith("ToOrderPublications")) {
        List<KmeliaPublication> publications = kmelia.getSessionPublicationsList();

        request.setAttribute("Publications", publications);
        request.setAttribute("Path", kmelia.getSessionPath());

        destination = rootDestination + "orderPublications.jsp";
      } else if (function.startsWith("OrderPublications")) {
        String sortedIds = request.getParameter("sortedIds");

        StringTokenizer tokenizer = new StringTokenizer(sortedIds, ",");
        List<String> ids = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
          ids.add(tokenizer.nextToken());
        }
        kmelia.orderPublications(ids);

        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.equals("ToOrderTopics")) {
        String id = request.getParameter("Id");
        if (!SilverpeasRole.admin.isInRole(kmelia.getUserTopicProfile(id))) {
          destination = "/admin/jsp/accessForbidden.jsp";
        } else {
          TopicDetail topic = kmelia.getTopic(id);
          request.setAttribute("Nodes", topic.getNodeDetail().getChildrenDetails());
          destination = rootDestination + "orderTopics.jsp";
        }
      } else if (function.startsWith("Wizard")) {
        destination = processWizard(function, kmelia, request, rootDestination);
      } else if ("ViewTopicProfiles".equals(function)) {
        String role = request.getParameter("Role");
        if (!StringUtil.isDefined(role)) {
          role = SilverpeasRole.admin.toString();
        }

        String id = request.getParameter("NodeId");
        if (!StringUtil.isDefined(id)) {
          id = (String) request.getAttribute("NodeId");
        }
        request.setAttribute("Profiles", kmelia.getTopicProfiles(id));
        NodeDetail topic = kmelia.getNodeHeader(id);
        ProfileInst profile = null;
        if (topic.haveInheritedRights()) {
          profile = kmelia.getTopicProfile(role, Integer.toString(topic.getRightsDependsOn()));

          request.setAttribute("RightsDependsOn", "AnotherTopic");
        } else if (topic.haveLocalRights()) {
          profile = kmelia.getTopicProfile(role, Integer.toString(topic.getRightsDependsOn()));

          request.setAttribute("RightsDependsOn", "ThisTopic");
        } else {
          profile = kmelia.getProfile(role);
          // Rights of the component
          request.setAttribute("RightsDependsOn", "ThisComponent");
        }

        request.setAttribute("CurrentProfile", profile);
        request.setAttribute("Groups", kmelia.groupIds2Groups(profile.getAllGroups()));
        request.setAttribute("Users", kmelia.userIds2Users(profile.getAllUsers()));
        List<NodeDetail> path = kmelia.getTopicPath(id);
        request.setAttribute("Path", kmelia.displayPath(path, true, 3));
        request.setAttribute("NodeDetail", topic);

        destination = rootDestination + "topicProfiles.jsp";
      } else if (function.equals("TopicProfileSelection")) {
        String role = request.getParameter("Role");
        String nodeId = request.getParameter("NodeId");
        try {
          kmelia.initUserPanelForTopicProfile(role, nodeId);
        } catch (Exception e) {
          SilverTrace.warn("jobStartPagePeas", "JobStartPagePeasRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
      } else if (function.equals("TopicProfileSetUsersAndGroups")) {
        String role = request.getParameter("Role");
        String nodeId = request.getParameter("NodeId");

        kmelia.updateTopicRole(role, nodeId);

        request.setAttribute("urlToReload", "ViewTopicProfiles?Role=" + role + "&NodeId=" + nodeId);
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.equals("TopicProfileRemove")) {
        String profileId = request.getParameter("Id");

        kmelia.deleteTopicRole(profileId);

        destination = getDestination("ViewTopicProfiles", kmelia, request);
      } else if (function.equals("CloseWindow")) {
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.startsWith("UpdateChain")) {
        destination = processUpdateChainOperation(rootDestination, function, kmelia, request);
      } else if (function.equals("SuggestDelegatedNews")) {

        String pubId = kmelia.addDelegatedNews();

        request.setAttribute("PubId", pubId);
        destination = getDestination("ViewPublication", kmelia, request);
      }/**
       * *************************
       * Kmax mode ***********************
       */
      else if (function.equals("KmaxMain")) {
        destination = rootDestination + "kmax.jsp?Action=KmaxView&Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxAxisManager")) {
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxViewAxis&Profile="
            + kmelia.getProfile();
      } else if (function.equals("KmaxAddAxis")) {
        String newAxisName = request.getParameter("Name");
        String newAxisDescription = request.getParameter("Description");
        NodeDetail axis = new NodeDetail("-1", newAxisName, newAxisDescription, DateUtil
            .today2SQLDate(),
            kmelia.getUserId(), null, "0", "X");
        // I18N
        I18NHelper.setI18NInfo(axis, request);
        kmelia.addAxis(axis);

        request.setAttribute("urlToReload", "KmaxAxisManager");
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.equals("KmaxUpdateAxis")) {
        String axisId = request.getParameter("AxisId");
        String newAxisName = request.getParameter("AxisName");
        String newAxisDescription = request.getParameter("AxisDescription");
        NodeDetail axis = new NodeDetail(axisId, newAxisName, newAxisDescription, null, null, null,
            "0", "X");
        // I18N
        I18NHelper.setI18NInfo(axis, request);
        kmelia.updateAxis(axis);
        destination = getDestination("KmaxAxisManager", kmelia, request);
      } else if (function.equals("KmaxDeleteAxis")) {
        String axisId = request.getParameter("AxisId");
        kmelia.deleteAxis(axisId);
        destination = getDestination("KmaxAxisManager", kmelia, request);
      } else if (function.equals("KmaxManageAxis")) {
        String axisId = request.getParameter("AxisId");
        String translation = request.getParameter("Translation");
        request.setAttribute("Translation", translation);
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxManageAxis&Profile="
            + kmelia.getProfile() + "&AxisId=" + axisId;
      } else if (function.equals("KmaxManagePosition")) {
        String positionId = request.getParameter("PositionId");
        String translation = request.getParameter("Translation");
        request.setAttribute("Translation", translation);
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxManagePosition&Profile="
            + kmelia.getProfile() + "&PositionId=" + positionId;
      } else if (function.equals("KmaxAddPosition")) {
        String axisId = request.getParameter("AxisId");
        String newPositionName = request.getParameter("Name");
        String newPositionDescription = request.getParameter("Description");
        String translation = request.getParameter("Translation");
        NodeDetail position = new NodeDetail("toDefine", newPositionName, newPositionDescription,
            null, null, null, "0", "X");
        // I18N
        I18NHelper.setI18NInfo(position, request);
        kmelia.addPosition(axisId, position);
        request.setAttribute("AxisId", axisId);
        request.setAttribute("Translation", translation);
        destination = getDestination("KmaxManageAxis", kmelia, request);
      } else if (function.equals("KmaxUpdatePosition")) {
        String positionId = request.getParameter("PositionId");
        String positionName = request.getParameter("PositionName");
        String positionDescription = request.getParameter("PositionDescription");
        NodeDetail position = new NodeDetail(positionId, positionName, positionDescription, null,
            null, null, "0", "X");
        // I18N
        I18NHelper.setI18NInfo(position, request);
        kmelia.updatePosition(position);
        destination = getDestination("KmaxAxisManager", kmelia, request);
      } else if (function.equals("KmaxDeletePosition")) {
        String positionId = request.getParameter("PositionId");
        kmelia.deletePosition(positionId);
        destination = getDestination("KmaxAxisManager", kmelia, request);
      } else if (function.equals("KmaxViewUnbalanced")) {
        List<KmeliaPublication> publications = kmelia.getUnbalancedPublications();
        kmelia.setSessionPublicationsList(publications);
        kmelia.orderPubs();

        destination = rootDestination + "kmax.jsp?Action=KmaxViewUnbalanced&Profile="
            + kmelia.getProfile();
      } else if (function.equals("KmaxViewBasket")) {
        TopicDetail basket = kmelia.getTopic("1");
        List<KmeliaPublication> publications = (List<KmeliaPublication>) basket.
            getKmeliaPublications();
        kmelia.setSessionPublicationsList(publications);
        kmelia.orderPubs();

        destination = rootDestination + "kmax.jsp?Action=KmaxViewBasket&Profile=" + kmelia
            .getProfile();

      } else if (function.equals("KmaxViewToValidate")) {
        destination = rootDestination + "kmax.jsp?Action=KmaxViewToValidate&Profile="
            + kmelia.getProfile();
      } else if (function.equals("KmaxSearch")) {
        String axisValuesStr = request.getParameter("SearchCombination");
        if (!StringUtil.isDefined(axisValuesStr)) {
          axisValuesStr = (String) request.getAttribute("SearchCombination");
        }
        String timeCriteria = request.getParameter("TimeCriteria");

        SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "axisValuesStr = " + axisValuesStr + " timeCriteria="
            + timeCriteria);
        List<String> combination = kmelia.getCombination(axisValuesStr);
        List<KmeliaPublication> publications = null;
        if (StringUtil.isDefined(timeCriteria) && !"X".equals(timeCriteria)) {
          publications = kmelia.search(combination, Integer.parseInt(timeCriteria));
        } else {
          publications = kmelia.search(combination);
        }
        SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "publications = " + publications + " Combination="
            + combination + " timeCriteria=" + timeCriteria);

        kmelia.setIndexOfFirstPubToDisplay("0");
        kmelia.orderPubs();
        kmelia.setSessionCombination(combination);
        kmelia.setSessionTimeCriteria(timeCriteria);

        destination = rootDestination + "kmax.jsp?Action=KmaxSearchResult&Profile=" + kmelia
            .getProfile();
      } else if (function.equals("KmaxSearchResult")) {
        if (kmelia.getSessionCombination() == null) {
          destination = getDestination("KmaxMain", kmelia, request);
        } else {
          destination = rootDestination + "kmax.jsp?Action=KmaxSearchResult&Profile="
              + kmelia.getProfile();
        }
      } else if (function.equals("KmaxViewCombination")) {
        setWizardParams(request, kmelia);

        request.setAttribute("CurrentCombination", kmelia.getCurrentCombination());
        destination = rootDestination + "kmax_viewCombination.jsp?Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxAddCoordinate")) {
        String pubId = request.getParameter("PubId");
        String axisValuesStr = request.getParameter("SearchCombination");
        StringTokenizer st = new StringTokenizer(axisValuesStr, ",");
        List<String> combination = new ArrayList<String>();
        String axisValue = "";
        while (st.hasMoreTokens()) {
          axisValue = st.nextToken();
          // axisValue is xx/xx/xx where xx are nodeId
          axisValue = axisValue.substring(axisValue.lastIndexOf('/') + 1, axisValue.length());
          combination.add(axisValue);
        }
        kmelia.addPublicationToCombination(pubId, combination);
        // Store current combination
        kmelia.setCurrentCombination(kmelia.getCombination(axisValuesStr));
        destination = getDestination("KmaxViewCombination", kmelia, request);
      } else if (function.equals("KmaxDeleteCoordinate")) {
        String coordinateId = request.getParameter("CoordinateId");
        String pubId = request.getParameter("PubId");
        SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "coordinateId = " + coordinateId + " PubId=" + pubId);
        kmelia.deletePublicationFromCombination(pubId, coordinateId);
        destination = getDestination("KmaxViewCombination", kmelia, request);
      } else if (function.equals("KmaxExportComponent")) {
        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> publicationsIds = kmelia.getAllVisiblePublications();
        request.setAttribute("selectedResultsWa", publicationsIds);

        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/KmaxExportComponent";
      } else if (function.equals("KmaxExportPublications")) {
        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> publicationsIds = kmelia.getCurrentPublicationsList();
        List<String> combination = kmelia.getSessionCombination();
        // get the time axis
        String timeCriteria = null;
        if (kmelia.isTimeAxisUsed() && StringUtil.isDefined(kmelia.getSessionTimeCriteria())) {
          ResourceLocator timeSettings = new ResourceLocator(
              "org.silverpeas.kmelia.multilang.timeAxisBundle", kmelia.getLanguage());
          if (kmelia.getSessionTimeCriteria().equals("X")) {
            timeCriteria = null;
          } else {
            timeCriteria = "<b>" + kmelia.getString("TimeAxis") + "</b> > " + timeSettings
                .getString(kmelia.getSessionTimeCriteria(), "");
          }
        }
        request.setAttribute("selectedResultsWa", publicationsIds);
        request.setAttribute("Combination", combination);
        request.setAttribute("TimeCriteria", timeCriteria);
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/KmaxExportPublications";
      } else if ("statistics".equals(function)) {
        destination = rootDestination + statisticRequestHandler.handleRequest(request, function,
            kmelia);
      } else if ("statSelectionGroup".equals(function)) {
        destination = statisticRequestHandler.handleRequest(request, function, kmelia);
      } else {
        destination = rootDestination + function;
      }

      if (profileError) {
        destination = GeneralPropertiesManager.getString("sessionTimeout");
      }
      SilverTrace.info("kmelia", "KmeliaRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private String getDocumentNotFoundDestination(KmeliaSessionController kmelia,
      HttpServletRequest request) {
    request.setAttribute("ComponentId", kmelia.getComponentId());
    return "/admin/jsp/documentNotFound.jsp";
  }

  private PublicationDetail getPublicationDetail(List<FileItem> parameters,
      KmeliaSessionController kmelia) throws Exception {
    String id = FileUploadUtil.getParameter(parameters, "PubId");
    String status = FileUploadUtil.getParameter(parameters, "Status");
    String name = FileUploadUtil.getParameter(parameters, "Name");
    String description = FileUploadUtil.getParameter(parameters, "Description");
    String keywords = FileUploadUtil.getParameter(parameters, "Keywords");
    String beginDate = FileUploadUtil.getParameter(parameters, "BeginDate");
    String endDate = FileUploadUtil.getParameter(parameters, "EndDate");
    String version = FileUploadUtil.getParameter(parameters, "Version");
    String importance = FileUploadUtil.getParameter(parameters, "Importance");
    String beginHour = FileUploadUtil.getParameter(parameters, "BeginHour");
    String endHour = FileUploadUtil.getParameter(parameters, "EndHour");
    String author = FileUploadUtil.getParameter(parameters, "Author");
    String targetValidatorId = FileUploadUtil.getParameter(parameters, "ValideurId");
    String tempId = FileUploadUtil.getParameter(parameters, "TempId");
    String infoId = FileUploadUtil.getParameter(parameters, "InfoId");
    String draftOutDate = FileUploadUtil.getParameter(parameters, "DraftOutDate");

    Date jBeginDate = null;
    Date jEndDate = null;
    Date jDraftOutDate = null;

    if (StringUtil.isDefined(beginDate)) {
      jBeginDate = DateUtil.stringToDate(beginDate, kmelia.getLanguage());
    }
    if (StringUtil.isDefined(endDate)) {
      jEndDate = DateUtil.stringToDate(endDate, kmelia.getLanguage());
    }
    if (StringUtil.isDefined(draftOutDate)) {
      jDraftOutDate = DateUtil.stringToDate(draftOutDate, kmelia.getLanguage());
    }

    String pubId = "X";
    if (StringUtil.isDefined(id)) {
      pubId = id;
    }
    PublicationDetail pubDetail = new PublicationDetail(pubId, name, description, null, jBeginDate,
        jEndDate, null, importance, version, keywords, "", status, "", author);
    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);
    pubDetail.setStatus(status);
    pubDetail.setDraftOutDate(jDraftOutDate);
    if (StringUtil.isDefined(targetValidatorId)) {
      pubDetail.setTargetValidatorId(targetValidatorId);
    }

    pubDetail.setCloneId(tempId);

    if (StringUtil.isDefined(infoId)) {
      pubDetail.setInfoId(infoId);
    }

    I18NHelper.setI18NInfo(pubDetail, parameters);

    return pubDetail;
  }

  private void processVignette(List<FileItem> parameters, KmeliaSessionController kmelia,
      PublicationDetail publication)
      throws Exception {
    // First, check if image have been uploaded
    FileItem file = FileUploadUtil.getFile(parameters, "WAIMGVAR0");
    String mimeType = null;
    String physicalName = null;
    if (file != null) {
      String logicalName = file.getName().replace('\\', '/');
      if (StringUtil.isDefined(logicalName)) {
        logicalName = logicalName.substring(logicalName.lastIndexOf('/') + 1, logicalName.length());
        mimeType = FileUtil.getMimeType(logicalName);
        String type = FileRepositoryManager.getFileExtension(logicalName);
        if (FileUtil.isImage(logicalName)) {
          physicalName = String.valueOf(System.currentTimeMillis()) + '.' + type;
          File dir = new File(FileRepositoryManager.getAbsolutePath(kmelia.getComponentId())
              + kmelia.getPublicationSettings().getString("imagesSubDirectory"));
          if (!dir.exists()) {
            dir.mkdirs();
          }
          File target = new File(dir, physicalName);
          file.write(target);
        } else {
          throw new ThumbnailRuntimeException("KmeliaRequestRouter.processVignette()",
              SilverpeasRuntimeException.ERROR,
              "thumbnail_EX_MSG_WRONG_TYPE_ERROR");
        }
      }
    }

    // If no image have been uploaded, check if one have been picked up from a gallery
    if (physicalName == null) {
      // on a pas d'image, regarder s'il y a une provenant de la galerie
      String nameImageFromGallery = FileUploadUtil.getParameter(parameters, "valueImageGallery");
      if (StringUtil.isDefined(nameImageFromGallery)) {
        physicalName = nameImageFromGallery;
        mimeType = "image/jpeg";
      }
    }

    // If one image is defined, save it through Thumbnail service
    if (StringUtil.isDefined(physicalName)) {
      ThumbnailDetail detail = new ThumbnailDetail(kmelia.getComponentId(),
          Integer.parseInt(publication.getPK().getId()),
          ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      detail.setOriginalFileName(physicalName);
      detail.setMimeType(mimeType);
      try {
        int[] thumbnailSize = kmelia.getThumbnailWidthAndHeight();
        ThumbnailController.updateThumbnail(detail, thumbnailSize[0], thumbnailSize[1]);

        // force indexation to taking into account new thumbnail
        if (publication.isIndexable()) {
          kmelia.getPublicationBm().createIndex(publication.getPK());
        }
      } catch (ThumbnailRuntimeException e) {
        SilverTrace.error("thumbnail", "KmeliaRequestRouter.processVignette",
            "thumbnail_MSG_UPDATE_THUMBNAIL_KO", e);
        try {
          ThumbnailController.deleteThumbnail(detail);
        } catch (Exception exp) {
          SilverTrace.info("thumbnail", "KmeliaRequestRouter.processVignette",
              "thumbnail_MSG_DELETE_THUMBNAIL_KO", exp);
        }
      }
    }
  }

  /**
   * Process Form Upload for publications import
   *
   * @param kmeliaScc
   * @param request
   * @param routeDestination
   * @return destination
   */
  private String processFormUpload(KmeliaSessionController kmeliaScc,
      HttpServletRequest request, String routeDestination, boolean isMassiveMode) {
    String destination = "";
    String topicId = "";
    String importMode = KmeliaSessionController.UNITARY_IMPORT_MODE;
    boolean draftMode = false;
    String logicalName = "";
    String message = "";

    String tempFolderName = "";
    String tempFolderPath = "";

    String fileType = "";
    long fileSize = 0L;
    long processStart = new Date().getTime();
    ResourceLocator attachmentResourceLocator = new ResourceLocator(
        "org.silverpeas.util.attachment.multilang.attachment",
        kmeliaScc.getLanguage());
    FileItem fileItem = null;
    int versionType = DocumentVersion.TYPE_DEFAULT_VERSION;

    try {
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      topicId = FileUploadUtil.getParameter(items, "topicId");
      importMode = FileUploadUtil.getParameter(items, "opt_importmode");

      String sVersionType = FileUploadUtil.getParameter(items,
          "opt_versiontype");
      if (StringUtil.isDefined(sVersionType)) {
        versionType = Integer.parseInt(sVersionType);
      }

      String sDraftMode = FileUploadUtil.getParameter(items, "chk_draft");
      if (StringUtil.isDefined(sDraftMode)) {
        draftMode = StringUtil.getBooleanValue(sDraftMode);
      }

      fileItem = FileUploadUtil.getFile(items, "file_name");

      if (fileItem != null) {
        logicalName = fileItem.getName();
        if (logicalName != null) {
          boolean runOnUnix = !FileUtil.isWindows();
          if (runOnUnix) {
            logicalName = logicalName.replace('\\', File.separatorChar);
            SilverTrace.info("kmelia", "KmeliaRequestRouter.processFormUpload",
                "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = "
                + logicalName);
          }

          logicalName = logicalName.substring(logicalName.lastIndexOf(File.separator) + 1,
              logicalName.length());

          // Name of temp folder: timestamp and userId
          tempFolderName = Long.toString(System.currentTimeMillis()) + "_" + kmeliaScc.getUserId();

          // Mime type of the file
          fileType = fileItem.getContentType();

          // Zip contentType not detected under Firefox !
          if (!ClientBrowserUtil.isInternetExplorer(request)) {
            fileType = MimeTypes.ARCHIVE_MIME_TYPE;
          }

          fileSize = fileItem.getSize();

          // Directory Temp for the uploaded file
          tempFolderPath = FileRepositoryManager.getAbsolutePath(kmeliaScc.getComponentId())
              + GeneralPropertiesManager.getString("RepositoryTypeTemp") + File.separator
              + tempFolderName;
          if (!new File(tempFolderPath).exists()) {
            FileRepositoryManager.createAbsolutePath(kmeliaScc.getComponentId(),
                GeneralPropertiesManager.getString("RepositoryTypeTemp") + File.separator
                + tempFolderName);
          }

          // Creation of the file in the temp folder
          File fileUploaded = new File(FileRepositoryManager.getAbsolutePath(kmeliaScc
              .getComponentId()) + GeneralPropertiesManager.getString("RepositoryTypeTemp")
              + File.separator + tempFolderName + File.separator + logicalName);
          fileItem.write(fileUploaded);

          // Is a real file ?
          if (fileSize > 0L) {
            SilverTrace.debug("kmelia", "KmeliaRequestRouter.processFormUpload()",
                "root.MSG_GEN_PARAM_VALUE", "fileUploaded = " + fileUploaded
                + " fileSize=" + fileSize + " fileType=" + fileType
                + " importMode=" + importMode + " draftMode=" + draftMode);
            int nbFiles = 1;
            // Compute nbFiles only in unitary Import mode
            if (!KmeliaSessionController.UNITARY_IMPORT_MODE.equals(importMode)
                && fileUploaded.getName().toLowerCase().endsWith(".zip")) {
              nbFiles = ZipManager.getNbFiles(fileUploaded);
            }

            // Import !!
            List<PublicationDetail> publicationDetails = kmeliaScc.importFile(fileUploaded,
                fileType, topicId, importMode, draftMode, versionType);
            long processDuration = new Date().getTime() - processStart;

            // Title for popup report
            String importModeTitle = "";
            if (importMode.equals(KmeliaSessionController.UNITARY_IMPORT_MODE)) {
              importModeTitle = kmeliaScc.getString("kmelia.ImportModeUnitaireTitre");
            } else {
              importModeTitle = kmeliaScc.getString("kmelia.ImportModeMassifTitre");
            }
            SilverTrace.debug("kmelia", "KmeliaRequestRouter.processFormUpload()",
                "root.MSG_GEN_PARAM_VALUE", "nbFiles = " + nbFiles
                + " publicationDetails=" + publicationDetails
                + " ProcessDuration=" + processDuration + " ImportMode="
                + importMode + " Draftmode=" + draftMode + " Title="
                + importModeTitle);

            request.setAttribute("PublicationsDetails", publicationDetails);
            request.setAttribute("NbFiles", nbFiles);
            request.setAttribute("ProcessDuration", FileRepositoryManager.formatFileUploadTime(
                processDuration));
            request.setAttribute("ImportMode", importMode);
            request.setAttribute("DraftMode", draftMode);
            request.setAttribute("Title", importModeTitle);
            request.setAttribute("Context", URLManager.getApplicationURL());

            destination = routeDestination + "reportImportFiles.jsp";
            String componentId = publicationDetails.get(0).getComponentInstanceId();
            if (kmeliaScc.isDefaultClassificationModifiable(topicId, componentId)) {
              destination = routeDestination + "validateImportedFilesClassification.jsp";
            }
          } else {
            // File access failed
            message = attachmentResourceLocator.getString("liaisonInaccessible");
            request.setAttribute("Message", message);
            request.setAttribute("TopicId", topicId);
            destination = routeDestination + "importOneFile.jsp";
            if (isMassiveMode) {
              destination = routeDestination + "importMultiFiles.jsp";
            }
          }
          FileFolderManager.deleteFolder(tempFolderPath);
        } else {
          // the field did not contain a file
          request.setAttribute("Message", attachmentResourceLocator
              .getString("liaisonInaccessible"));
          request.setAttribute("TopicId", topicId);
          destination = routeDestination + "importOneFile.jsp";
          if (isMassiveMode) {
            destination = routeDestination + "importMultiFiles.jsp";
          }
        }
      }
    } catch (Exception e) {
      // Other exception
      request.setAttribute("Message", e.getMessage());
      request.setAttribute("TopicId", topicId);
      destination = routeDestination + "importOneFile.jsp";
      if (isMassiveMode) {
        destination = routeDestination + "importMultiFiles.jsp";
      }

      SilverTrace.warn("kmelia", "KmeliaRequestRouter.processFormUpload()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    return destination;
  }

  private void processPath(KmeliaSessionController kmeliaSC, String id)
      throws RemoteException {
    if (!kmeliaSC.isKmaxMode) {
      NodePK pk = null;
      if (!StringUtil.isDefined(id)) {
        pk = kmeliaSC.getCurrentFolderPK();
      } else {
        pk = kmeliaSC.getAllowedPublicationFather(id); // get publication parent
        kmeliaSC.setCurrentFolderId(pk.getId(), true);
      }

      Collection<NodeDetail> pathColl = kmeliaSC.getTopicPath(pk.getId());
      String linkedPathString = kmeliaSC.displayPath(pathColl, true, 3);
      String pathString = kmeliaSC.displayPath(pathColl, false, 3);
      kmeliaSC.setSessionPath(linkedPathString);
      kmeliaSC.setSessionPathString(pathString);
    }
  }

  private void putXMLDisplayerIntoRequest(PublicationDetail pubDetail,
      KmeliaSessionController kmelia, HttpServletRequest request) throws
      PublicationTemplateException, FormException {
    String infoId = pubDetail.getInfoId();
    String pubId = pubDetail.getPK().getId();
    if (!StringUtil.isInteger(infoId)) {
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
          pubDetail.getPK().getInstanceId() + ":" + infoId);

      // RecordTemplate recordTemplate = pubTemplate.getRecordTemplate();
      Form formView = pubTemplate.getViewForm();
      // get displayed language
      String language = checkLanguage(kmelia, pubDetail);
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(pubId, language);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(pubId);
      }
      request.setAttribute("XMLForm", formView);
      request.setAttribute("XMLData", data);
    }
  }

  private String processWizard(String function,
      KmeliaSessionController kmeliaSC, HttpServletRequest request,
      String rootDestination) throws RemoteException,
      PublicationTemplateException, FormException {
    String destination = "";
    if (function.equals("WizardStart")) {
      // récupération de l'id du thème dans lequel on veux mettre la
      // publication
      // si on ne viens pas d'un theme-tracker
      String topicId = request.getParameter("TopicId");
      if (StringUtil.isDefined(topicId)) {
        TopicDetail topic = kmeliaSC.getTopic(topicId);
        kmeliaSC.setSessionTopic(topic);
      }
      // recherche du dernier onglet
      String wizardLast = "1";
      List<String> invisibleTabs = kmeliaSC.getInvisibleTabs();
      if (kmeliaSC.isKmaxMode) {
        wizardLast = "4";
      } else if (invisibleTabs.indexOf(KmeliaSessionController.TAB_ATTACHMENTS) == -1) {
        wizardLast = "3";
      } else if (invisibleTabs.indexOf(KmeliaSessionController.TAB_CONTENT) == -1) {
        wizardLast = "2";
      }
      kmeliaSC.setWizardLast(wizardLast);
      request.setAttribute("WizardLast", wizardLast);
      kmeliaSC.setWizard("progress");
      request.setAttribute("Action", "Wizard");
      request.setAttribute("Profile", kmeliaSC.getProfile());
      destination = rootDestination + "wizardPublicationManager.jsp";
    } else if (function.equals("WizardHeader")) {
      // passage des paramètres
      String id = request.getParameter("PubId");
      if (!StringUtil.isDefined(id)) {
        id = (String) request.getAttribute("PubId");
      }
      request.setAttribute("WizardRow", kmeliaSC.getWizardRow());
      request.setAttribute("WizardLast", kmeliaSC.getWizardLast());
      request.setAttribute("Action", "UpdateWizard");
      request.setAttribute("Profile", kmeliaSC.getProfile());
      request.setAttribute("PubId", id);

      destination = rootDestination + "wizardPublicationManager.jsp";
    } else if (function.equals("WizardNext")) {
      // redirige vers l'onglet suivant de l'assistant de publication
      String position = request.getParameter("Position");
      if (!StringUtil.isDefined(position)) {
        position = (String) request.getAttribute("Position");
      }

      String next = "End";

      String wizardRow = kmeliaSC.getWizardRow();
      request.setAttribute("WizardRow", wizardRow);
      int numRow = 0;
      if (StringUtil.isDefined(wizardRow)) {
        numRow = Integer.parseInt(wizardRow);
      }

      List<String> invisibleTabs = kmeliaSC.getInvisibleTabs();

      if (position.equals("View")) {
        if (invisibleTabs.indexOf(KmeliaSessionController.TAB_CONTENT) == -1) {
          // on passe à la page du contenu
          next = "Content";
          if (numRow <= 2) {
            wizardRow = "2";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }

        } else if (invisibleTabs.indexOf(KmeliaSessionController.TAB_ATTACHMENTS) == -1) {
          next = "Attachment";
          if (numRow <= 3) {
            wizardRow = "3";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }
        } else if (kmeliaSC.isKmaxMode) {
          if (numRow <= 4) {
            wizardRow = "4";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }
          next = "KmaxClassification";
        }
      } else if (position.equals("Content")) {
        if (invisibleTabs.indexOf(KmeliaSessionController.TAB_ATTACHMENTS) == -1) {
          next = "Attachment";
          if (numRow <= 3) {
            wizardRow = "3";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }
        } else if (kmeliaSC.isKmaxMode) {
          if (numRow <= 4) {
            wizardRow = "4";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }
          next = "KmaxClassification";
        }
      } else if (position.equals("Attachment")) {
        if (kmeliaSC.isKmaxMode) {
          next = "KmaxClassification";
        } else {
          next = "End";
        }

        if (!next.equals("End")) {
          if (numRow <= 4) {
            wizardRow = "4";
          } else if (numRow < Integer.parseInt(wizardRow)) {
            wizardRow = Integer.toString(numRow);
          }
        }
      } else if (position.equals("KmaxClassification")) {
        if (numRow <= 4) {
          wizardRow = "4";
        } else if (numRow < Integer.parseInt(wizardRow)) {
          wizardRow = Integer.toString(numRow);
        }
        next = "End";
      }

      // mise à jour du rang en cours
      kmeliaSC.setWizardRow(wizardRow);

      // passage des paramètres
      setWizardParams(request, kmeliaSC);

      if (next.equals("View")) {
        destination = getDestination("WizardStart", kmeliaSC, request);
      } else if (next.equals("Content")) {
        destination = getDestination("ToPubliContent", kmeliaSC, request);
      } else if (next.equals("Attachment")) {
        destination = getDestination("ViewAttachments", kmeliaSC, request);
      } else if (next.equals("KmaxClassification")) {
        destination = getDestination("KmaxViewCombination", kmeliaSC, request);
      } else if (next.equals("End")) {
        // terminer la publication : la sortir du mode brouillon
        kmeliaSC.setWizard("finish");
        kmeliaSC.draftOutPublication();
        destination = getDestination("ViewPublication", kmeliaSC, request);
      }
    }

    return destination;
  }

  private void setWizardParams(HttpServletRequest request,
      KmeliaSessionController kmelia) {
    // Paramètres du wizard
    request.setAttribute("Wizard", kmelia.getWizard());
    request.setAttribute("WizardRow", kmelia.getWizardRow());
    request.setAttribute("WizardLast", kmelia.getWizardLast());
  }

  private void resetWizard(KmeliaSessionController kmelia) {
    kmelia.setWizard("none");
    kmelia.setWizardLast("0");
    kmelia.setWizardRow("0");
  }

  private void setXMLForm(HttpServletRequest request,
      KmeliaSessionController kmelia, String xmlFormName)
      throws PublicationTemplateException, FormException {
    PublicationDetail pubDetail = kmelia.getSessionPubliOrClone().getDetail();
    String pubId = pubDetail.getPK().getId();

    String xmlFormShortName = null;
    if (!StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName = pubDetail.getInfoId();
      xmlFormName = null;
    } else {
      xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName
          .indexOf('.'));
      SilverTrace.info("kmelia", "KmeliaRequestRouter.setXMLForm()",
          "root.MSG_GEN_PARAM_VALUE", "xmlFormShortName = " + xmlFormShortName);

      // register xmlForm to publication
      getPublicationTemplateManager().addDynamicPublicationTemplate(kmelia.getComponentId()
          + ":" + xmlFormShortName, xmlFormName);
    }
    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
        .getPublicationTemplate(kmelia.getComponentId() + ':' + xmlFormShortName, xmlFormName);
    Form formUpdate = pubTemplate.getUpdateForm();
    RecordSet recordSet = pubTemplate.getRecordSet();

    // get displayed language
    String language = checkLanguage(kmelia, pubDetail);

    DataRecord data = recordSet.getRecord(pubId, language);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(pubId);
    }

    request.setAttribute("Form", formUpdate);
    request.setAttribute("Data", data);
    request.setAttribute("XMLFormName", xmlFormName);
  }

  private void setLanguage(HttpServletRequest request, KmeliaSessionController kmelia) {
    String language = request.getParameter("SwitchLanguage");
    if (StringUtil.isDefined(language)) {
      kmelia.setCurrentLanguage(language);
    }
    request.setAttribute("Language", kmelia.getCurrentLanguage());
  }

  private String checkLanguage(KmeliaSessionController kmelia) {
    return checkLanguage(kmelia, kmelia.getSessionPublication().getDetail());
  }

  private String checkLanguage(KmeliaSessionController kmelia,
      PublicationDetail pubDetail) {
    return pubDetail.getLanguageToDisplay(kmelia.getCurrentLanguage());
  }

  private void checkAlias(KmeliaSessionController kmelia,
      KmeliaPublication publication) {
    if (!kmelia.getComponentId().equals(
        publication.getDetail().getPK().getInstanceId())) {
      publication.asAlias();
    }
  }

  private void updatePubliDuringUpdateChain(String id, HttpServletRequest request,
      KmeliaSessionController kmelia) throws RemoteException {
    // enregistrement des modifications de la publi
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    String keywords = request.getParameter("Keywords");
    String tree = request.getParameter("Tree");
    String[] topics = request.getParameterValues("topicChoice");

    // sauvegarde des données
    Fields fields = kmelia.getFieldUpdateChain();
    FieldUpdateChainDescriptor field = kmelia.getFieldUpdateChain().getName();
    field.setName("Name");
    field.setValue(name);
    fields.setName(field);
    field = kmelia.getFieldUpdateChain().getDescription();
    field.setName("Description");
    field.setValue(description);
    fields.setDescription(field);
    field = kmelia.getFieldUpdateChain().getKeywords();
    field.setName("Keywords");
    field.setValue(keywords);
    fields.setKeywords(field);
    field = kmelia.getFieldUpdateChain().getTree();
    if (field != null) {
      field.setName("Topics");
      field.setValue(tree);
      fields.setTree(field);
    }

    fields.setTopics(topics);
    kmelia.setFieldUpdateChain(fields);
    String pubId = "X";
    if (StringUtil.isDefined(id)) {
      pubId = id;
    }
    PublicationDetail pubDetail = new PublicationDetail(pubId, name, description, null, null, null,
        null, "0", "", keywords, "", "", "", "");
    pubDetail.setStatus("Valid");
    I18NHelper.setI18NInfo(pubDetail, request);

    try {
      // Execute helper
      String helperClassName = kmelia.getFieldUpdateChain().getHelper();
      UpdateChainHelper helper;
      helper = (UpdateChainHelper) Class.forName(helperClassName).newInstance();
      UpdateChainHelperContext uchc = new UpdateChainHelperContext(pubDetail, kmelia);
      uchc.setAllTopics(kmelia.getAllTopics());
      helper.execute(uchc);
      pubDetail = uchc.getPubDetail();

      // mettre à jour les emplacements si necessaire
      String[] calculedTopics = uchc.getTopics();
      if (calculedTopics != null) {
        topics = calculedTopics;
      }
    } catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
    }
    kmelia.updatePublication(pubDetail);

    Alias alias = null;
    List<Alias> aliases = new ArrayList<Alias>();
    for (int i = 0; topics != null && i < topics.length; i++) {
      String topicId = topics[i];
      StringTokenizer tokenizer = new StringTokenizer(topicId, ",");
      String nodeId = tokenizer.nextToken();
      String instanceId = tokenizer.nextToken();

      alias = new Alias(nodeId, instanceId);
      alias.setUserId(kmelia.getUserId());
      aliases.add(alias);
    }
    kmelia.setAliases(pubDetail.getPK(), aliases);
  }

  private String processUpdateChainOperation(String rootDestination,
      String function, KmeliaSessionController kmelia,
      HttpServletRequest request) throws IOException, ClassNotFoundException,
      SAXException, ParserConfigurationException {
    if ("UpdateChainInit".equals(function)) {
      // récupération du descripteur
      kmelia.initUpdateChainDescriptor();

      // Modification par chaine de toutes les publications du thème :
      // positionnement sur la première publi
      String pubId = kmelia.getFirst();
      request.setAttribute("PubId", pubId);
      // initialiser le topic en cours
      kmelia.initUpdateChainTopicChoice(pubId);

      return getDestination("UpdateChainPublications", kmelia, request);
    } else if ("UpdateChainPublications".equals(function)) {
      String id = (String) request.getAttribute("PubId");

      request.setAttribute("Action", "UpdateChain");
      request.setAttribute("Profile", kmelia.getProfile());
      request.setAttribute("PubId", id);
      request.setAttribute("SaveFields", kmelia.getFieldUpdateChain());

      if (StringUtil.isDefined(id)) {
        request.setAttribute("Rang", kmelia.getRang());
        request.setAttribute("NbPublis", kmelia.getNbPublis());

        // request.setAttribute("PathList",kmelia.getPublicationFathers(id));
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());

        request.setAttribute("Topics", kmelia.getUpdateChainTopics());

        // mise à jour de la publication en session pour récupérer les alias
        KmeliaPublication kmeliaPublication = kmelia.getPublication(id);
        kmelia.setSessionPublication(kmeliaPublication);

        List<Alias> aliases = kmelia.getAliases();
        request.setAttribute("Aliases", aliases);

        // url du fichier joint
        request.setAttribute("FileUrl", kmelia.getFirstAttachmentURLOfCurrentPublication());
        return rootDestination + "updateByChain.jsp";
      }
    } else if ("UpdateChainNextUpdate".equals(function)) {
      String id = request.getParameter("PubId");
      updatePubliDuringUpdateChain(id, request, kmelia);

      // récupération de la publication suivante
      String nextPubId = kmelia.getNext();
      request.setAttribute("PubId", nextPubId);

      return getDestination("UpdateChainPublications", kmelia, request);
    } else if ("UpdateChainLastUpdate".equals(function)) {
      String id = request.getParameter("PubId");
      updatePubliDuringUpdateChain(id, request, kmelia);

      // mise à jour du theme pour le retour
      request.setAttribute("Id", kmelia.getCurrentFolderId());
      return getDestination("GoToTopic", kmelia, request);
    } else if ("UpdateChainSkipUpdate".equals(function)) {
      // récupération de la publication suivante
      String pubId = kmelia.getNext();
      request.setAttribute("PubId", pubId);
      return getDestination("UpdateChainPublications", kmelia, request);
    } else if ("UpdateChainEndUpdate".equals(function)) {
      // mise à jour du theme pour le retour
      request.setAttribute("Id", kmelia.getCurrentFolderId());
      return getDestination("GoToTopic", kmelia, request);
    } else if ("UpdateChainUpdateAll".equals(function)) {
      // mise à jour du theme pour le retour
      request.setAttribute("Id", kmelia.getCurrentFolderId());

      // enregistrement des modifications sur toutes les publications restantes
      // publication courante
      String id = request.getParameter("PubId");
      updatePubliDuringUpdateChain(id, request, kmelia);

      // passer à la suivante si elle existe
      int rang = kmelia.getRang();
      int nbPublis = kmelia.getSessionPublicationsList().size();
      while (rang < nbPublis - 1) {
        String pubId = kmelia.getNext();
        updatePubliDuringUpdateChain(pubId, request, kmelia);
        rang = kmelia.getRang();
      }

      // retour au thème
      return getDestination("GoToTopic", kmelia, request);
    }
    return "";
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  public PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private void setTemplatesUsedIntoRequest(KmeliaSessionController kmelia,
      HttpServletRequest request) throws RemoteException {
    Collection<String> modelUsed = kmelia.getModelUsed();
    Collection<PublicationTemplate> listModelXml = new ArrayList<PublicationTemplate>();
    List<PublicationTemplate> templates = kmelia.getForms();
    // recherche de la liste des modèles utilisables
    for (PublicationTemplate template : templates) {
      PublicationTemplate xmlForm = template;
      // recherche si le modèle est dans la liste
      if (modelUsed.contains(xmlForm.getFileName())) {
        listModelXml.add(xmlForm);
      }
    }

    request.setAttribute("XMLForms", listModelXml);

    // put dbForms
    Collection<ModelDetail> dbForms = kmelia.getAllModels();
    // recherche de la liste des modèles utilisables
    Collection<ModelDetail> listModelForm = new ArrayList<ModelDetail>();
    for (ModelDetail modelDetail : dbForms) {
      // recherche si le modèle est dans la liste
      if (modelUsed.contains(modelDetail.getId())) {
        listModelForm.add(modelDetail);
      }
    }
    request.setAttribute("DBForms", listModelForm);

    // recherche si modele Wysiwyg utilisable
    boolean wysiwygValid = false;
    if (modelUsed.contains("WYSIWYG")) {
      wysiwygValid = true;
    }
    request.setAttribute("WysiwygValid", wysiwygValid);

    // s'il n'y a pas de modèles selectionnés, les présenter tous
    if ((listModelXml == null || listModelXml.isEmpty())
        && (listModelForm == null || listModelForm.isEmpty()) && !wysiwygValid) {
      request.setAttribute("XMLForms", templates);
      request.setAttribute("DBForms", dbForms);
      request.setAttribute("WysiwygValid", Boolean.TRUE);
    }
  }

  /**
   * Converts the specified identifier into a Silverpeas content primary key.
   *
   * @param instanceId the unique identifier of the component instance to which the contents
   * belongs.
   * @param ids one or several identifiers of Silverpeas contents.
   * @return a list of one or several Silverpeas primary keys, each of them corresponding to one
   * specified identifier.
   */
  private List<ForeignPK> asPks(String instanceId, String... ids) {
    List<ForeignPK> pks = new ArrayList<ForeignPK>();
    for (String oneId : ids) {
      pks.add(new ForeignPK(oneId, instanceId));
    }
    return pks;
  }
  
  private void setAttributesToPublicationHeader(KmeliaSessionController kmelia, HttpServletRequest request) {
    
  }
}
