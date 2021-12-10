/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.components.kmelia.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.kmelia.KmeliaConstants;
import org.silverpeas.components.kmelia.SearchContext;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.FileFolder;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.servlets.handlers.StatisticRequestHandler;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.subscription.PublicationAliasSubscriptionResource;
import org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionResource;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.contribution.util.ContributionBatchManagementContext;
import org.silverpeas.core.contribution.util.ContributionManagementContext;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.mvc.util.WysiwygRouting;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.util.ClientBrowserUtil;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static org.silverpeas.core.contribution.model.CoreContributionType.NODE;

public class KmeliaRequestRouter extends ComponentRequestRouter<KmeliaSessionController> {

  private static final long serialVersionUID = 1L;
  private static final StatisticRequestHandler STATISTIC_REQUEST_HANDLER = new StatisticRequestHandler();
  private static final String SINGLE_ATTACHMENT_URL_ATTR = "SingleAttachmentURL";

  /**
   * This method creates a KmeliaSessionController instance
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
   * @param function The entering request function ( : "Main.jsp")
   * @param kmelia The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, KmeliaSessionController kmelia,
      HttpRequest request) {
    String destination = "";
    String rootDestination = "/kmelia/jsp/";
    boolean profileError = false;
    boolean kmaxMode = false;
    boolean toolboxMode;
    SilverpeasRole userRoleOnCurrentTopic =
        SilverpeasRole.fromString(kmelia.getUserTopicProfile(kmelia.getCurrentFolderId()));
    SilverpeasRole highestSilverpeasUserRoleOnCurrentTopic = null;
    if (userRoleOnCurrentTopic != null) {
      highestSilverpeasUserRoleOnCurrentTopic =
          SilverpeasRole.getHighestFrom(userRoleOnCurrentTopic);
    }
    try {
      if ("kmax".equals(kmelia.getComponentRootName())) {
        kmaxMode = true;
        kmelia.setKmaxMode(true);
      }
      request.setAttribute("KmaxMode", kmaxMode);

      toolboxMode = KmeliaHelper.isToolbox(kmelia.getComponentId());

      // Set language choosen by the user
      setLanguage(request, kmelia);

      if (function.startsWith("Main")) {
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
        request.setAttribute("Context", URLUtil.getApplicationURL());
        request.setAttribute("PublicationsDetails", publications);
        destination = rootDestination + "validateImportedFilesClassification.jsp";
      } else if (function.startsWith("portlet")) {
        kmelia.setSessionPublication(null);
        String flag = kmelia.getHighestSilverpeasUserRole().getName();
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

        String path;
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
        request.setAttribute("CurrentFolderId", topicId);
        request.setAttribute("DisplayNBPublis", kmelia.displayNbPublis());
        request.setAttribute("DisplaySearch", kmelia.isSearchOnTopicsEnabled());

        request.setAttribute("Profile", kmelia.getUserTopicProfile(topicId));
        request.setAttribute("IsGuest", kmelia.getUserDetail().isAccessGuest());
        request.setAttribute("RightsOnTopicsEnabled", kmelia.isRightsOnTopicsEnabled());
        request.setAttribute("WysiwygDescription", kmelia.getWysiwygOnTopic());
        request.setAttribute("PageIndex", kmelia.getIndexOfFirstPubToDisplay());

        request.setAttribute("ExtraForm", kmelia.getXmlFormSearchForPublications());

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
        SearchContext searchContext = kmelia.getSearchContext();
        request.setAttribute("SearchContext", searchContext);
        kmelia.setCurrentFolderId(searchContext.getNode().getId(), true);
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        String fileAlreadyOpened = request.getParameter("FileOpened");
        String from = request.getParameter("From");
        if ("Search".equals(from)) {
          // identify clearly access from global search
          // because same URL is used from portlet, permalink...
          request.setAttribute("SearchScope", SearchContext.GLOBAL);
        }

        if (type != null && ("Publication".equals(type) ||
            "org.silverpeas.core.personalorganizer.model.TodoDetail".equals(type) ||
            "Attachment".equals(type) || "Document".equals(type) || type.startsWith("Comment"))) {
          try {
            PublicationDetail pub2Check = kmelia.getPublicationDetail(id);
            // If given PK defines a clone, change PK to master
            if (pub2Check.haveGotClone()) {
              // check if publication is really the master or the clone ?
              int pubId = Integer.parseInt(pub2Check.getId());
              int cloneId = Integer.parseInt(pub2Check.getCloneId());
              boolean clone = pubId > cloneId;
              if (clone) {
                id = pub2Check.getCloneId();
                request.setAttribute("ForcedId", id);
              }
            }
            final PublicationPK pubPk = new PublicationPK(id, kmelia.getComponentId());
            if (PublicationAccessControl.get().isUserAuthorized(kmelia.getUserId(), pubPk)) {
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
            destination = processDocumentNotFoundException(request, kmelia, e);
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
              destination = processDocumentNotFoundException(request, kmelia, e);
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
          destination = processDocumentNotFoundException(request, kmelia, e);
        }
      } else if ("ToUpdatePublicationHeader".equals(function)) {
        request.setAttribute("Action", "UpdateView");
        destination = getDestination("ToPublicationHeader", kmelia, request);
      } else if ("ToPublicationHeader".equals(function)) {
        String action = (String) request.getAttribute("Action");
        if ("UpdateView".equals(action)) {
          request.setAttribute("AttachmentsEnabled", false);
          request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
          request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());
          request.setAttribute("Publication", kmelia.getSessionPubliOrClone());
          setupRequestForContributionManagementContext(request,
              highestSilverpeasUserRoleOnCurrentTopic, kmelia.getCurrentFolderPK(),
              kmelia.getSessionPubliOrClone().getDetail());
        } else if ("New".equals(action)) {
          // Attachments area must be displayed or not ?
          request.setAttribute("AttachmentsEnabled", kmelia.isAttachmentsEnabled());
          request.setAttribute("TaxonomyOK", true);
          request.setAttribute("ValidatorsOK", true);
        }

        request.setAttribute("Path", kmelia.getTopicPath(kmelia.getCurrentFolderId()));
        request.setAttribute("Profile", kmelia.getProfile());

        destination = rootDestination + "publicationManager.jsp";
        // thumbnail error for front explication
        if (request.getParameter("errorThumbnail") != null) {
          destination = destination + "&resultThumbnail=" + request.getParameter("errorThumbnail");
        }
      } else if (function.equals("ToAddTopic")) {
        String topicId = request.getParameter("Id");
        if (!SilverpeasRole.ADMIN.isInRole(kmelia.getUserTopicProfile(topicId))) {
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
        if (!SilverpeasRole.ADMIN.isInRole(kmelia.getUserTopicProfile(id)) &&
            !SilverpeasRole.ADMIN.isInRole(kmelia.getUserTopicProfile(NodePK.ROOT_NODE_ID)) &&
            !SilverpeasRole.ADMIN
                .isInRole(kmelia.getUserTopicProfile(node.getFatherPK().getId()))) {
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

        NodeDetail topic = new NodeDetail("-1", name, description, 0, "X");
        I18NHelper.setI18NInfo(topic, request);

        if (StringUtil.isDefined(path)) {
          topic.setNodeType(NodeDetail.FILE_LINK_TYPE);
          topic.setPath(path);
        }

        String rightsDependsOn = NodeDetail.NO_RIGHTS_DEPENDENCY;
        if (StringUtil.isDefined(rightsUsed)) {
          if ("father".equalsIgnoreCase(rightsUsed)) {
            NodeDetail father = kmelia.getCurrentFolder();
            rightsDependsOn = father.getRightsDependsOn();
          } else {
            rightsDependsOn = NodePK.ROOT_NODE_ID;
          }
          topic.setRightsDependsOn(rightsDependsOn);
        }
        NodePK nodePK = kmelia.addSubTopic(topic, alertType, parentId);
        if (kmelia.isRightsOnTopicsEnabled()) {
          if (rightsDependsOn.equals(NodePK.ROOT_NODE_ID)) {
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
        NodeDetail topic = new NodeDetail(id, name, description, 0, "X");
        I18NHelper.setI18NInfo(topic, request);
        if (StringUtil.isDefined(path)) {
          topic.setNodeType(NodeDetail.FILE_LINK_TYPE);
          topic.setPath(path);
        }
        boolean goToProfilesDefinition = false;
        if (kmelia.isRightsOnTopicsEnabled()) {
          String rightsUsed = request.getParameter("RightsUsed");
          topic.setRightsDependsOn(rightsUsed);

          // process destination
          NodeDetail oldTopic = kmelia.getNodeHeader(id);
          if (!oldTopic.getRightsDependsOn().equals(rightsUsed) &&
              !rightsUsed.equals(NodeDetail.NO_RIGHTS_DEPENDENCY)) {
            // rights dependency have changed and  folder uses its own rights
            goToProfilesDefinition = true;
          }
        }
        kmelia.updateTopicHeader(topic, alertType);

        if (goToProfilesDefinition) {
          request.setAttribute("NodeId", id);
          destination = getDestination("ViewTopicProfiles", kmelia, request);
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

        KmeliaPublication kmeliaPublication;
        if (!cloneId.equals("-1")) {
          kmeliaPublication = kmelia.getPublication(cloneId);
          kmelia.setSessionClone(kmeliaPublication);
        }
        kmeliaPublication = kmelia.getSessionClone();

        request.setAttribute("Publication", kmeliaPublication);
        request.setAttribute("ValidationType", kmelia.getValidationType());
        request.setAttribute("Profile", kmelia.getProfile());
        request.setAttribute("VisiblePublicationId", pubDetail.getPK().getId());
        request.setAttribute("UserCanValidate", kmelia.isUserCanValidatePublication());
        request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
        request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());

        putXMLDisplayerIntoRequest(kmeliaPublication.getDetail(), kmelia, request);

        // Attachments area must be displayed or not ?
        request.setAttribute("AttachmentsEnabled", kmelia.isAttachmentsEnabled());

        destination = rootDestination + "clone.jsp";
      } else if ("ViewPublication".equals(function)) {
        String id = (String) request.getAttribute("ForcedId");
        if (!StringUtil.isDefined(id)) {
          id = request.getParameter("PubId");
          if (!StringUtil.isDefined(id)) {
            id = request.getParameter("Id");
            if (!StringUtil.isDefined(id)) {
              id = (String) request.getAttribute("PubId");
            }
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

        if (!kmaxMode) {
          boolean checkPath = StringUtil.getBooleanValue(request.getParameter("CheckPath"));
          boolean fromSearch = searchScope != null || kmelia.getSearchContext() != null;
          if (fromSearch || checkPath || KmeliaHelper.isSpecialFolder(kmelia.getCurrentFolderId())) {
            processPath(kmelia, id);
          } else {
            processPath(kmelia, null);
          }
        }

        KmeliaPublication kmeliaPublication;
        if (StringUtil.isDefined(id)) {
          kmeliaPublication = kmelia.getPublication(id, true);
          // Check user publication access
          PublicationAccessControl publicationAccessController = PublicationAccessControl.get();
          if (!publicationAccessController
              .isUserAuthorized(kmelia.getUserId(), kmeliaPublication.getPk())) {
            SilverLogger.getLogger(this).warn("Security alert from {0} with publication {1}",
                kmelia.getUserId(), id);
            return "/admin/jsp/accessForbidden.jsp";
          }
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
          destination = getDestination("ToUpdatePublicationHeader", kmelia, request);
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

          request.setAttribute("Publication", kmeliaPublication);
          request.setAttribute("PubId", id);
          request.setAttribute("UserCanValidate",
              kmelia.isUserCanValidatePublication() && kmelia.getSessionClone() == null);
          request.setAttribute("ValidationStep", kmelia.getValidationStep());
          request.setAttribute("ValidationType", kmelia.getValidationType());

          // check if user is writer with approval right (versioning case)
          request.setAttribute("WriterApproval", kmelia.isWriterApproval());
          request.setAttribute("NotificationAllowed", kmelia.isNotificationAllowed());

          // check is requested publication is an alias
          boolean alias = kmeliaPublication.isAlias();

          if (alias) {
            request.setAttribute("Profile", "user");
            request.setAttribute("TaxonomyOK", false);
            request.setAttribute("ValidatorsOK", false);
          } else {
            request.setAttribute("Profile", kmelia.getProfile());
            request.setAttribute("TaxonomyOK", kmelia.isPublicationTaxonomyOK());
            request.setAttribute("ValidatorsOK", kmelia.isPublicationValidatorsOK());
          }

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
          if (!alreadyOpened) {
            if (kmelia.openSingleAttachmentAutomatically() && !kmelia.isCurrentPublicationHaveContent()) {
              request.setAttribute(SINGLE_ATTACHMENT_URL_ATTR, kmelia.getSingleAttachmentURLOfCurrentPublication(alias));
            } else if (attachmentId != null) {
              request.setAttribute(SINGLE_ATTACHMENT_URL_ATTR, kmelia.getAttachmentURL(attachmentId, alias));
            } else if (documentId != null) {
              request.setAttribute(SINGLE_ATTACHMENT_URL_ATTR, kmelia.getAttachmentURL(documentId, alias));
            }
          }

          // Attachments area must be displayed or not ?
          request.setAttribute("AttachmentsEnabled", kmelia.isAttachmentsEnabled());

          // Last vistors area must be displayed or not ?
          final boolean lastVisitorsEnabled = kmelia.isLastVisitorsEnabled();
          request.setAttribute("LastVisitorsEnabled", lastVisitorsEnabled);
          if (lastVisitorsEnabled) {
            request.setAttribute("LastAccess", kmelia.getLastAccess(kmeliaPublication.getPk()));
          }
          request.setAttribute("PublicationRatingsAllowed", kmelia.isPublicationRatingAllowed());
          request.setAttribute("SeeAlsoEnabled", kmelia.isSeeAlsoEnabled());

          // Subscription management
          setupRequestForContributionManagementContext(request,
              highestSilverpeasUserRoleOnCurrentTopic, kmelia.getCurrentFolderPK(),
              kmeliaPublication.getDetail());

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

        destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
            "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("cut")) {
        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType)) {
          kmelia.cutTopic(objectId);
        } else {
          kmelia.cutPublication(objectId);
        }

        destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
            "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.equals("ReadingControl")) {
        PublicationDetail publication = kmelia.getSessionPublication().getDetail();
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        request.setAttribute("Publication", publication);
        request.setAttribute("UserIds", kmelia.getUserIdsOfTopic());

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
        request.setAttribute("ForcedId", kmelia.getSessionPublication().getId());
        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("ViewValidationSteps")) {
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        request.setAttribute("Publication", kmelia.getSessionPubliOrClone().getDetail());
        request.setAttribute("ValidationSteps", kmelia.getValidationSteps());

        request.setAttribute("Role", kmelia.getProfile());

        destination = rootDestination + "validationSteps.jsp";
      } else if ("ValidatePublication".equals(function)) {
        String pubId = kmelia.getSessionPublication().getDetail().getPK().getId();
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
          String from = request.getParameter("From");
          if (StringUtil.isDefined(from)) {
            destination = getDestination(from, kmelia, request);
          } else {
            destination = getDestination("ToUpdatePublicationHeader", kmelia, request);
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
        String browseInfo = kmelia.getSessionPathString();
        final String topicName = topic.getName(kmelia.getCurrentLanguage());
        if (browseInfo != null && !browseInfo.contains(topicName)) {
          browseInfo +=  " > " + topicName;
        }
        if (StringUtil.isDefined(browseInfo)) {
          browseInfo += " > ";
        }
        browseInfo += kmelia.getString("TopicWysiwyg");

        WysiwygRouting routing = new WysiwygRouting();
        WysiwygRouting.WysiwygRoutingContext context =
            WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController(kmelia)
                .withBrowseInfo(browseInfo)
                .withContributionId(
                    ContributionIdentifier.from(kmelia.getComponentId(), "Node_" + subTopicId, NODE))
                .withIndexation(false)
                .withContentLanguage(kmelia.getCurrentLanguage())
                .withComeBackUrl(URLUtil.getApplicationURL() +
                    URLUtil.getURL(kmelia.getSpaceId(), kmelia.getComponentId()) +
                    "FromTopicWysiwyg?Action=Search&Id=" + topicId + "&ChildId=" + subTopicId +
                    "&Profile=" + flag);

        destination = routing.getWysiwygEditorPath(context, request);
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
        String id = request.getParameter("Id");
        destination = rootDestination + "publicationViewOnly.jsp?Id=" + id;
      } else if (function.equals("ImportFileUpload")) {
        destination = processFormUpload(kmelia, request, rootDestination, false);
      } else if (function.equals("ImportFilesUpload")) {
        destination = processFormUpload(kmelia, request, rootDestination, true);
      } else if (function.equals("ExportAttachementsToPDF")) {
        String topicId = request.getParameter("TopicId");
        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> publicationsIds =
            kmelia.getAllVisiblePublicationsByTopic(topicId);
        request.setAttribute("selectedResultsWa", publicationsIds);
        request.setAttribute("RootPK", new NodePK(topicId, kmelia.getComponentId()));
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportPDF";
      } else if (function.equals("NewPublication")) {
        request.setAttribute("Action", "New");
        request.setAttribute("ExtraForm", kmelia.getXmlFormForPublications());

        PublicationDetail volatilePublication = kmelia.prepareNewPublication();
        request.setAttribute("VolatilePublication", volatilePublication);

        PagesContext extraFormPageContext = new PagesContext();
        extraFormPageContext.setUserId(kmelia.getUserId());
        extraFormPageContext.setComponentId(kmelia.getComponentId());
        extraFormPageContext.setObjectId(volatilePublication.getPK().getId());
        extraFormPageContext.setNodeId(kmelia.getCurrentFolderId());
        extraFormPageContext.setLanguage(kmelia.getLanguage());
        request.setAttribute("ExtraFormPageContext", extraFormPageContext);

        destination = getDestination("ToPublicationHeader", kmelia, request);
      } else if (function.equals("ManageSubscriptions")) {
        destination = kmelia.manageSubscriptions();
      } else if (function.equals("AddPublication")) {
        List<FileItem> parameters = request.getFileItems();

        // create publication
        String positions = FileUploadUtil.getParameter(parameters, "KmeliaPubPositions");
        PdcClassificationEntity withClassification =
            PdcClassificationEntity.undefinedClassification();
        if (StringUtil.isDefined(positions)) {
          withClassification = PdcClassificationEntity.fromJSON(positions);
        }
        PublicationDetail pubDetail = getPublicationDetail(parameters, kmelia);
        String newPubId = kmelia.createPublication(pubDetail, withClassification);

        if(kmelia.isReminderUsed()) {
          PublicationDetail pubDetailCreated = kmelia.getPublicationDetail(newPubId);
          kmelia.addPublicationReminder(pubDetailCreated, parameters);
        }

        // create thumbnail if exists
        boolean newThumbnail = ThumbnailController
            .processThumbnail(new ResourceReference(newPubId, kmelia.getComponentId()),
                parameters);

        //process files
        Collection<UploadedFile> attachments = request.getUploadedFiles();
        kmelia.addUploadedFilesToPublication(attachments, pubDetail);

        String volatileId = FileUploadUtil.getParameter(parameters, "KmeliaPubVolatileId");
        if (StringUtil.isDefined(volatileId)) {
          //process extra form
          kmelia.saveXMLFormToPublication(pubDetail, parameters, false);
        }

        // force indexation to add thumbnail and attachments to publication index
        if ((newThumbnail || !attachments.isEmpty()) && pubDetail.isIndexable()) {
          kmelia.getPublicationService().createIndex(pubDetail.getPK());
        }

        request.setAttribute("PubId", newPubId);
        processPath(kmelia, newPubId);
        StringBuffer requestURI = request.getRequestURL();
        destination = requestURI.substring(0, requestURI.indexOf("AddPublication")) +
            "ViewPublication?PubId=" + newPubId;
      } else if ("UpdatePublication".equals(function)) {
        List<FileItem> parameters = request.getFileItems();

        PublicationDetail pubDetail = getPublicationDetail(parameters, kmelia);
        String pubId = pubDetail.getPK().getId();
        ThumbnailController.processThumbnail(new ResourceReference(pubId, kmelia.getComponentId()),
            parameters);

        kmelia.updatePublication(pubDetail);

        if(kmelia.isReminderUsed()) {
          kmelia.updatePublicationReminder(pubId, parameters);
        }

        if (kmelia.getSessionClone() != null) {
          destination = getDestination("ViewClone", kmelia, request);
        } else {
          request.setAttribute("PubId", pubId);
          request.setAttribute("CheckPath", "1");
          destination = getDestination("ViewPublication", kmelia, request);
        }
      } else if (function.equals("SelectValidator")) {
        String formElementName = request.getParameter("FormElementName");
        String formElementId = request.getParameter("FormElementId");
        String folderId = request.getParameter("FolderId");
        destination = kmelia.initUPToSelectValidator(formElementName, formElementId, folderId);
      } else if (function.equals("PublicationPaths")) {
        KmeliaPublication publication = kmelia.getSessionPublication();
        request.setAttribute("Publication", publication);
        request.setAttribute("LinkedPathString", kmelia.getSessionPath());
        Collection<Location> locations = kmelia.getPublicationLocations();
        request.setAttribute("Locations", locations);
        if (toolboxMode) {
          request.setAttribute("Topics", kmelia.getAllTopics());
        } else {
          request.setAttribute("Components", kmelia.getComponents(locations));
        }

        destination = rootDestination + "publicationPaths.jsp";
      } else if (function.equals("SetPath")) {
        final String[] topics = request.getParameterValues("topicChoice");
        final List<String> loadedComponentIds = request.getParameterAsList("LoadedComponentIds");
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; topics != null && i < topics.length; i++) {
          String topicId = topics[i];
          StringTokenizer tokenizer = new StringTokenizer(topicId, ",");
          String nodeId = tokenizer.nextToken();
          String instanceId = tokenizer.nextToken();

          Location location = new Location(nodeId, instanceId);
          location.setAsAlias(kmelia.getUserId());
          locations.add(location);
        }

        // Tous les composants ayant un alias n'ont pas forcément été chargés
        Collection<Location> oldLocations = kmelia.getPublicationAliases();
        for (Location oldLocation : oldLocations) {
          if (!loadedComponentIds.contains(oldLocation.getInstanceId()) && oldLocation.isAlias()) {
            // le composant de l'alias n'a pas été chargé
            locations.add(oldLocation);
          }
        }

        kmelia.setPublicationAliases(locations);

        destination = getDestination("ViewPublication", kmelia, request);
      } else if (function.equals("ShowAliasTree")) {
        String componentId = request.getParameter("ComponentId");

        request.setAttribute("Tree", kmelia.getAliasTreeview(componentId));
        request.setAttribute("Aliases", kmelia.getPublicationAliases());

        destination = rootDestination + "treeview4PublicationPaths.jsp";
      } else if ("ToAddLinksToPublication".equals(function)) {

        PublicationPK publicationPK = kmelia.getSessionPublication().getPk();

        // put into session the current list of linked publications (get up-to-date data)
        List<PublicationLink> links =
            kmelia.getKmeliaService().getCompletePublication(publicationPK).getLinkList();
        HashSet<String> linkedList = new HashSet<>(links.size());
        for (PublicationLink link : links) {
          linkedList.add(link.getTarget().getId() + "-" + link.getTarget().getComponentInstanceId());
        }
        request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, linkedList);

        request.setAttribute("PublicationPK", publicationPK);
        destination = rootDestination + "publicationLinksManager.jsp";

      } else if (function.equals("AddLinksToPublication")) {
        String id = request.getParameter("PubId");

        //noinspection unchecked
        HashSet<String> list =
            (HashSet) request.getSession().getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);

        int nb = kmelia.addPublicationsToLink(id, list);

        request.setAttribute("NbLinks", Integer.toString(nb));
        request.setAttribute("PublicationPK", new PublicationPK(id, kmelia.getComponentId()));

        destination = rootDestination + "publicationLinksManager.jsp";
      } else if (function.equals("ExportTopic")) {
        String topicId = request.getParameter("TopicId");
        boolean exportFullApp = !StringUtil.isDefined(topicId) || NodePK.ROOT_NODE_ID.
            equals(topicId);
        if (kmaxMode) {
          if (exportFullApp) {
            destination = getDestination("KmaxExportComponent", kmelia, request);
          } else {
            destination = getDestination("KmaxExportPublications", kmelia, request);
          }
        } else {
          // build an exploitable list by importExportPeas
          final List<WAAttributeValuePair> publicationsIds;
          if (exportFullApp) {
            publicationsIds = kmelia.getAllVisiblePublications();
          } else {
            publicationsIds = kmelia.getAllVisiblePublicationsByTopic(topicId);
          }
          request.setAttribute("selectedResultsWa", publicationsIds);
          request.setAttribute("RootPK", new NodePK(topicId, kmelia.getComponentId()));
          // Go to importExportPeas
          destination = "/RimportExportPeas/jsp/SelectExportMode";
        }
      } else if (function.equals("ExportPublications")) {
        String selectedIds = request.getParameter("SelectedIds");
        String notSelectedIds = request.getParameter("NotSelectedIds");
        List<PublicationPK> pks = kmelia.processSelectedPublicationIds(selectedIds, notSelectedIds);

        List<WAAttributeValuePair> publicationIds = new ArrayList<>();
        for (PublicationPK pk : pks) {
          publicationIds.add(new WAAttributeValuePair(pk.getId(), pk.getInstanceId()));
        }
        request.setAttribute("selectedResultsWa", publicationIds);
        request.setAttribute("RootPK",
            new NodePK(kmelia.getCurrentFolderId(), kmelia.getComponentId()));
        kmelia.resetSelectedPublicationPKs();
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/SelectExportMode";
      } else if (function.equals("ToPubliContent")) {
        PublicationDetail publicationDetail = kmelia.getSessionPubliOrClone().getDetail();
        if (WysiwygController.haveGotWysiwyg(kmelia.getComponentId(),
            publicationDetail.getPK().getId(),
            kmelia.getCurrentLanguage())) {

          destination = getDestination("ToWysiwyg", kmelia, request);
        } else {
          String infoId = publicationDetail.getInfoId();
          if (infoId == null || "0".equals(infoId)) {
            List<String> usedModels = kmelia.getModelUsed();
            if (usedModels.size() == 1) {
              String modelId = usedModels.get(0);
              if ("WYSIWYG".equals(modelId)) {
                // Wysiwyg content
                destination = getDestination("ToWysiwyg", kmelia, request);
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

        destination = rootDestination + "modelsList.jsp";
      } else if (function.equals("ModelUsed")) {
        request.setAttribute("XMLForms", kmelia.getForms());

        Collection<String> modelUsed = kmelia.getModelUsed();
        request.setAttribute("ModelUsed", modelUsed);

        destination = rootDestination + "modelUsedList.jsp";
      } else if (function.equals("SelectModel")) {
        kmelia.setModelUsed(request.getParameterValues("modelChoice"));
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if ("ChangeTemplate".equals(function)) {
        kmelia.removePublicationContent();
        destination = getDestination("ToPubliContent", kmelia, request);
      } else if ("ToWysiwyg".equals(function)) {
        if (kmelia.isCloneNeeded()) {
          kmelia.clonePublication();
        }
        // put current publication
        PublicationDetail publication = kmelia.getSessionPubliOrClone().getDetail();

        String browseInfo;
        if (kmaxMode) {
          browseInfo = publication.getName();
        } else {
          browseInfo = kmelia.getSessionPathString();
          if (StringUtil.isDefined(browseInfo)) {
            browseInfo += " > ";
          }
          browseInfo += publication.getName(kmelia.getCurrentLanguage());
        }

        // Subscription management
        setupRequestForContributionManagementContext(request,
            highestSilverpeasUserRoleOnCurrentTopic, kmelia.getCurrentFolderPK(), publication);

        WysiwygRouting routing = new WysiwygRouting();
        WysiwygRouting.WysiwygRoutingContext context =
            WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController(kmelia)
                .withBrowseInfo(browseInfo)
                .withContributionId(
                    ContributionIdentifier.from(kmelia.getComponentId(), publication.getId(), publication.getContributionType()))
                .withContentLanguage(checkLanguage(kmelia, publication))
                .withComeBackUrl(
                    URLUtil.getApplicationURL() + kmelia.getComponentUrl() + "FromWysiwyg?PubId=" +
                        publication.getId())
                .withIndexation(false);
        destination = routing.getWysiwygEditorPath(context, request);
      } else if ("FromWysiwyg".equals(function)) {
        String id = request.getParameter("PubId");
        if (kmelia.getSessionClone() != null &&
            id.equals(kmelia.getSessionClone().getDetail().getPK().
                getId())) {
          destination = getDestination("ViewClone", kmelia, request);
        } else {
          destination = getDestination("ViewPublication", kmelia, request);
        }
      } else if (function.equals("GoToXMLForm")) {
        String xmlFormName = (String) request.getAttribute("Name");
        if (!StringUtil.isDefined(xmlFormName)) {
          xmlFormName = request.getParameter("Name");
        }
        setXMLUpdateForm(request, kmelia, xmlFormName);
        // put current publication
        request.setAttribute("CurrentPublicationDetail", kmelia.getSessionPubliOrClone().
            getDetail());
        // template can be changed only if current topic is using at least two templates
        setTemplatesUsedIntoRequest(kmelia, request);
        @SuppressWarnings("unchecked") Collection<PublicationTemplate> templates =
            (Collection<PublicationTemplate>) request.getAttribute("XMLForms");
        boolean wysiwygUsable = (Boolean) request.getAttribute("WysiwygValid");
        request.setAttribute("IsChangingTemplateAllowed",
            templates.size() >= 2 || (!templates.isEmpty() && wysiwygUsable));

        setupRequestForContributionManagementContext(request,
            highestSilverpeasUserRoleOnCurrentTopic, kmelia.getCurrentFolderPK(),
            kmelia.getSessionPubliOrClone().getDetail());


        destination = rootDestination + "xmlForm.jsp";
      } else if (function.equals("UpdateXMLForm")) {
        List<FileItem> items = request.getFileItems();

        kmelia.saveXMLForm(items, true);

        if (kmelia.getSessionClone() != null) {
          destination = getDestination("ViewClone", kmelia, request);
        } else if (kmaxMode) {
          destination = getDestination("ViewAttachments", kmelia, request);
        } else {
          destination = getDestination("ViewPublication", kmelia, request);
        }
      } else if (function.startsWith("ToOrderPublications")) {
        List<KmeliaPublication> publications = kmelia.getSessionPublicationsList();

        request.setAttribute("Publications", publications);
        request.setAttribute("Path", kmelia.getSessionPath());

        destination = rootDestination + "orderPublications.jsp";
      } else if (function.startsWith("OrderPublications")) {
        String sortedIds = request.getParameter("sortedIds");

        StringTokenizer tokenizer = new StringTokenizer(sortedIds, ",");
        List<String> ids = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
          ids.add(tokenizer.nextToken());
        }
        kmelia.orderPublications(ids);

        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else if (function.equals("ToOrderTopics")) {
        String id = request.getParameter("Id");
        if (!SilverpeasRole.ADMIN.isInRole(kmelia.getUserTopicProfile(id))) {
          destination = "/admin/jsp/accessForbidden.jsp";
        } else {
          TopicDetail topic = kmelia.getTopic(id);
          request.setAttribute("Nodes", topic.getNodeDetail().getChildrenDetails());
          destination = rootDestination + "orderTopics.jsp";
        }
      } else if ("ViewTopicProfiles".equals(function)) {
        String role = request.getParameter("Role");
        if (!StringUtil.isDefined(role)) {
          role = SilverpeasRole.ADMIN.toString();
        }

        String id = request.getParameter("NodeId");
        if (!StringUtil.isDefined(id)) {
          id = (String) request.getAttribute("NodeId");
        }
        if (!(kmelia.isTopicAdmin(id) || kmelia.isUserComponentAdmin())) {
          SilverLogger.getLogger(this).warn("Security alert from {0}", kmelia.getUserId());
          return "/admin/jsp/accessForbidden.jsp";
        }
        request.setAttribute("Profiles", kmelia.getTopicProfiles(id));
        NodeDetail topic = kmelia.getNodeHeader(id);
        ProfileInst profile;
        if (topic.haveInheritedRights()) {
          profile = kmelia.getTopicProfile(role, topic.getRightsDependsOn());

          request.setAttribute("RightsDependsOn", "AnotherTopic");
        } else if (topic.haveLocalRights()) {
          profile = kmelia.getTopicProfile(role, topic.getRightsDependsOn());

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
        String[] userIds =
            StringUtil.split(request.getParameter("UserPanelCurrentUserIds"), ',');
        String[] groupIds =
            StringUtil.split(request.getParameter("UserPanelCurrentGroupIds"), ',');
        try {
          kmelia.initUserPanelForTopicProfile(role, nodeId, groupIds, userIds);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
        destination = Selection.getSelectionURL();
      } else if (function.equals("TopicProfileSetUsersAndGroups")) {
        String role = request.getParameter("Role");
        String nodeId = request.getParameter("NodeId");
        String[] userIds =
            StringUtil.split(request.getParameter("roleItems" + "UserPanelCurrentUserIds"), ',');
        String[] groupIds =
            StringUtil.split(request.getParameter("roleItems" + "UserPanelCurrentGroupIds"), ',');

        if (kmelia.isTopicAdmin(nodeId) || kmelia.isUserComponentAdmin()) {
          kmelia.updateTopicRole(role, nodeId, groupIds, userIds);
        } else {
          SilverLogger.getLogger(this).warn("Security alert from {0}", kmelia.getUserId());
        }
        destination = getDestination("ViewTopicProfiles", kmelia, request);
      } else if (function.equals("CloseWindow")) {
        destination = rootDestination + "closeWindow.jsp";
      }
      /**
       * *************************
       * Kmax mode ***********************
       */
      else if (function.equals("KmaxMain")) {
        destination = rootDestination + "kmax.jsp?Action=KmaxView&Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxAxisManager")) {
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxViewAxis&Profile=" +
            kmelia.getProfile();
      } else if (function.equals("KmaxAddAxis")) {
        String newAxisName = request.getParameter("Name");
        String newAxisDescription = request.getParameter("Description");
        NodeDetail axis =
            new NodeDetail("-1", newAxisName, newAxisDescription, 0, "X");
        axis.setCreationDate(new Date());
        axis.setCreatorId(kmelia.getUserId());
        // I18N
        I18NHelper.setI18NInfo(axis, request);
        kmelia.addAxis(axis);

        request.setAttribute("urlToReload", "KmaxAxisManager");
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.equals("KmaxUpdateAxis")) {
        String axisId = request.getParameter("AxisId");
        String newAxisName = request.getParameter("AxisName");
        String newAxisDescription = request.getParameter("AxisDescription");
        NodeDetail axis =
            new NodeDetail(axisId, newAxisName, newAxisDescription, 0, "X");
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
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxManageAxis&Profile=" +
            kmelia.getProfile() + "&AxisId=" + axisId;
      } else if (function.equals("KmaxManagePosition")) {
        String positionId = request.getParameter("PositionId");
        String translation = request.getParameter("Translation");
        request.setAttribute("Translation", translation);
        destination = rootDestination + "kmax_axisManager.jsp?Action=KmaxManagePosition&Profile=" +
            kmelia.getProfile() + "&PositionId=" + positionId;
      } else if (function.equals("KmaxAddPosition")) {
        String axisId = request.getParameter("AxisId");
        String newPositionName = request.getParameter("Name");
        String newPositionDescription = request.getParameter("Description");
        String translation = request.getParameter("Translation");
        NodeDetail position =
            new NodeDetail("toDefine", newPositionName, newPositionDescription, 0, "X");
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
        NodeDetail position =
            new NodeDetail(positionId, positionName, positionDescription, 0,"X");
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

        destination =
            rootDestination + "kmax.jsp?Action=KmaxViewUnbalanced&Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxViewBasket")) {
        TopicDetail basket = kmelia.getTopic("1");
        List<KmeliaPublication> publications = (List<KmeliaPublication>) basket.
            getKmeliaPublications();
        kmelia.setSessionPublicationsList(publications);
        kmelia.orderPubs();

        destination =
            rootDestination + "kmax.jsp?Action=KmaxViewBasket&Profile=" + kmelia.getProfile();

      } else if (function.equals("KmaxViewToValidate")) {
        destination =
            rootDestination + "kmax.jsp?Action=KmaxViewToValidate&Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxSearch")) {
        String axisValuesStr = request.getParameter("SearchCombination");
        if (!StringUtil.isDefined(axisValuesStr)) {
          axisValuesStr = (String) request.getAttribute("SearchCombination");
        }
        String timeCriteria = request.getParameter("TimeCriteria");

        List<String> combination = kmelia.getCombination(axisValuesStr);
        if (StringUtil.isDefined(timeCriteria) && !"X".equals(timeCriteria)) {
          kmelia.search(combination, Integer.parseInt(timeCriteria));
        } else {
          kmelia.search(combination);
        }

        kmelia.setIndexOfFirstPubToDisplay("0");
        kmelia.orderPubs();
        kmelia.setSessionCombination(combination);
        kmelia.setSessionTimeCriteria(timeCriteria);

        destination =
            rootDestination + "kmax.jsp?Action=KmaxSearchResult&Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxSearchResult")) {
        if (kmelia.getSessionCombination() == null) {
          destination = getDestination("KmaxMain", kmelia, request);
        } else {
          destination =
              rootDestination + "kmax.jsp?Action=KmaxSearchResult&Profile=" + kmelia.getProfile();
        }
      } else if (function.equals("KmaxViewCombination")) {
        request.setAttribute("CurrentCombination", kmelia.getCurrentCombination());
        destination = rootDestination + "kmax_viewCombination.jsp?Profile=" + kmelia.getProfile();
      } else if (function.equals("KmaxAddCoordinate")) {
        String pubId = request.getParameter("PubId");
        String axisValuesStr = request.getParameter("SearchCombination");
        StringTokenizer st = new StringTokenizer(axisValuesStr, ",");
        List<String> combination = new ArrayList<>();
        String axisValue;
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
          LocalizationBundle timeSettings = ResourceLocator.getLocalizationBundle(
              "org.silverpeas.kmelia.multilang.timeAxisBundle");
          if (kmelia.getSessionTimeCriteria().equals("X")) {
            timeCriteria = null;
          } else {
            String localizedCriteria = timeSettings.getString(kmelia.getSessionTimeCriteria());
            if (localizedCriteria == null || localizedCriteria.trim().isEmpty()) {
              localizedCriteria = "";
            }
            timeCriteria = "<b>" + kmelia.getString("TimeAxis") + "</b> > " + localizedCriteria;
          }
        }
        request.setAttribute("selectedResultsWa", publicationsIds);
        request.setAttribute("Combination", combination);
        request.setAttribute("TimeCriteria", timeCriteria);
        // Go to importExportPeas
        destination = "/RimportExportPeas/jsp/KmaxExportPublications";
      } else if ("statistics".equals(function)) {
        destination =
            rootDestination + STATISTIC_REQUEST_HANDLER.handleRequest(request, function, kmelia);
      } else if ("statSelectionGroup".equals(function)) {
        destination = STATISTIC_REQUEST_HANDLER.handleRequest(request, function, kmelia);
      } else if ("SetPublicationValidator".equals(function)) {
        String userIds = request.getParameter("ValideurId");
        kmelia.setPublicationValidator(userIds);
        destination = getDestination("ViewPublication", kmelia, request);
      } else if ("ToUpdatePublications".equals(function)) {
        final String selectedIds = request.getParameter("SelectedIds");
        final String notSelectedIds = request.getParameter("NotSelectedIds");
        final List<KmeliaPublication> authorizedPublications = kmelia.getPublicationsForModification(
            kmelia.processSelectedPublicationIds(selectedIds, notSelectedIds))
            .stream()
            .map(p -> p.getSecond() != null ? p.getSecond() : p.getFirst())
            .filter(p -> Objects.equals(p.getComponentInstanceId(), kmelia.getComponentId()))
            .collect(Collectors.toList());
        setupRequestForContributionBatchManagementContext(request, kmelia, authorizedPublications);
        request.setAttribute("Form", kmelia.getXmlFormForPublications());
        request.setAttribute("Language", kmelia.getLanguage());
        request.setAttribute("NumberOfSelectedPublications", authorizedPublications.size());
        destination = rootDestination + "updatePublicationsContent.jsp";
      } else if ("UpdatePublications".equals(function)) {
        List<FileItem> items = request.getFileItems();
        kmelia.saveXMLFormOfSelectedPublications(items);
        destination = getDestination("GoToCurrentTopic", kmelia, request);
      } else {
        destination = rootDestination + function;
      }

      if (profileError) {
        destination = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
      }
    } catch (Exception exceAll) {
      request.setAttribute("javax.servlet.jsp.jspException", exceAll);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private String processDocumentNotFoundException(final HttpRequest request,
      final KmeliaSessionController kmelia, final Exception e) {
    final String destination;
    if (e instanceof AliasOnOtherKmeliaException) {
      final AliasOnOtherKmeliaException aliasException = (AliasOnOtherKmeliaException) e;
      destination = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, aliasException.pubId,
          aliasException.aliasLocation.getInstanceId(), false);
    } else {
      SilverLogger.getLogger(this).error("Document not found. {0}", new String[]{e.getMessage()}, e);
      destination = getDocumentNotFoundDestination(kmelia, request);
    }
    return destination;
  }

  @Override
  protected boolean checkUserAuthorization(final String function,
      final KmeliaSessionController kmelia) {
    KmeliaActionAccessController actionAccessController =
        ServiceProvider.getSingleton(KmeliaActionAccessController.class);
    return actionAccessController.hasRightAccess(function, kmelia.getHighestSilverpeasUserRole());
  }

  private String getDocumentNotFoundDestination(KmeliaSessionController kmelia,
      HttpServletRequest request) {
    request.setAttribute("ComponentId", kmelia.getComponentId());
    return "/admin/jsp/documentNotFound.jsp";
  }

  private PublicationDetail getPublicationDetail(List<FileItem> parameters,
      KmeliaSessionController kmelia) throws Exception {
    String id = FileUploadUtil.getParameter(parameters, "KmeliaPubId");
    String status = FileUploadUtil.getParameter(parameters, "KmeliaPubStatus");
    String name = FileUploadUtil.getParameter(parameters, "KmeliaPubName");
    String description = FileUploadUtil.getParameter(parameters, "KmeliaPubDescription");
    String keywords = FileUploadUtil.getParameter(parameters, "KmeliaPubKeywords");
    String beginDate = FileUploadUtil.getParameter(parameters, "KmeliaPubBeginDate");
    String endDate = FileUploadUtil.getParameter(parameters, "KmeliaPubEndDate");
    String version = FileUploadUtil.getParameter(parameters, "KmeliaPubVersion");
    String importance = FileUploadUtil.getParameter(parameters, "KmeliaPubImportance");
    String beginHour = FileUploadUtil.getParameter(parameters, "KmeliaPubBeginHour");
    String endHour = FileUploadUtil.getParameter(parameters, "KmeliaPubEndHour");
    String author = FileUploadUtil.getParameter(parameters, "KmeliaPubAuthor");
    String targetValidatorId = FileUploadUtil.getParameter(parameters, "KmeliaPubValideurId");
    String tempId = FileUploadUtil.getParameter(parameters, "KmeliaPubTempId");
    String infoId = FileUploadUtil.getParameter(parameters, "KmeliaPubInfoId");
    String draftOutDate = FileUploadUtil.getParameter(parameters, "KmeliaPubDraftOutDate");

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

    String pubId = ResourceReference.UNKNOWN_ID;
    if (StringUtil.isDefined(id)) {
      pubId = id;
    }

    PublicationDetail pubDetail = PublicationDetail.builder(kmelia.getLanguage())
        .setPk(new PublicationPK(pubId, kmelia.getSpaceId(), kmelia.getComponentId()))
        .setNameAndDescription(name, description)
        .setBeginDateTime(jBeginDate, beginHour)
        .setEndDateTime(jEndDate, endHour)
        .setImportance(Integer.parseInt(importance))
        .setVersion(version)
        .setKeywords(keywords)
        .setContentPagePath("")
        .build();

    pubDetail.setAuthor(author);
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

  /**
   * Process Form Upload for publications import
   * @param kmeliaScc
   * @param request
   * @param routeDestination
   * @return destination
   */
  private String processFormUpload(KmeliaSessionController kmeliaScc, HttpRequest request,
      String routeDestination, boolean isMassiveMode) {
    String destination = "";
    String topicId = "";
    String importMode;
    boolean draftMode = false;
    String logicalName;
    String message;
    boolean error = false;

    String tempFolderName;
    String tempFolderPath = null;

    String fileType;
    long fileSize;
    long processStart = new Date().getTime();
    LocalizationBundle attachmentResourceLocator =
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.attachment.multilang.attachment",
            kmeliaScc.getLanguage());
    FileItem fileItem;
    int versionType = DocumentVersion.TYPE_DEFAULT_VERSION;

    try {
      List<FileItem> items = request.getFileItems();
      topicId = FileUploadUtil.getParameter(items, "topicId");
      importMode = FileUploadUtil.getParameter(items, "opt_importmode");

      String sVersionType = FileUploadUtil.getParameter(items, "opt_versiontype");
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
          }

          logicalName = logicalName
              .substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.length());

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
          tempFolderPath = FileRepositoryManager.getAbsolutePath(kmeliaScc.getComponentId()) +
              ResourceLocator.getGeneralSettingBundle().getString("RepositoryTypeTemp") + File.separator +
              tempFolderName;
          if (!new File(tempFolderPath).exists()) {
            FileRepositoryManager.createAbsolutePath(kmeliaScc.getComponentId(),
                ResourceLocator.getGeneralSettingBundle().getString("RepositoryTypeTemp") + File.separator +
                    tempFolderName);
          }

          // Creation of the file in the temp folder
          File fileUploaded = new File(
              FileRepositoryManager.getAbsolutePath(kmeliaScc.getComponentId()) +
                  ResourceLocator.getGeneralSettingBundle().getString("RepositoryTypeTemp") + File.separator +
                  tempFolderName + File.separator + logicalName);
          fileItem.write(fileUploaded);

          // Is a real file ?
          if (fileSize <= 0L) {
            // File access failed
            message = attachmentResourceLocator.getString("liaisonInaccessible");
            error = true;
          } else {
            // Import !!
            ImportReport importReport =
                kmeliaScc.importFile(fileUploaded, importMode, draftMode, versionType);
            long processDuration = new Date().getTime() - processStart;

            // Compute nbPublication created
            int nbPublication = kmeliaScc.getNbPublicationImported(importReport);

            // nbFiles imported (only in unitary Import mode)
            int nbFiles = importReport.getNbFilesProcessed();

            // Title for popup report
            String importModeTitle;
            if (importMode.equals(KmeliaSessionController.UNITARY_IMPORT_MODE)) {
              importModeTitle = kmeliaScc.getString("kmelia.ImportModeUnitaireTitre");
            } else {
              importModeTitle = kmeliaScc.getString("kmelia.ImportModeMassifTitre");
            }
            message = kmeliaScc.getErrorMessageImportation(importReport, importMode);

            if (message != null && importMode.equals(KmeliaSessionController.UNITARY_IMPORT_MODE)) {
              error = true;
            }

            request.setAttribute("NbPublication", nbPublication);
            request.setAttribute("NbFiles", nbFiles);
            request.setAttribute("ProcessDuration",
                FileRepositoryManager.formatFileUploadTime(processDuration));
            request.setAttribute("ImportMode", importMode);
            request.setAttribute("DraftMode", draftMode);
            request.setAttribute("Title", importModeTitle);
            request.setAttribute("Context", URLUtil.getApplicationURL());
            request.setAttribute("Message", message);

            destination = routeDestination + "reportImportFiles.jsp";

            String componentId = kmeliaScc.getComponentId();
            if (kmeliaScc.isDefaultClassificationModifiable(topicId, componentId)) {
              List<PublicationDetail> publicationDetails =
                  kmeliaScc.getListPublicationImported(importReport, importMode);
              if (publicationDetails.size() > 0) {
                request.setAttribute("PublicationsDetails", publicationDetails);
                destination = routeDestination + "validateImportedFilesClassification.jsp";
              }
            }
          }

          // Delete temp folder
          FileFolderManager.deleteFolder(tempFolderPath);

        } else {
          // the field did not contain a file
          message = attachmentResourceLocator.getString("liaisonInaccessible");
          error = true;
        }
      } else {
        // the field did not contain a file
        message = attachmentResourceLocator.getString("liaisonInaccessible");
        error = true;
      }

      if (error) {
        request.setAttribute("Message", message);
        request.setAttribute("TopicId", topicId);
        destination = routeDestination + "importOneFile.jsp";
        if (isMassiveMode) {
          destination = routeDestination + "importMultiFiles.jsp";
        }
      }
    } catch (Exception e) {
      String exMessage = SilverpeasTransverseErrorUtil.performExceptionMessage(e, kmeliaScc.
          getLanguage());
      if (StringUtil.isNotDefined(exMessage)) {
        request.setAttribute("Message", e.getMessage());
      }
      // Other exception
      request.setAttribute("TopicId", topicId);
      destination = routeDestination + "importOneFile.jsp";
      if (isMassiveMode) {
        destination = routeDestination + "importMultiFiles.jsp";
      }

      SilverLogger.getLogger(this).error("Attachment upload failure", e);
    } finally {
      if (tempFolderPath != null) {
        FileFolderManager.deleteFolder(tempFolderPath);
      }
    }
    return destination;
  }

  private void processPath(KmeliaSessionController kmeliaSC, String id)
      throws AliasOnOtherKmeliaException {
    if (!kmeliaSC.isKmaxMode()) {
      NodePK pk;
      if (!StringUtil.isDefined(id)) {
        pk = kmeliaSC.getCurrentFolderPK();
      } else {
        // get best location on the component instance
        pk = kmeliaSC.getBestAllowedPublicationFather(id);
        if (!pk.getInstanceId().equals(kmeliaSC.getComponentId())) {
          // the user is not allowed to access the publication on this instance even as an alias
          throw new AliasOnOtherKmeliaException(id, pk);
        }
        kmeliaSC.getBestTopicDetailsOfPublication(id);
      }

      String nodeId = pk.getId();
      /*if (kmeliaSC.getSearchContext() != null) {
          nodeId = kmeliaSC.getSearchContext().getNode().getId();
      }*/
      Collection<NodeDetail> pathColl = kmeliaSC.getTopicPath(nodeId);
      String linkedPathString = kmeliaSC.displayPath(pathColl, true, 3);
      String pathString = kmeliaSC.displayPath(pathColl, false, 3);
      kmeliaSC.setSessionPath(linkedPathString);
      kmeliaSC.setSessionPathString(pathString);
    }
  }

  private void putXMLDisplayerIntoRequest(PublicationDetail pubDetail,
      KmeliaSessionController kmelia, HttpServletRequest request)
      throws PublicationTemplateException, FormException {
    String infoId = pubDetail.getInfoId();
    String pubId = pubDetail.getPK().getId();
    if (!StringUtil.isInteger(infoId)) {
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager()
              .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + infoId);
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

  private void setXMLUpdateForm(HttpRequest request, KmeliaSessionController kmelia,
      String xmlFormName) throws PublicationTemplateException, FormException {
    PublicationDetail pubDetail = kmelia.getSessionPubliOrClone().getDetail();
    String pubId = pubDetail.getPK().getId();

    String xmlFormShortName;
    if (!StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName = pubDetail.getInfoId();
      xmlFormName = null;
    } else {
      xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      // register xmlForm to publication
      getPublicationTemplateManager()
          .addDynamicPublicationTemplate(kmelia.getComponentId() + ":" + xmlFormShortName,
              xmlFormName);
    }
    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
        .getPublicationTemplate(kmelia.getComponentId() + ':' + xmlFormShortName, xmlFormName);
    Form formUpdate = pubTemplate.getUpdateForm();
    RecordSet recordSet = pubTemplate.getRecordSet();

    // get displayed language
    String language = checkLanguage(kmelia, pubDetail);

    DataRecord data = recordSet.getRecord(pubId, language);
    if (data == null || (language != null && !language.equals(data.getLanguage()))) {
      // This publication haven't got any content at all or for requested language
      data = recordSet.getEmptyRecord();
      data.setId(pubId);
      data.setLanguage(language);
    }

    request.setAttribute("Form", formUpdate);
    request.setAttribute("Data", data);
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

  private String checkLanguage(KmeliaSessionController kmelia, PublicationDetail pubDetail) {
    return pubDetail.getLanguageToDisplay(kmelia.getCurrentLanguage());
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  public PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private void setTemplatesUsedIntoRequest(KmeliaSessionController kmelia,
      HttpServletRequest request) {
    Collection<String> modelUsed = kmelia.getModelUsed();
    Collection<PublicationTemplate> listModelXml = new ArrayList<>();
    List<PublicationTemplate> templates = kmelia.getForms();
    // recherche de la liste des modèles utilisables
    for (PublicationTemplate xmlForm : templates) {
      // recherche si le modèle est dans la liste
      if (modelUsed.contains(xmlForm.getFileName())) {
        listModelXml.add(xmlForm);
      }
    }

    request.setAttribute("XMLForms", listModelXml);

    // recherche si modele Wysiwyg utilisable
    boolean wysiwygValid = false;
    if (modelUsed.contains("WYSIWYG")) {
      wysiwygValid = true;
    }
    request.setAttribute("WysiwygValid", wysiwygValid);

    // s'il n'y a pas de modèles selectionnés, les présenter tous
    if (listModelXml.isEmpty() && !wysiwygValid) {
      request.setAttribute("XMLForms", templates);
      request.setAttribute("WysiwygValid", Boolean.TRUE);
    }
  }

  /**
   * Setup the request to manager some behaviors around validation of modifications.
   * @param request the current request.
   * @param highestSilverpeasUserRoleOnCurrentTopic the highest role the user has on the current
   * folder.
   * @param currentFolderPK the primary key of the current folder.
   * @param publication the current handled publication.
   */
  private void setupRequestForContributionManagementContext(HttpRequest request,
      SilverpeasRole highestSilverpeasUserRoleOnCurrentTopic, NodePK currentFolderPK,
      PublicationDetail publication) {
    ContributionStatus statusBeforeSave = publication.isValid() ? ContributionStatus.VALIDATED :
        ContributionStatus.from(publication.getStatus());
    ContributionStatus statusAfterSave = ContributionStatus.UNKNOWN;
    if (highestSilverpeasUserRoleOnCurrentTopic.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
      statusAfterSave = ContributionStatus.VALIDATED;
    }
    final String componentInstanceId = currentFolderPK.getComponentInstanceId();
    final PublicationPK publicationPK = new PublicationPK(publication.getId(), componentInstanceId);
    final SubscriptionResource resource = publication.isAlias() ?
        PublicationAliasSubscriptionResource.from(publicationPK) :
        PublicationSubscriptionResource.from(publicationPK);
    request.setAttribute("contributionManagementContext", ContributionManagementContext
        .on(publication)
        .aboutSubscriptionResource(resource)
        .atLocation(new Location(currentFolderPK.getId(), componentInstanceId))
        .forPersistenceAction(statusBeforeSave, ActionType.UPDATE, statusAfterSave));
  }

  /**
   * Setup the request to manager some behaviors around validation of modifications.
   * @param request the current request.
   * @param publications the current handled publications.
   */
  private void setupRequestForContributionBatchManagementContext(HttpRequest request,
      KmeliaSessionController kmeliaSC, List<KmeliaPublication> publications) {
    final ContributionBatchManagementContext context = ContributionBatchManagementContext
        .initialize()
        .forPersistenceAction(ActionType.UPDATE);
    final String componentInstanceId = kmeliaSC.getComponentId();
    publications.stream().forEach(k -> {
      final PublicationDetail publication = k.getDetail();
      final PublicationPK publicationPK = new PublicationPK(publication.getId(), componentInstanceId);
      final SubscriptionResource resource = publication.isAlias() ?
          PublicationAliasSubscriptionResource.from(publicationPK) :
          PublicationSubscriptionResource.from(publicationPK);
      context.addContributionContext(publication, publication.getContributionStatus(),
          new Location(k.getLocation().getId(), componentInstanceId), resource);
    });
    request.setAttribute("contributionBatchManagementContext", context);
  }

  /**
   * Converts the specified identifier into a Silverpeas content primary key.
   * @param instanceId the unique identifier of the component instance to which the contents
   * belongs.
   * @param ids one or several identifiers of Silverpeas contents.
   * @return a list of one or several Silverpeas primary keys, each of them corresponding to one
   * specified identifier.
   */
  private List<ResourceReference> asPks(String instanceId, String... ids) {
    List<ResourceReference> pks = new ArrayList<>();
    for (String oneId : ids) {
      pks.add(new ResourceReference(oneId, instanceId));
    }
    return pks;
  }

  private static class AliasOnOtherKmeliaException extends Exception {
    private static final long serialVersionUID = 2594081198295164318L;

    private final String pubId;
    private final NodePK aliasLocation;

    private AliasOnOtherKmeliaException(final String pubId, final NodePK aliasLocation) {
      this.pubId = pubId;
      this.aliasLocation = aliasLocation;
    }
  }
}
