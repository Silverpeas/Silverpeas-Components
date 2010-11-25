/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.stratelia.webactiv.kmelia.servlets;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.UserPublication;
import com.stratelia.webactiv.kmelia.model.UserPublicationComparator;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.board.Board;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;

/**
 * @author ehugonnet
 */
public class AjaxPublicationsListServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");
    String nodeId = req.getParameter("Id");
    String sToLink = req.getParameter("ToLink");
    String TopicToLinkId = req.getParameter("TopicToLinkId");
    // check if trying to link attachment
    String attachmentLink = req.getParameter("attachmentLink");
    boolean attachmentToLink = (isDefined(attachmentLink) && "1".equals(attachmentLink));

    boolean toLink = (isDefined(sToLink) && "1".equals(sToLink));

    KmeliaSessionController kmeliaSC = (KmeliaSessionController) session.getAttribute("Silverpeas_"
        + "kmelia" + "_" + componentId);
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        "SessionGraphicElementFactory");
    String context =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

    if (kmeliaSC == null && (toLink || attachmentToLink)) {
      MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
          "SilverSessionController");
      ComponentContext componentContext =
          mainSessionCtrl.createComponentContext(null, componentId);
      kmeliaSC =
          new KmeliaSessionController(mainSessionCtrl, componentContext);
      session.setAttribute("Silverpeas_kmelia_" + componentId, kmeliaSC);
    }

    if (kmeliaSC != null) {

      if (StringUtil.isDefined(nodeId)) {
        kmeliaSC.getTopic(nodeId, true);
        session.setAttribute("Silverpeas_DragAndDrop_TopicId", nodeId); // used by drag n drop
      }

      if ((toLink || attachmentToLink) && StringUtil.isDefined(TopicToLinkId)) {
        TopicDetail currentTopicToLink = kmeliaSC.getTopic(TopicToLinkId, false);
        kmeliaSC.setSessionTopicToLink(currentTopicToLink);
      }

      ResourcesWrapper resources = new ResourcesWrapper(
          kmeliaSC.getMultilang(), kmeliaSC.getIcon(), kmeliaSC.getSettings(),
          kmeliaSC.getLanguage());

      String index = req.getParameter("Index");
      String sort = req.getParameter("Sort");
      String sToValidate = req.getParameter("ToValidate");
      String sToPortlet = req.getParameter("ToPortlet");
      String pubIdToHighlight = req.getParameter("PubIdToHighLight");
      String query = req.getParameter("Query");

      boolean toValidate = (isDefined(sToValidate) && "1".equals(sToValidate));
      boolean toPortlet = (isDefined(sToPortlet) && "1".equals(sToPortlet));
      boolean toSearch = StringUtil.isDefined(query);

      if (isDefined(index)) {
        kmeliaSC.setIndexOfFirstPubToDisplay(index);
      }
      if (isDefined(sort)) {
        kmeliaSC.setSortValue(sort);
      }

      sort = kmeliaSC.getSortValue();

      SilverTrace.info("kmelia", "AjaxPublicationsListServlet.doPost",
          "root.MSG_GEN_PARAM_VALUE", "Request parameters = "
              + req.getQueryString());

      TopicDetail currentTopic = null;
      boolean sortAllowed = true;
      boolean linksAllowed = true;
      boolean checkboxAllowed = false;
      List<String> selectedIds = new ArrayList<String>();
      List<UserPublication> publications = null;
      boolean subTopics = false;
      String role = kmeliaSC.getProfile();
      if (toLink) {
        currentTopic = kmeliaSC.getSessionTopicToLink();
        sortAllowed = false;
        linksAllowed = false;
        checkboxAllowed = true;
        // get selected publication ids from session
        selectedIds = processPublicationsToLink(req);
        String currentPubId = req.getParameter("PubId");
        String currentPubComponentId = req.getParameter("PubComponentName");
        PublicationPK publicationPK = null;
        if (StringUtil.isDefined(currentPubId) && StringUtil.isDefined(currentPubComponentId)) {
          publicationPK = new PublicationPK(currentPubId, currentPubComponentId);
        }
        publications = currentTopic.getValidPublications(publicationPK);
        if (resources.getSetting("linkManagerSortByPubId", false)) {
          Collections.sort(publications, new UserPublicationComparator());
        }
      } else if (toPortlet) {
        sortAllowed = false;
        currentTopic = kmeliaSC.getSessionTopic();
        publications = kmeliaSC.getSessionPublicationsList();
        role = SilverpeasRole.user.toString();
      } else if (toValidate) {
        kmeliaSC.orderPubsToValidate(Integer.parseInt(sort));
        publications = kmeliaSC.getSessionPublicationsList();
      } else if (toSearch) {
        publications = kmeliaSC.search(query, Integer.parseInt(sort));
      } else {
        currentTopic = kmeliaSC.getSessionTopic();
        publications = kmeliaSC.getSessionPublicationsList();
      }

      if (attachmentToLink) {
        sortAllowed = false;
        linksAllowed = false;
        checkboxAllowed = false;
        toSearch = false;
      }

      if (KmeliaHelper.isToolbox(componentId)) {
        String profile = kmeliaSC.getUserTopicProfile(currentTopic.getNodePK().getId());
        linksAllowed = !SilverpeasRole.user.isInRole(profile);
      }

      subTopics = currentTopic != null && currentTopic.getNodeDetail().getChildrenNumber() > 0;

      res.setContentType("text/xml");
      res.setHeader("charset", "UTF-8");

      Writer writer = res.getWriter();
      if (kmeliaSC.isRightsOnTopicsEnabled() && kmeliaSC.isUserComponentAdmin()
          && !kmeliaSC.isCurrentTopicAvailable()) {
        Board board = gef.getBoard();
        writer.write(board.printBefore());
        writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" align=\"center\">");
        writer.write("<tr>");
        writer.write("<td>"
            + EncodeHelper.escapeXml(resources.getString("GML.ForbiddenAccessContent")) + "</td>");
        writer.write("</tr>");
        writer.write("</table>");
        writer.write(board.printAfter());
      } else if (currentTopic != null && "0".equals(currentTopic.getNodePK().getId()) && kmeliaSC.
          getNbPublicationsOnRoot() != 0 && kmeliaSC.isTreeStructure()) {
        List<UserPublication> publicationsToDisplay = new ArrayList<UserPublication>();
        KmeliaSecurity kmeliaSecurity = new KmeliaSecurity();
        Iterator<UserPublication> iterator = currentTopic.getPublicationDetails().iterator();
        UserPublication userPub;
        while (iterator.hasNext()) {
          userPub = iterator.next();
          if (!kmeliaSC.isPublicationDeleted(userPub.getPublication().getPK().getId()) &&
              kmeliaSecurity.
                  isObjectAvailable(componentId, kmeliaSC.getUserId(), userPub.getPublication()
                      .getPK().
                      getId(), "Publication")) {
            publicationsToDisplay.add(userPub);
          }
        }
        displayLastPublications(publicationsToDisplay, kmeliaSC, resources, gef, writer);
      } else {
        displayPublications(publications, subTopics, sortAllowed, linksAllowed, checkboxAllowed,
            toSearch, kmeliaSC, role, gef, context, resources, selectedIds, pubIdToHighlight,
            writer, attachmentToLink);
      }
    }
  }

  /**
   * @param allPubs
   * @param subtopicsExist
   * @param sortAllowed
   * @param linksAllowed
   * @param checkboxAllowed
   * @param kmeliaScc
   * @param profile
   * @param gef
   * @param context
   * @param resources
   * @param selectedIds
   * @param pubIdToHighlight
   * @param out
   * @param linkAttachment indicates if it displays the attachment publication for the link
   * management administration
   * @throws IOException
 * @throws ThumbnailException 
 * @throws NumberFormatException 
   */
  private void displayPublications(List<UserPublication> allPubs, boolean subtopicsExist,
      boolean sortAllowed, boolean linksAllowed, boolean checkboxAllowed, boolean toSearch,
      KmeliaSessionController kmeliaScc, String profile,
      GraphicElementFactory gef, String context, ResourcesWrapper resources,
      List<String> selectedIds, String pubIdToHighlight, Writer out, boolean linkAttachment)
      throws IOException {
    PublicationDetail pub;
    UserPublication userPub;
    UserDetail user;

    String publicationSrc = resources.getIcon("kmelia.publication");
    String fullStarSrc = resources.getIcon("kmelia.fullStar");
    String emptyStarSrc = resources.getIcon("kmelia.emptyStar");
    ResourceLocator publicationSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.publication.publicationSettings",
        kmeliaScc.getLanguage());
    boolean displayLinks = URLManager.displayUniversalLinks();
    boolean showImportance = resources.getSetting("showImportance", true);
    boolean showNoPublisMessage = resources.getSetting("showNoPublisMessage", true);
    boolean fileStorageShowExtraInfoPub =
        resources.getSetting("fileStorageShowExtraInfoPub", false);
    boolean showTopicPathNameinSearchResult =
        resources.getSetting("showTopicPathNameinSearchResult", true);
    String language = kmeliaScc.getCurrentLanguage();
    String name = null;
    String description = null;

    String currentUserId = kmeliaScc.getUserDetail().getId();
    String currentTopicId = "0";
    if (kmeliaScc.getSessionTopic() != null) {
      currentTopicId = kmeliaScc.getSessionTopic().getNodePK().getId();
    }

    int nbPubsPerPage = kmeliaScc.getNbPublicationsPerPage();
    int firstDisplayedItemIndex = kmeliaScc.getIndexOfFirstPubToDisplay();
    int nbPubs = allPubs.size();
    Board board = gef.getBoard();
    Pagination pagination = gef.getPagination(nbPubs, nbPubsPerPage, firstDisplayedItemIndex);
    List<UserPublication> pubs = allPubs.subList(pagination.getFirstItemIndex(), pagination.
        getLastItemIndex());
    out.write("<form name=\"publicationsForm\">");
    if (pubs.size() > 0) {
      out.write(board.printBefore());
      displayPublicationsListHeader(nbPubs, sortAllowed, pagination, resources, out);
      out.write("<ul>");
      String pubColor = "";
      String pubState = "";
      String highlightClassBegin = "";
      String highlightClassEnd = "";
      for (int p = 0; p < pubs.size(); p++) {
        userPub = pubs.get(p);
        pub = userPub.getPublication();
        user = userPub.getOwner();
        name = pub.getName(language);
        description = pub.getDescription(language);

        pubColor = "";
        pubState = "";
        highlightClassBegin = "";
        highlightClassEnd = "";

        if (StringUtil.isDefined(pubIdToHighlight)
            && pubIdToHighlight.equals(pub.getPK().getId())) {
          highlightClassBegin = "<span class=\"highlight\">";
          highlightClassEnd = "</span>";
        }

        out.write("<!-- Publication Body -->");

        if (pub.getStatus() != null && pub.getStatus().equals("Valid")) {
          if (pub.haveGotClone() && "Clone".equals(pub.getCloneStatus())
              && !SilverpeasRole.user.isInRole(profile)) {
            pubColor = "blue";
            pubState = resources.getString("kmelia.UpdateInProgress");
          } else if ("Draft".equals(pub.getCloneStatus())) {
            if (currentUserId.equals(user.getId())) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else if ("ToValidate".equals(pub.getCloneStatus())) {
            if (SilverpeasRole.admin.isInRole(profile) ||
                SilverpeasRole.publisher.isInRole(profile)
                || currentUserId.equals(user.getId())) {
              pubColor = "red";
              pubState = resources.getString("kmelia.PubStateToValidate");
            }
          } else {
            if (pub.isNotYetVisible()) {
              pubState = resources.getString("kmelia.VisibleFrom") + " "
                  + resources.getOutputDateAndHour(pub.getBeginDateAndHour());
            } else if (pub.isNoMoreVisible()) {
              pubState = resources.getString("kmelia.VisibleTo") + " "
                  + resources.getOutputDateAndHour(pub.getEndDateAndHour());
            }
            if (!pub.isVisible()) {
              pubColor = "gray";
            }
          }
        } else {
          if (pub.getStatus() != null
              && pub.getStatus().equals(PublicationDetail.DRAFT)) {
            // en mode brouillon, si on est en co-rédaction et si on autorise
            // le
            // mode brouillon visible par tous,
            // les publication en mode brouillon sont visibles par tous sauf les
            // lecteurs
            // sinon, seules les publications brouillons de l'utilisateur sont
            // visibles
            if (currentUserId.equals(pub.getUpdaterId())
                ||
                ((kmeliaScc.isCoWritingEnable() && kmeliaScc.isDraftVisibleWithCoWriting()) &&
                !SilverpeasRole.user.isInRole(profile))) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else {
            if (profile.equals("admin") || profile.equals("publisher")
                || currentUserId.equals(pub.getUpdaterId())
                || (!profile.equals("user") && kmeliaScc.isCoWritingEnable())) {
              // si on est en co-rédaction, on affiche toutes les publications
              // à
              // valider (sauf pour les lecteurs)
              pubColor = "red";
              if ("UnValidate".equalsIgnoreCase(pub.getStatus())) {
                pubState = resources.getString("kmelia.PubStateUnvalidate");
              } else {
                pubState = resources.getString("kmelia.PubStateToValidate");
              }
            }
          }
        }

        if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
          pubState = resources.getString("kmelia.Shortcut");
        }

        out.write("<li>");
        out.write("<div class=\"firstColumn\">");
        if (checkboxAllowed) {
          String checked = "";
          if (selectedIds != null && selectedIds.contains(pub.getPK().getId())) {
            checked = "checked=\"checked\"";
          }
          out.write("<span class=\"selection\">");
          out.write("<input type=\"checkbox\" name=\"C1\" value=\""
              + pub.getPK().getId() + "/" + pub.getPK().getInstanceId() + "\" " + checked
              + " onclick=\"sendPubId(this.value, this.checked);\"/>");
          out.write("</span>");
        } else {
          ThumbnailDetail thumbnail = pub.getThumbnail();
          if (thumbnail != null
              && Boolean.valueOf(resources.getSetting("isVignetteVisible"))) {
            out.write("<span class=\"thumbnail\">");
            try{
            	displayThumbnail(pub, kmeliaScc, publicationSettings, out);
            }catch (ThumbnailException e) {
        		SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayPublications()",
        		        "root.MSG_GEN_ENTER_METHOD", "exception = " + e);
			}
            out.write("</span>");
          } else {
            out.write("<span class=\"thumbnail\">");
            out.write("<img src=\"" + resources.getIcon("kmelia.1px") + "\" alt=\"\"/>");
            out.write("</span>");
          }
        }
        out.write("</div>");

        out.write("<div class=\"publication\">");
        out.write("<div class=\"line1\">");
        out.write("<span class=\"bullet\">&#8226;</span><a name=\""
            + pub.getPK().getId() + "\"></a>");
        if (linksAllowed) {
          out.write("<font color=\"" + pubColor
              + "\"><a href=\"javascript:onClick=publicationGoTo('"
              + pub.getPK().getId() + "')\"><b>" + highlightClassBegin
              + EncodeHelper.escapeXml(name) + highlightClassEnd
              + "</b></a></font>");
        } else {
          String ref = "";
          if (checkboxAllowed && resources.getSetting("linkManagerShowPubId", false)) {
            ref = " [ " + pub.getPK().getId() + " ] ";
          }
          out.write("<font color=\"" + pubColor + "\"><b>"
              + highlightClassBegin + ref + EncodeHelper.escapeXml(name)
              + highlightClassEnd + "</b></font>");
        }
        out.write("&#160;");
        if (pubState.length() > 0) {
          out.write("<span class=\"state_" + pubState + "\">(" + EncodeHelper.escapeXml(pubState) +
              ")</span>");
        } else if (showImportance && !linkAttachment) {
          out.write("<span class=\"importance\"><nobr>"
              + displayImportance(new Integer(pub.getImportance()).intValue(),
                  5, fullStarSrc, emptyStarSrc, out) + "</nobr></span>");
        }
        out.write("</div>");
        out.write("<div class=\"line2\">");
        out.write("<font color=\"" + pubColor + "\">");
        // Show topic name only in search in topic case
        if (toSearch && showTopicPathNameinSearchResult) {
          displayPublicationFullPath(kmeliaScc, pub, out);
        }
        // check if user name in the list must be display
        boolean showUserNameInList = kmeliaScc.showUserNameInList();
        if (linkAttachment) {
          showUserNameInList = showUserNameInList && fileStorageShowExtraInfoPub;
        }
        if (showUserNameInList) {
          out.write("<span class=\"user\">");
          out.write(getUserName(userPub, kmeliaScc) + " - ");
          out.write("</span>");
        }
        // check if the pub date must be display
        boolean showPubDate = true;
        if (linkAttachment) {
          showPubDate = showPubDate && fileStorageShowExtraInfoPub;
        }
        if (showPubDate) {
          out.write("<span class=\"date\">");
          if ("5".equals(kmeliaScc.getSortValue()) || "6".equals(kmeliaScc.getSortValue())) {
            out.write(resources.getOutputDate(pub.getCreationDate()));
          } else {
            out.write(resources.getOutputDate(pub.getUpdateDate()));
          }
          out.write("</span>");
        }

        // check if he author name must be display
        boolean showAuthor = kmeliaScc.isAuthorUsed() && pub.getAuthor() != null
            && !pub.getAuthor().equals("");
        if (linkAttachment) {
          showAuthor = showAuthor && fileStorageShowExtraInfoPub;
        }
        if (showAuthor) {
          out.write("<span class=\"author\">");
          out.write("&#160;-&#160;(" + resources.getString("GML.author")
              + ":&#160;" + EncodeHelper.escapeXml(pub.getAuthor()) + ")");
          out.write("</span>");
        }
        // displays permalink
        if (displayLinks && (!checkboxAllowed && !linkAttachment)) {
          out.write("<span class=\"permalink\">");
          displayPermalink(pub, kmeliaScc, resources, out);
          out.write("</span>");
        }
        out.write("</div>");

        // displays publication description
        if (StringUtil.isDefined(description) && !description.equals(name)) {
          out.write("<div class=\"line3\">");
          out.write("<span class=\"description\">");
          out.write(EncodeHelper.javaStringToHtmlParagraphe(pub.getDescription(language)));
          out.write("</span>");
          out.write("</div>");
        }

        out.write("</font>");
        if ((KmeliaHelper.isToolbox(kmeliaScc.getComponentId()) || kmeliaScc.attachmentsInPubList())
            &&
            !checkboxAllowed || linkAttachment) {
          out.write("<span class=\"files\">");
          // Can be a shortcut. Must check attachment mode according to publication source.
          boolean isAlias = !kmeliaScc.getComponentId().equalsIgnoreCase(pub.getPK().getInstanceId());
          if (kmeliaScc.isVersionControlled(pub.getPK().getInstanceId())) {
            out.write(displayVersioning(pub, out, resources, linkAttachment, isAlias));
          } else {
            out.write(displayAttachments(pub, currentUserId, currentTopicId, out, resources,
                linkAttachment, isAlias));
          }
          out.write("</span>");
        }
        out.write("</div>");

      } // End while
      out.write("</ul>");
      if (nbPubs > nbPubsPerPage) {
        out.write("<div id=\"pagination\">");
        out.write(pagination.printIndex("doPagination"));
        out.write("</div>");
      }
      out.write(board.printAfter());
    } // End if
    else if (showNoPublisMessage
        && (toSearch || kmeliaScc.getNbPublicationsOnRoot() != 0 || !currentTopicId.equals("0"))) {
      String noPublications = kmeliaScc.getString("PubAucune");
      if (toSearch)
        noPublications = kmeliaScc.getString("NoPubFound");
      out.write(board.printBefore());
      out.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" align=\"center\">");
      out.write("<tr valign=\"middle\" class=\"intfdcolor\">");
      out.write("<td width=\"80\"><img src=\"" + publicationSrc
          + "\" border=\"0\"/></td>");
      out.write("<td align=\"left\"><b>"
          + resources.getString("GML.publications") + "</b></td></tr>");
      out.write("<tr class=\"intfdcolor4\"><td colspan=\"2\">&#160;</td></tr>");
      out.write("<tr>");
      out.write("<td>&#160;</td>");
      out.write("<td>"
          + EncodeHelper.escapeXml(noPublications) + "</td>");
      out.write("</tr>");
      out.write("</table>");
      out.write(board.printAfter());
    }
    out.write("</form>");
  }

  void displayThumbnail(PublicationDetail pub, KmeliaSessionController ksc,
      ResourceLocator publicationSettings, Writer out) throws IOException, NumberFormatException, ThumbnailException {
    int[] defaultSizes = ksc.getThumbnailWidthAndHeight();
    String vignette_url;
    if (pub.getImage().startsWith("/")) {
      vignette_url = pub.getImage() + "&Size=133x100";
      out.write("<img src=\"" + vignette_url + "\" alt=\"\"/>&#160;");
    } else {
      vignette_url =
          EncodeHelper.escapeXml(FileServerUtils.getUrl(pub.getPK().getSpace(), pub.
              getPK().getComponentName(),
              "vignette", pub.getImage(), pub.getImageMimeType(),
              publicationSettings.getString("imagesSubDirectory")));
      String height = "";
      String width = "";
      if (!StringUtil.isDefined(pub.getThumbnail().getCropFileName())) {
        // thumbnail is not cropable, process sizes  
        String[] size = new String[2];
        File image = getThumbnail(pub, publicationSettings);
        if (defaultSizes[0] != -1) {
          size = ImageUtil.getWidthAndHeightByWidth(image, defaultSizes[0]);
        } else if (defaultSizes[1] != -1) {
          size = ImageUtil.getWidthAndHeightByHeight(image, defaultSizes[1]);
        }
        if (StringUtil.isDefined(size[0]) && StringUtil.isDefined(size[1])) {
          width = size[0];
          height = size[1];
        }
      }
      out.write("<img src=\"" + vignette_url + "\""
          + ((height == null) ? "" : " height=\"" + height + "\"")
          + ((width == null) ? "" : " width=\"" + width + "\"")
          + "/ alt=\"\">&#160;");
    }
  }

  void displayPermalink(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      ResourcesWrapper resources, Writer out) throws IOException {
    String link = null;
    if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
      link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId(),
          kmeliaScc.getComponentId());
    } else {
      link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId());
    }
    out.write(" - <a href=\""
        + link
        + "\"><img src=\""
        + resources.getIcon("kmelia.link")
        + "\" border=\"0\" align=\"absmiddle\" alt=\""
        + EncodeHelper.escapeXml(resources.getString("kmelia.CopyPublicationLink"))
        + "\" title=\""
        + EncodeHelper.escapeXml(resources.getString("kmelia.CopyPublicationLink"))
        + "\"/></a>");
  }

  void displaySortingListBox(ResourcesWrapper resources, Writer out)
      throws IOException {
    out
        .write("<select name=\"sortBy\" id=\"sortingList\" onChange=\"javascript:sortGoTo(this.selectedIndex);\">");
    out.write("<option selected=\"selected\">"
        + EncodeHelper.escapeXml(resources.getString("SortBy")) + "</option>");
    out.write("<option>-------------------------------</option>");
    out.write("<option value=\"1\" id=\"sort1\">"
        + EncodeHelper.escapeXml(resources.getString("DateAsc")) + "</option>");
    out.write("<option value=\"2\" id=\"sort2\">"
        + EncodeHelper.escapeXml(resources.getString("DateDesc"))
        + "</option>");
    out.write("<option value=\"5\" id=\"sort5\">"
        + EncodeHelper.escapeXml(resources.getString("CreateDateAsc")) + "</option>");
    out.write("<option value=\"6\" id=\"sort6\">"
        + EncodeHelper.escapeXml(resources.getString("CreateDateDesc"))
        + "</option>");
    out.write("<option value=\"0\" id=\"sort0\">"
        + EncodeHelper.escapeXml(resources.getString("PubAuteur"))
        + "</option>");
    if (!"no".equals(resources.getSetting("showImportance"))) {
      out.write("<option value=\"3\" id=\"sort3\">"
          + EncodeHelper.escapeXml(resources.getString("PubImportance")) + "</option>");
    }
    out.write("<option value=\"4\" id=\"sort4\">" +
        EncodeHelper.escapeXml(resources.getString("PubTitre"))
        + "</option>");
    out.write("<option value=\"7\" id=\"sort7\">" +
        EncodeHelper.escapeXml(resources.getString("PubDescription"))
        + "</option>");
    out.write("</select>");
  }

  void displayPublicationsListHeader(int nbPubs, boolean sortAllowed,
      Pagination pagination, ResourcesWrapper resources, Writer out)
      throws IOException {
    String publicationSrc = resources.getIcon("kmelia.publication");
    out.write("<div id=\"pubsHeader\">");
    out.write("<img src=\"" + publicationSrc + "\" alt=\"\"/>");
    out.write("<span id=\"pubsCounter\">");
    out.write(pagination.printCounter());
    if (nbPubs > 1) {
      out.write(EncodeHelper.escapeXml(resources.getString("GML.publications")));
    } else {
      out.write(EncodeHelper.escapeXml(resources.getString("GML.publication")));
    }
    out.write("</span>");
    out.write("<span id=\"pubsSort\">");
    if (sortAllowed) {
      displaySortingListBox(resources, out);
    }
    out.write("</span>");
    out.write("</div>");
  }

  String getUserName(UserPublication userPub, KmeliaSessionController kmeliaScc) {
    UserDetail user = userPub.getOwner(); // contains creator
    PublicationDetail pub = userPub.getPublication();
    String updaterId = pub.getUpdaterId();
    UserDetail updater = null;
    if (updaterId != null && updaterId.length() > 0) {
      updater = kmeliaScc.getUserDetail(updaterId);
    }
    if (updater == null) {
      updater = user;
    }

    String userName = "";
    if (updater != null
        && (updater.getFirstName().length() > 0 || updater.getLastName().length() > 0)) {
      userName = updater.getDisplayedName();
    } else {
      userName = kmeliaScc.getString("kmelia.UnknownUser");
    }
    return EncodeHelper.escapeXml(userName);
  }

  private String displayImportance(int importance, int maxImportance, String fullStar,
      String emptyStar, Writer out) throws IOException {
    String stars = "";

    // display full Stars
    for (int i = 0; i < importance; i++) {
      stars += "<img src=\"" + fullStar + "\" align=\"absmiddle\" alt=\"\"/>";
    }
    // display empty stars
    for (int i = importance + 1; i <= 5; i++) {
      stars += "<img src=\"" + emptyStar + "\" align=\"absmiddle\" alt=\"\"/>";
    }
    return stars;
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !param.equals("null"));
  }

  @SuppressWarnings("unchecked")
  private List<String> processPublicationsToLink(
      HttpServletRequest request) {
    // get from session the list of publications to link with current publication
    HashSet<String> list =
        (HashSet) request.getSession().getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);

    // store the publication identifiers in an array list
    List<String> publicationsToLink = new ArrayList<String>();
    StringTokenizer tokens = null;
    if (list != null) {
      for (String link : list) {
        tokens = new StringTokenizer(link, "/");
        publicationsToLink.add(tokens.nextToken());
      }
    }
    return publicationsToLink;
  }

  @SuppressWarnings("unchecked")
  private String displayVersioning(PublicationDetail pubDetail, Writer out,
      ResourcesWrapper resources, boolean linkAttachment, boolean alias) throws IOException {
    VersioningUtil versioning = new VersioningUtil();
    ForeignPK foreignPK = new ForeignPK(pubDetail.getPK());
    List<Document> documents = versioning.getDocuments(foreignPK);
    Iterator<Document> iterator = documents.iterator();
    StringBuilder result = new StringBuilder();
    String url = "";
    String title = "";
    String info = "";
    String icon;
    String logicalName;
    String size;
    String downloadTime;
    Date creationDate;
    String permalink = null;
    boolean oneFile = false;
    while (iterator.hasNext()) {
      Document document = iterator.next();
      DocumentVersion version = versioning.getLastPublicVersion(document.getPk());
      if (version != null) {
        if (result.length() == 0) {
          result.append("<table border=\"0\">");
          oneFile = true;
        }

        title = document.getName() + " v" + version.getMajorNumber();
        info = document.getDescription();
        icon = versioning.getDocumentVersionIconPath(version.getPhysicalName());
        logicalName = version.getLogicalName();
        size = FileRepositoryManager.formatFileSize(version.getSize());
        downloadTime = versioning.getDownloadEstimation(version.getSize());
        creationDate = version.getCreationDate();
        permalink = URLManager.getSimpleURL(URLManager.URL_DOCUMENT, document.getPk().getId());
        url = FileServerUtils.getApplicationContext() +
            versioning.getDocumentVersionURL(document.getPk().getInstanceId(),
                logicalName, document.getPk().getId(), version.getPk().getId());
        
        if (alias) {
          url = FileServerUtils.getAliasURL(document.getPk().getInstanceId(), logicalName,
              document.getPk().getId(), version.getPk().getId());
        }

        result.append(displayFile(url, title, info, icon, logicalName, size, downloadTime,
            creationDate, permalink, out, resources, linkAttachment));
      }
    }
    if (oneFile) {
      result.append("</table>");
    }
    return result.toString();
  }

  private String displayAttachments(PublicationDetail pubDetail, String userId, String nodeId,
      Writer out, ResourcesWrapper resources, boolean linkAttachment, boolean alias) throws
      IOException {
    SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubDetail.getPK().getId());
    StringBuilder result = new StringBuilder();

    AttachmentPK foreignKey = new AttachmentPK(pubDetail.getPK().getId(),
        pubDetail.getPK().getInstanceId());

    Collection<AttachmentDetail> attachmentList =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, "Images");
    if (!attachmentList.isEmpty()) {
      result.append("<table border=\"0\">");
      for (AttachmentDetail attachmentDetail : attachmentList) {
        String url = attachmentDetail.getAttachmentURLToMemorize(userId, nodeId);
        String title = attachmentDetail.getTitle();
        String info = attachmentDetail.getInfo();
        String icon = attachmentDetail.getAttachmentIcon();
        String logicalName = attachmentDetail.getLogicalName();
        String id = attachmentDetail.getPK().getId();
        String size = attachmentDetail.getAttachmentFileSize();
        String downloadTime = attachmentDetail.getAttachmentDownloadEstimation();
        Date creationDate = attachmentDetail.getCreationDate();
        String permalink = null;
        if (!attachmentDetail.isAttachmentLinked()) {
          permalink = URLManager.getSimpleURL(URLManager.URL_FILE, id);
        }
        
        if (alias) {
          url = FileServerUtils.getAliasURL(foreignKey.getInstanceId(), logicalName, id);
        }
        
        result.append(displayFile(url, title, info, icon, logicalName, size, downloadTime,
            creationDate, permalink, out, resources, linkAttachment));
      }
      result.append("</table>");
    }
    SilverTrace.info("kmelia", "JSPattachmentUtils.displayAttachments()",
        "root.MSG_GEN_EXIT_METHOD", "result = " + result.toString());
    return result.toString();
  }

  /**
   * @param url
   * @param title
   * @param info
   * @param icon
   * @param logicalName
   * @param size
   * @param downloadTime
   * @param creationDate
   * @param permalink
   * @param out
   * @param resources
   * @param attachmentLink determines if the attachments are displayed in the management interface
   * links to attachments. If it's true it formats differently the HTML rendering
   * @return
   * @throws IOException
   */
  private String displayFile(String url, String title, String info, String icon,
      String logicalName,
      String size, String downloadTime, Date creationDate, String permalink, Writer out,
      ResourcesWrapper resources, boolean attachmentLink) throws IOException {
    SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayFile()",
        "root.MSG_GEN_ENTER_METHOD");
    StringBuilder result = new StringBuilder();

    if (!attachmentLink) {
      String link = "<A href=\"" + EncodeHelper.escapeXml(url) + "\" target=\"_blank\">";
      result.append("<TR><TD valign=\"top\">");
      // Add doc type icon
      result.append(link).append("<IMG src=\"").append(icon).append(
          "\" border=\"0\" align=\"absmiddle\"/></A>&#160;</TD>");
      result.append("<TD valign=\"top\">").append(link);
      if (title == null || title.length() == 0) {
        result.append(EncodeHelper.escapeXml(logicalName));
      } else {
        result.append(EncodeHelper.escapeXml(title));
      }
      result.append("</A>");

      if (StringUtil.isDefined(permalink)) {
        result.append("&#160;<a href=\"").append(EncodeHelper.escapeXml(permalink)).append(
            "\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link")).append(
            "\" border=\"0\" valign=\"absmiddle\" alt=\"").append(EncodeHelper.escapeXml(
            resources.getString("toolbox.CopyFileLink"))).append("\" title=\"").append(
            EncodeHelper.escapeXml(resources.getString("toolbox.CopyFileLink"))).append("\"/></a>");
      }

      result.append("<br/>");

      result.append("<i>");
      if (StringUtil.isDefined(title) && !"no".equals(resources.getSetting("showTitle"))) {
        result.append(EncodeHelper.escapeXml(logicalName)).append(" / ");
      }
      // Add file size
      if (!"no".equals(resources.getSetting("showFileSize"))) {
        result.append(EncodeHelper.escapeXml(size));
      }
      // and download estimation
      if (!"no".equals(resources.getSetting("showDownloadEstimation"))) {
        result.append(" / ").append(EncodeHelper.escapeXml(downloadTime)).append(" / ").append(
            resources.getOutputDate(creationDate));
      }
      result.append("</i>");

      // Add info
      if (StringUtil.isDefined(info) && !"no".equals(resources.getSetting("showInfo"))) {
        result.append("<BR/>").append(
            EncodeHelper.javaStringToHtmlParagraphe(EncodeHelper.escapeXml(info)));
      }
      result.append("</TD></TR>");

    } else {
      // determines the label to display
      String displayedTitle = "";
      if (title == null || title.length() == 0) {
        displayedTitle = EncodeHelper.escapeXml(logicalName);
      } else {
        displayedTitle = EncodeHelper.escapeXml(title);
      }
      // create the javascript which allows the attachment link selecting
      String javascriptFunction =
          "selectAttachment('" + EncodeHelper.escapeXml(url) + "','" + icon + "','" +
              displayedTitle
              + "')";
      String link = "<a href=\"javascript:" + javascriptFunction + "\" >";
      result.append("<tr><td valign=\"top\">");

      // Add doc type icon
      result.append(link).append("<img src=\"").append(icon).append(
          "\" border=\"0\" align=\"absmiddle\"/></a>&#160;</td>");
      result.append("<td valign=\"top\">").append(link);

      // inserts label (attachment title or logical file name)
      result.append(displayedTitle);
      result.append("</a>");

      // inserts permalink
      if (StringUtil.isDefined(permalink)) {
        result.append("&#160;<a href=\"").append(EncodeHelper.escapeXml(permalink)).append(
            "\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link")).append(
            "\" border=\"0\" valign=\"absmiddle\" alt=\"").append(EncodeHelper.escapeXml(
            resources.getString("toolbox.CopyFileLink"))).append("\" title=\"").append(
            EncodeHelper.escapeXml(resources.getString("toolbox.CopyFileLink"))).append("\"/></a>");
      }
      result.append("<br/>");
      // displays extra information if parameter is true
      if (resources.getSetting("fileStorageShowExtraInfoAttachment", false)) {
        result.append("<i>");
        if (StringUtil.isDefined(title)) {
          result.append(EncodeHelper.escapeXml(logicalName)).append(" / ");
        }
        // Add file size
        result.append(EncodeHelper.escapeXml(size));

        // and download estimation
        result.append(" / ").append(EncodeHelper.escapeXml(downloadTime)).append(" / ").append(
            resources.getOutputDate(creationDate));
        result.append("</i>");
        // Add info
        if (StringUtil.isDefined(info)) {
          result.append("<br/>").append(
              EncodeHelper.javaStringToHtmlParagraphe(EncodeHelper.escapeXml(info)));
        }
      }

    }
    SilverTrace.info("kmelia", "JSPattachmentUtils.displayFile()", "root.MSG_GEN_EXIT_METHOD");
    return result.toString();

  }

  private void displayLastPublications(List<UserPublication> pubs,
      KmeliaSessionController kmeliaScc, ResourcesWrapper resources, GraphicElementFactory gef,
      Writer writer) throws IOException {

    boolean displayLinks = URLManager.displayUniversalLinks();
    PublicationDetail pub;
    UserPublication userPub;
    String language = kmeliaScc.getCurrentLanguage();

    Iterator<UserPublication> iterator = pubs.iterator();

    Board board = gef.getBoard();
    writer.write(board.printBefore());
    writer.write("<table border=\"0\" width=\"98%\" align=\"center\">");
    writer.write("<tr>");
    writer.write("<td width=\"40\" align=\"left\"><img src=\"" + resources.getIcon(
        "kmelia.publication") + "\" border=0></td>");
    writer.write("<td align=\"left\" width=\"100%\"><b>" + kmeliaScc.getString("PublicationsLast")
        + "</b></td>");
    writer.write("</tr>");
    if (iterator.hasNext()) {
      writer.write("<tr><td colspan=\"2\">&nbsp;</td></tr>");
      writer.write("<!-- Publications Header End -->");
      writer.write("<tr><td colspan=\"2\"><table border=\"0\" width=\"100%\">");
      int j = 1;
      int nbCol = new Integer(resources.getSetting("HomeNbCols")).intValue();
      if (pubs.size() < nbCol) {
        nbCol = pubs.size();
      }
      String width = new Integer(100 / nbCol).toString();
      boolean endRaw = false;
      String linkIcon = resources.getIcon("kmelia.link");
      String shortcut = null;
      while (iterator.hasNext()) {
        if (j == 1) {
          writer.write("<tr>\n");
          writer.write("<td valign=\"top\">&nbsp;</td>\n");
          endRaw = false;
        }
        if (j <= nbCol) {
          userPub = iterator.next();
          pub = userPub.getPublication();

          if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
            shortcut = " (" + resources.getString("kmelia.Shortcut") + ")";
          } else {
            shortcut = "";
          }

          writer.write("<!-- Publication Body -->");
          writer.write("<td valign=\"top\" width=\"100\">&#149; </td>");
          writer.write("<td valign=\"top\" width=\"" + width + "%\">");
          writer.write("<p><b><a href=\"javascript:onClick=publicationGoToFromMain('" +
              pub.getPK().
                  getId() + "')\">" + EncodeHelper.javaStringToHtmlString(pub.getName(language))
              + "</a>" + shortcut + "</b><br/>");

          if (kmeliaScc.showUserNameInList()) {
            writer.write(getUserName(userPub, kmeliaScc) + " - ");
          }
          writer.write(resources.getOutputDate(pub.getUpdateDate()));
          if (displayLinks) {
            String link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId());
            writer.write(" - <a href=\"" + link + "\"><img src=\"" + linkIcon
                + "\" border=\"0\" align=\"absmiddle\" alt=\"" + resources.getString(
                    "kmelia.CopyPublicationLink") + "\" title=\"" + resources.getString(
                    "kmelia.CopyPublicationLink") + "\"></a>");
          }
          writer.write("<br>");
          writer.write(EncodeHelper.javaStringToHtmlParagraphe(pub.getDescription(language))
              + "<BR><BR></p>");
          writer.write("</td>");
          writer.write("<!-- Publication Body End -->");
          j++;
        }
        if (j > nbCol) {
          writer.write("\t</tr>");
          endRaw = true;
          j = 1;
        }
      }
      if (!endRaw) {
        int nbTd = nbCol - j + 1;
        int k = 1;
        while (k <= nbTd) {
          writer.write("<td colspan=\"3\" valign=\"top\">&nbsp;</td>\n");
          k++;
        }
        writer.write("</tr>\n");
      }
      writer.write("</td></tr></table>");
    } // End if
    else {
      writer.write("<tr>");
      writer.write("<td>&nbsp;</td>");
      writer.write("<td>" + kmeliaScc.getString("PubAucune") + "</td>");
      writer.write("</tr>");
    }
    writer.write("</td>");
    writer.write("</tr>");
    writer.write("</table>");
    writer.write(board.printAfter());
  }

  private void displayPublicationFullPath(KmeliaSessionController kmelia, PublicationDetail pub,
      Writer writer) throws IOException {
    // Get space and componentLabel of the publication (can be different from context)
    OrganizationController orga = kmelia.getOrganizationController();
    ComponentInstLight compoInstLight = orga.getComponentInstLight(pub.getInstanceId());
    String componentLabel = compoInstLight.getLabel(kmelia.getCurrentLanguage());
    String spaceLabel =
        orga.getSpaceInstLightById(compoInstLight.getDomainFatherId()).getName(
            kmelia.getCurrentLanguage());
    List<NodePK> nodesPK = (List<NodePK>) pub.getPublicationBm().getAllFatherPK(pub.getPK());
    if (nodesPK != null) {
      NodePK firstNodePK = (NodePK) nodesPK.get(0);
      String topicPathName = spaceLabel + " > " + componentLabel + " > " +
          kmelia.displayPath(kmelia.getKmeliaBm().getPath(firstNodePK.getId(),
              firstNodePK.getInstanceId()), false, 3);
      writer.write("<div class=\"publiPath\">" + topicPathName + "</div>");
    }
  }

  private File getThumbnail(PublicationDetail pubDetail, ResourceLocator publicationSettings) throws ThumbnailException {
    if (StringUtil.isDefined(pubDetail.getImage())) {
      return new File(FileRepositoryManager.getAbsolutePath(pubDetail.getPK().getInstanceId())
          + publicationSettings.getString("imagesSubDirectory") + File.separator
          + pubDetail.getImage());
    } else {
      return null;
    }
  }
}
