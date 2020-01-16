<%--

    Copyright (C) 2000 - 2019 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.components.websites.control.WebSiteSessionController"%>
<%@ page import="org.silverpeas.components.websites.service.WebSitesException"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.Link"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>

<%!
  /**
   * Centralizing the rendering of topic navigation.<br/>
   * <p>
   * <u>In JAVASCRIPT context:</u><br/>
   * An instance of WebSiteManager MUST have been instantiated and registered into 'wsm' variable name.<br/>
   * This variable MUST be scoped into window.
   * </p>
   */
  public String renderTopicNavigation(final WebSiteSessionController scc,
      final GraphicElementFactory gef, final FolderDetail currentFolder)
      throws WebSitesException {
    final Collection<NodeDetail> topics = currentFolder.getNodeDetail().getChildrenDetails();
    final Collection<Integer> nbSitesByTopic = currentFolder.getNbPubByTopic();
    if (topics.isEmpty()) {
      return "";
    }
    final NavigationList navList = gef.getNavigationList();
    final Iterator<Integer> nbSiteIt = nbSitesByTopic.iterator();
    for (final NodeDetail topic : topics) {
      final String topicName = topic.getName();
      final String topicId = topic.getNodePK().getId();
      final FolderDetail folder = scc.getFolder(topicId);
      final Collection<NodeDetail> subTopics = folder.getNodeDetail().getChildrenDetails();
      final List<Link> subTopicLinks = subTopics.stream()
          .map(t -> new Link(t.getName(), "javascript:wsm.goToAppTopic('" + t.getNodePK().getId() + "')"))
          .collect(Collectors.toList());
      final int nbSites = nbSiteIt.hasNext() ? nbSiteIt.next() : 0;
      navList.addItemSubItem(topicName, "javascript:wsm.goToAppTopic('" + topicId + "')", nbSites, subTopicLinks);
    }
    return navList.print();
  }

  /**
   * Centralizing the rendering of sites of a topic.<br/>
   * <p>
   * <u>In JAVASCRIPT context:</u><br/>
   * An instance of WebSiteManager MUST have been instantiated and registered into 'wsm' variable name.<br/>
   * This variable MUST be scoped into window.
   * </p>
   */
  public String renderTopicSites(final WebSiteSessionController scc,
      final FolderDetail currentFolder) throws WebSitesException {
    final String pxmag = URLUtil.getApplicationURL() + "/util/icons/colorPix/1px.gif";
    final String redFlag = URLUtil.getApplicationURL() + "/util/icons/urgent.gif";
    final StringBuilder htmlList = new StringBuilder();
    final Collection<PublicationDetail> siteList = currentFolder.getPublicationDetails();
    if (!siteList.isEmpty()) {
      htmlList.append("<TABLE CELLPADDING=3 CELLSPACING=0 ALIGN=CENTER BORDER=0 WIDTH=\"98%\"><tr><td>\n");
      for (final PublicationDetail site : siteList) {
        final String siteId = site.getVersion();
        final String siteName = site.getName();
        final String siteDescription = WebEncodeHelper.javaStringToHtmlParagraphe(site.getDescription());
        final StringBuilder iconList = new StringBuilder();
        final Collection<IconDetail> icons = scc.getIcons(siteId);
        boolean rouge = false;
        htmlList.append("<tr>\n");
        for (final IconDetail icon : scc.getAllIcons()) {
          if (isIconIncludedIn(icon, icons)) {
            if (icon.getName().equals("Icon0")) {
              rouge = true;
            } else {
              iconList
                  .append("<A href=\"javascript:void(0)\" onclick=\"wsm.openIconDictionary()\"><img src=\"")
                  .append(icon.getAddress()).append("\" alt=\"")
                  .append(scc.getString(icon.getName()))
                  .append("\" border=0 align=absmiddle title=\"")
                  .append(scc.getString(icon.getName())).append("\"></A>&nbsp;\n");
            }
          }
        }
        if (rouge) {
          htmlList.append("<td valign=\"top\"><img src=\"").append(redFlag).append("\" border=\"0\" align=absmiddle></td>\n");
        } else {
          htmlList.append("<td valign=\"top\">&nbsp;</td>\n");
        }
        htmlList.append(
            "<td valign=\"top\" align=left nowrap>&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=wsm.goToSite('")
            .append(siteId).append("')\">").append(siteName)
            .append("</a></td><td align=left>\n");
        htmlList.append(iconList);
        htmlList.append(
            "</td></tr><tr><td class=intfdcolor51>&nbsp;</td><td colspan=2 width=\"100%\" class=intfdcolor51><span class=\"txtnote\">")
            .append(siteDescription).append("</span></td></tr><tr><td colspan=3><img src=\"")
            .append(pxmag).append("\" height=3 width=200></td>\n");
      }
      htmlList.append("</td></tr></table>\n");
    } else {
      htmlList.append("<div class='inlineMessage'>").append(scc.getString("NoLinkAvailable")).append("</div>");
    }
    return htmlList.toString();
  }

  private boolean isIconIncludedIn(IconDetail iconDetail, Collection<IconDetail> c) {
    final String iconId = iconDetail.getIconPK().getId();
    return c.stream().map(i -> i.getIconPK().getId()).anyMatch(i -> i.equals(iconId));
  }

  /* doubleAntiSlash */
  public String doubleAntiSlash(String path) {
    StringBuilder res = new StringBuilder(path);
    int k = 0;
    for (int i = 0, j = 1; i < path.length(); i++, j++) {
      if (path.charAt(i) == '\\') {
        boolean hasNotAntiSlashAfter = j < path.length() && path.charAt(j) != '\\';
        boolean hasNotAntiSlashBefore = i > 0 && path.charAt(i - 1) != '\\';
        if (hasNotAntiSlashAfter && hasNotAntiSlashBefore) {
          res.insert(k+i, '\\');
          k++;
        }
      }
    }
    return res.toString();
  }

  private String ignoreSlash(String chemin)
  {
    /* ex : /rep1/rep2/rep3 */
    /* res = rep1/rep2/rep3 */

      String res = chemin;
      boolean ok = false;
      while (!ok) {
          char car = res.charAt(0);
          if (car == '/') {
              res = res.substring(1);
          }
          else ok = true;
      }
      return res;
  }

  /* node */
  private String node(String path) {
      /* ex : ....\\id\\rep1\\rep2\\rep3 */
      /* res : rep3 */

      int index = path.lastIndexOf("/");
      /* finChemin = \\rep3 */
      String finChemin = path.substring(index);
      return ignoreSlash(finChemin);
  }


  /* supprAntiSlashFin */
  private String supprAntiSlashFin(String path) {
      /* ex : ....\\id\\rep1\\rep2\\rep3\\  */
      /* res : ....\\id\\rep1\\rep2\\rep3 */

      int longueur = path.length();
      /*if (path.substring(longueur - 2).equals("\\\\"))
          return path.substring(0, longueur - 2);
      else */
      if (path.substring(longueur - 1).equals("/"))
          return path.substring(0, longueur - 1);
      else return path;
  }

 /* affichageChemin */
  private Collection affichageChemin(WebSiteSessionController scc, String currentPath) {
      /* retourne la liste des noeuds par lesquels on passe */
      /* ex : c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\ */
      /* res = */
      /* collection[0] = c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3 : toujours */
      /* collection[1] = c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1 */
      /* collection[1] = c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11 */

      List<String> liste = new ArrayList<>();
      boolean ok = true;
      String deb;
      String finChemin;

      String chemin = currentPath;
      if (chemin != null) {
          chemin = supprAntiSlashFin(chemin); /* c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11 */

          int longueur = scc.getComponentId().length();
          int index = chemin.lastIndexOf(scc.getComponentId());
          /* index de fin de webSite15 dans le chemin */
          index = index + longueur;
          /* finChemin = "\\id\\rep\\ ..." */
          finChemin = chemin.substring(index);
          /* saute les antiSlash, finChemin = id\\rep\\ ... */
          finChemin = ignoreSlash(finChemin);

          index = finChemin.indexOf("/");

          if (index == -1) {
                liste.add(chemin); /* la racine id */
          }

          else {
              /* deb */
              int indexRacine = chemin.indexOf(finChemin);
              int indexAntiSlashSuivant = finChemin.indexOf("/");
              deb = chemin.substring(0, indexRacine + indexAntiSlashSuivant);
              liste.add(deb); /* ajoute la racine */

              /* finChemin = \rep\\ ... */
	      finChemin = finChemin.substring(index + 1);
              /* saute les antiSlash s'il y en a, finChemin = rep\\rep1\\rep2\\ ... */
              finChemin = ignoreSlash(finChemin);

              if (! finChemin.equals("")) {
                  index = chemin.indexOf(finChemin);
                  /* deb */
                  deb = chemin.substring(0, index);

                  String fin = finChemin;
                  while (ok) {
                      index = fin.indexOf("/");
                      if (index == -1) { /* on a un noeud */
                          if (deb.endsWith("/"))
                            deb = deb + fin;
                          else deb = deb + "/" + fin;
                          liste.add(deb);

                          ok = false;
                      }
                      else {
                        String node = fin.substring(0, index);
                        if (deb.endsWith("/"))
                            deb = deb + node;
                        else deb = deb + "/" + node;
                        liste.add(deb);

                        fin = fin.substring(index);
                        fin = ignoreSlash(fin);
                      }
                  } //fin while
              } //fin if
          } //fin else
      } //fin if
      return liste;
  }

/* navigPath */
String navigPath(Collection path, boolean linked, int beforeAfter) {
      StringBuilder linkedPathString = new StringBuilder();
      StringBuilder pathString = new StringBuilder();
      int nbItemInPath = path.size();
      Iterator iterator = path.iterator();
      boolean alreadyCut = false;
      int i = 0;
      while (iterator.hasNext()) {
            NodeDetail nodeInPath = (NodeDetail) iterator.next();
            if (! nodeInPath.getNodePK().getId().equals("0")) { // on n'affiche pas le theme acceuil
                if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)) {
                    String name = nodeInPath.getName();
                    if (name.length() > 20)
                            name = name.substring(0, 20) + "...";
                    linkedPathString.append("<a href=\"javascript:wsm.goToAppTopic('")
                        .append(nodeInPath.getNodePK().getId()).append("')\">")
                        .append(WebEncodeHelper.javaStringToHtmlString(name)).append("</a>");
                    pathString.append(WebEncodeHelper.javaStringToHtmlString(nodeInPath.getName()));
                    if (iterator.hasNext()) {
                            linkedPathString.append(" > ");
                            pathString.append(" > ");
                    }
            } else {
                    if (!alreadyCut) {
                            linkedPathString.append(" ... > ");
                            pathString.append(" ... > ");
                            alreadyCut = true;
                    }
            }
           }
           i++;
      }
      if (linked) {
        return linkedPathString.toString();
      } else {
        return pathString.toString();
      }
}


  /* displayPath */
  private String displayPath(Collection path, boolean linked, int beforeAfter, String lien, String racine) {
      StringBuilder linkedPathString = new StringBuilder();
      StringBuilder pathString = new StringBuilder();
      int nbItemInPath = path.size();
      Iterator iterator = path.iterator();
      boolean alreadyCut = false;
      boolean first = true;
      int i = 0;

      while (iterator.hasNext()) {
            String aPath = (String) iterator.next(); /* chemin complet */
            if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)) {
                if (first) {
                    linkedPathString.append("<a href=\"").append(lien).append(aPath).append("\">")
                        .append(racine).append("</a>");
                    pathString.append(racine);
                    first = false;
                }
                else {
                    linkedPathString.append("<a href=\"").append(lien).append(aPath).append("\">")
                        .append(node(aPath)).append("</a>");
                    pathString.append(node(aPath));
                }
                if (iterator.hasNext()) {
                      linkedPathString.append(" > ");
                      pathString.append(" > ");
                }
           } else {
                if (!alreadyCut) {
                      linkedPathString.append(" ... > ");
                      pathString.append(" ... > ");
                      alreadyCut = true;
                }
           }
           i++;
      }
      if (linked) {
        return linkedPathString.toString();
      } else {
        return pathString.toString();
      }
  }
%>