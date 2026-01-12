/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.components.kmelia.KmeliaConstants;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.servlets.renderers.*;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * @author ehugonnet
 */
public class AjaxPublicationsListServlet extends HttpServlet {

  private static final long serialVersionUID = 1003665785797438465L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    try {
      doPost(req, res);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse res) throws IOException {
    final HttpRequest req = HttpRequest.decorate(request);
    final HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");
    String nodeId = req.getParameter("Id");
    String topicToLinkId = req.getParameter("TopicToLinkId");

    RenderingContext ctx = getRenderingContext(req, session, componentId);
    if (ctx == null) return;

    initTopic(session, ctx, nodeId, topicToLinkId);

    String query = req.getParameter("Query");
    boolean searchRequest = req.getParameterAsBoolean("SearchRequest");
    KmeliaSessionController kmeliaSC = ctx.getSessionController();

    QueryDescription queryDescription = new QueryDescription(query);
    PagesContext formContext = new PagesContext();
    initSearch(queryDescription, formContext, req, kmeliaSC);

    initPublicationsSelection(req, ctx);

    Integer index = req.getParameterAsInteger("Index");
    Integer nbItemsPerPage = req.getParameterAsInteger("NbItemsPerPage");
    initPublicationsPagination(kmeliaSC, index, nbItemsPerPage);

    Integer sort = req.getParameterAsInteger("Sort");
    boolean resetManualSort = StringUtil.getBooleanValue(req.getParameter("ResetManualSort"));
    initPublicationsSorting(kmeliaSC, sort, resetManualSort);

    boolean searchContextExists = kmeliaSC.getSearchContext() != null;
    boolean newSearchInProgress = searchRequest && !queryDescription.isEmpty();
    if (!newSearchInProgress && searchContextExists && (searchRequest || index == null) &&
        nbItemsPerPage == null && sort == null) {
      kmeliaSC.setSearchContext(null);
      searchContextExists = false;
      kmeliaSC.loadPublicationsOfCurrentFolder();
    }

    loadPublications(req, ctx, formContext, queryDescription);

    if (ctx.isAttachmentToLink()) {
      ctx.setSortAllowed(false).setLinksAllowed(false).setSeeAlso(false);
      kmeliaSC.setSearchContext(null);
      searchContextExists = false;
    }

    if (KmeliaHelper.isToolbox(componentId)) {
      String profile = kmeliaSC.getUserTopicProfile(kmeliaSC.getCurrentFolderId());
      ctx.setLinksAllowed(!SilverpeasRole.USER.isInRole(profile));
    }

    ctx.setSearchInProgress(newSearchInProgress || searchContextExists);
    renderWebPage(res, ctx);
  }

  private static void initPublicationsPagination(KmeliaSessionController kmeliaSC, Integer index,
      Integer nbItemsPerPage) {
    boolean inSearchContext = kmeliaSC.getSearchContext() != null;
    if (index != null) {
      if (inSearchContext) {
        kmeliaSC.getSearchContext().setPaginationIndex(index);
      } else {
        kmeliaSC.setIndexOfFirstPubToDisplay(index);
      }
    }
    if (nbItemsPerPage != null) {
      kmeliaSC.setNbPublicationsPerPage(nbItemsPerPage);
    }
  }

  private static void initPublicationsSelection(HttpRequest req, RenderingContext ctx) {
    String selectedPublicationIds = req.getParameter("SelectedPubIds");
    String notSelectedPublicationIds = req.getParameter("NotSelectedPubIds");
    List<PublicationPK> selectedIds = ctx.getSessionController()
        .processSelectedPublicationIds(selectedPublicationIds, notSelectedPublicationIds);
    ctx.setSelectedPublications(selectedIds);
  }

  private static void initPublicationsSorting(KmeliaSessionController kmeliaSC, Integer sort,
      boolean resetManualSort) {
    if (sort != null) {
      kmeliaSC.setSortValue(sort);
      final String contentLanguage = kmeliaSC.getCurrentLanguage();
      ofNullable(kmeliaSC.getSearchContext()).ifPresent(c -> c.applySort(sort, contentLanguage));
    } else if (resetManualSort) {
      kmeliaSC.resetPublicationsOrder();
    }
  }

  private static void initTopic(HttpSession session, RenderingContext ctx, String nodeId,
      String topicToLinkId) {
    var kmeliaSC = ctx.getSessionController();
    if (StringUtil.isDefined(nodeId)) {
      kmeliaSC.setCurrentFolderId(nodeId, true);
      kmeliaSC.loadPublicationsOfCurrentFolder();
      // used by drag n drop
      session.setAttribute("Silverpeas_DragAndDrop_TopicId", nodeId);
    }

    if ((ctx.isToLink() || ctx.isAttachmentToLink()) && StringUtil.isDefined(topicToLinkId)) {
      TopicDetail currentTopicToLink = kmeliaSC.getTopic(topicToLinkId, false);
      kmeliaSC.setSessionTopicToLink(currentTopicToLink);
    }
  }

  private static RenderingContext getRenderingContext(HttpRequest req, HttpSession session,
      String componentId) {
    // check if trying to link attachment
    boolean attachmentToLink = req.getParameterAsBoolean("attachmentLink");
    boolean toLink = StringUtil.getBooleanValue(req.getParameter("ToLink"));
    String pubIdToHighlight = req.getParameter("PubIdToHighLight");
    KmeliaSessionController kmeliaSc =
        (KmeliaSessionController) session.getAttribute("Silverpeas_kmelia_" + componentId);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    if (kmeliaSc == null && (toLink || attachmentToLink)) {
      kmeliaSc = createSessionController(session, componentId);
    }
    if (kmeliaSc == null) {
      return null;
    }
    var ctx = new RenderingContext(kmeliaSc, gef);
    return ctx.setToLink(toLink).setAttachmentToLink(attachmentToLink).
        setPublicationToHighlight(pubIdToHighlight);
  }

  private void renderWebPage(HttpServletResponse res, RenderingContext ctx) {
    res.setContentType("text/xml");
    res.setCharacterEncoding("UTF-8");
    try {
      var kmeliaSC = ctx.getSessionController();
      int previousSort = kmeliaSC.getSort().getCurrentSort();
      Renderer renderer;
      if (kmeliaSC.isRightsOnTopicsEnabled() && !kmeliaSC.isCurrentTopicAvailable()) {
        renderer = new ForbiddenAccessRenderer();
      } else if (!ctx.isToLink() && NodePK.ROOT_NODE_ID.equals(kmeliaSC.getCurrentFolderId()) &&
          kmeliaSC.getNbPublicationsOnRoot() != 0 && kmeliaSC.isTreeStructure() &&
          !ctx.isSearchInProgress()) {
        renderer = new LastPublicationsRenderer();
      } else if (ctx.displayPublications()) {
        renderer = new PublicationsRenderer();
      } else {
        renderer = new NothingRenderer();
      }
      renderer.render(res.getWriter(), ctx);

      //We roll back with previous sort of the topic
      if (ctx.isToLink()) {
        kmeliaSC.getSort().setCurrentSort(previousSort);
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void loadPublications(HttpRequest req, RenderingContext ctx, PagesContext formContext,
      QueryDescription queryDescription) {
    ctx.setSortAllowed(true).setLinksAllowed(true);
    var kmeliaSC = ctx.getSessionController();
    boolean toPortlet = req.getParameterAsBoolean("ToPortlet");
    boolean searchRequest = req.getParameterAsBoolean("SearchRequest");
    boolean newSearchInProgress = searchRequest && !queryDescription.isEmpty();
    boolean searchContextExists = kmeliaSC.getSearchContext() != null;

    TopicDetail currentTopic;
    ctx.setRole(kmeliaSC.getProfile());
    if (ctx.isToLink()) {
      currentTopic = kmeliaSC.getSessionTopicToLink();
      ctx.setLinksAllowed(false).setSeeAlso(true);
      // get selected publication ids from session
      ctx.setSelectedPublications(processPublicationsToLink(req));
      var publications = kmeliaSC.getKmeliaService()
          .getAuthorizedPublicationsOfFolder(new NodePK(currentTopic.getNodePK().getId(),
                  currentTopic.getNodePK().getComponentInstanceId()),
              kmeliaSC.getUserTopicProfile(currentTopic.getNodePK().getId()), kmeliaSC.getUserId()
              ,kmeliaSC.isTreeStructure());
      kmeliaSC.setSessionPublicationsList(publications);
      ctx.setPublications(kmeliaSC.getSessionPublicationsList());
    } else if (toPortlet) {
      ctx.setSortAllowed(false);
      var publications = kmeliaSC.getSessionPublicationsList();
      ctx.setPublications(publications);
      ctx.setRole(SilverpeasRole.USER.toString());
    } else if (newSearchInProgress) {
      var publications = kmeliaSC.search(queryDescription, formContext);
      ctx.setPublications(publications);
    } else if (searchContextExists) {
      var publications = kmeliaSC.getSearchContext().getResults();
      ctx.setPublications(publications);
    } else {
      var publications = kmeliaSC.getSessionPublicationsList();
      ctx.setPublications(publications);
    }
  }

  @SuppressWarnings("unchecked")
  private List<PublicationPK> processPublicationsToLink(HttpServletRequest request) {
    // get from session the list of publications to link with current publication
    HashSet<String> list = (HashSet<String>) request.getSession()
        .getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
    // store the publication identifiers in an array list
    List<PublicationPK> publicationsToLink = new ArrayList<>();
    if (list != null) {
      for (String link : list) {
        String[] tokens = StringUtil.splitByWholeSeparator(link, "-");
        publicationsToLink.add(new PublicationPK(tokens[0], tokens[1]));
      }
    }
    return publicationsToLink;
  }

  private static KmeliaSessionController createSessionController(HttpSession session,
      String componentId) {
    KmeliaSessionController kmeliaSC;
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
    kmeliaSC = new KmeliaSessionController(mainSessionCtrl, componentContext);
    session.setAttribute("Silverpeas_kmelia_" + componentId, kmeliaSC);
    return kmeliaSC;
  }

  private void initSearch(QueryDescription queryDescription, PagesContext formContext,
      HttpServletRequest req, KmeliaSessionController kmeliaSC) {
    Form searchForm = kmeliaSC.getXmlFormSearchForPublications();
    if (searchForm != null) {
      List<FieldTemplate> fields = searchForm.getFieldTemplates();
      for (FieldTemplate field : fields) {
        String fieldName = field.getFieldName();
        String[] fieldValues = req.getParameterValues(fieldName);
        if (ArrayUtil.isNotEmpty(fieldValues)) {

          String fieldValue = fieldValues[0];
          if (fieldValues.length > 1) {
            String operator = req.getParameter(fieldName + "Operator");
            formContext.setSearchOperator(fieldName, operator);
            fieldValue = StringUtils.join(fieldValues, " " + operator + " ");
          }

          queryDescription.addFieldQuery(
              new FieldDescription(searchForm.getFormName() + "$$" + fieldName,
                  fieldValue, kmeliaSC.getLanguage()));
        }
      }
    }
  }

}
