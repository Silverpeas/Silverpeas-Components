<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ page import="java.lang.StringBuffer"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.node.model.NodeDetail"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ page import="org.silverpeas.util.EncodeHelper"%>


<%!

  SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
  
  /* getMachine */
  public String getMachine(HttpServletRequest request) {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    SettingBundle generalSettings = ResourceLocator.getGeneralBundle();

    String machine = settings.getString("Machine");
    String context = (generalSettings.getString("ApplicationURL")).substring(1);

    if (machine.equals("")) {
      HttpUtils u = new HttpUtils();
      StringBuffer url =  u.getRequestURL(request);

      ArrayList a = construitTab(url.toString());

      int j = 1;

      while (true) {
        if (j > a.size()) {
          break;
        }

        if (! a.get(j).equals(context)) {
          if (machine.equals(""))
            machine += a.get(j);
          else machine = machine + "/" + a.get(j);
        }
        else break;
        j++;
      }
    }
    return machine;
  }

  /* ignoreAntiSlash */
  public String ignoreAntiSlash(String chemin) {
    /* ex : \\\rep1\\rep2\\rep3 */
    /* res = rep1\\rep2\\re3 */

      String res = chemin;
      boolean ok = false;
      while (!ok) {
          char car = res.charAt(0);
          if (car == '\\') {
              res = res.substring(1);
          }
          else ok = true;
      }
      return res;

  }


  /* doubleAntiSlash */
  public String doubleAntiSlash(String chemin) {
        int i = 0;
        String res = chemin;
        boolean ok = true;

        while (ok) {
          int j = i + 1;
          if ((i < res.length()) && (j < res.length())) {
              char car1 = res.charAt(i);
              char car2 = res.charAt(j);

              if ( (car1 == '\\' && car2 == '\\') ||
                   (car1 != '\\' && car2 != '\\') ) {
              }
              else {
                      String avant = res.substring(0, j);
                      String apres = res.substring(j);
                      if ( (apres.startsWith("\\\\")) ||
                           (avant.endsWith("\\\\")) ) {
                      }
                      else {
                          res = avant + '\\' + apres;
                          i++;
                      }
              }
          }
          else {
              if (i < res.length()) {
                  char car = res.charAt(i);
                  if (car == '\\')
                      res = res + '\\';
              }
              ok = false;
          }
          i = i + 2;
        }
        return res;
  }

  /* supprDoubleAntiSlash */
  public String supprDoubleAntiSlash(String chemin) {
    /* ex : id\\rep1\\rep11\\rep111 */
    /* res = id\rep1\rep11\re111 */

      String res = "";
      int i = 0;

      while (i < chemin.length()) {
          char car = chemin.charAt(i);
          if (car == '\\') {
              res = res + car;
              i++;
          }
          else res = res + car;
          i++;
      }
      return res;
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

  /* finNode */
  public String finNode(WebSiteSessionController scc, String path) {
      /* ex : ....webSite17\\id\\rep1\\rep2\\rep3 */
      /* res : id\rep1\rep2\rep3 */

      int longueur = scc.getComponentId().length();
      int index = path.lastIndexOf(scc.getComponentId());
      String chemin = path.substring(index + longueur);

      chemin = ignoreAntiSlash(chemin);
      chemin = supprDoubleAntiSlash(chemin);

      return chemin;
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
      
      ArrayList liste = new ArrayList();
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
	  SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "afficherChemin : finChemin = "+finChemin);

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
	      SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "afficherChemin : finChemin = "+finChemin);

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
      String linkedPathString = new String();
      String pathString = new String();
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
                    linkedPathString += "<a href=\"javascript:onClick=topicGoTo('"+nodeInPath.getNodePK().getId()+"')\">"+EncodeHelper.javaStringToHtmlString(name)+"</a>";
                    pathString += EncodeHelper.javaStringToHtmlString(nodeInPath.getName());
                    if (iterator.hasNext()) {
                            linkedPathString += " > ";
                            pathString += " > ";
                    }
            } else {
                    if (!alreadyCut) {
                            linkedPathString += " ... > ";
                            pathString += " ... > ";
                            alreadyCut = true;
                    }
            }
           }
           i++;
      }
      if (linked)
          return linkedPathString;
      else
          return pathString;
}


  /* displayPath */
  private String displayPath(Collection path, boolean linked, int beforeAfter, String lien, String racine) {
      String linkedPathString = new String();
      String pathString = new String();
      int nbItemInPath = path.size();
      Iterator iterator = path.iterator();
      boolean alreadyCut = false;
      boolean first = true;
      int i = 0;

      while (iterator.hasNext()) {
            String aPath = (String) iterator.next(); /* chemin complet */
            if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)) {
                if (first) {
                    linkedPathString += "<a href=\""+lien+aPath+"\">"+racine+"</a>";
                    pathString += racine;
                    first = false;
                }
                else {
                    linkedPathString += "<a href=\""+lien+aPath+"\">"+node(aPath)+"</a>";
                    pathString += node(aPath);
                }
                if (iterator.hasNext()) {
                      linkedPathString += " > ";
                      pathString += " > ";
                }
           } else {
                if (!alreadyCut) {
                      linkedPathString += " ... > ";
                      pathString += " ... > ";
                      alreadyCut = true;
                }
           }
           i++;
      }
      if (linked)
          return linkedPathString;
      else
          return pathString;
  }

  /* sortCommun */
  private ArrayList sortCommun(ArrayList tabContexte, ArrayList tab) {
    /* tabContexte = [id | rep1 | rep2] */
    /* tab = [id | rep1 | rep3] */
    /* res = [id | rep1] */
      int i = 0;
      boolean ok = true;
      ArrayList array = new ArrayList();



      while (ok && i < tabContexte.size()) {
          String contenuContexte = (String) tabContexte.get(i);
          if (i < tab.size()) {
            String contenu = (String) tab.get(i);
            if (contenuContexte.equals(contenu)) {
              array.add(contenu);

            }
            else ok = false;
            i++;
          }
          else ok = false;
       }
       return array;
  }

  /* sortRester */
  private String sortReste(ArrayList tab, ArrayList tabCommun) {
    /* tab = [id | rep1 | rep2 | rep3] */
    /* tabCommun = [id | rep1] */
    /* res = rep2/rep3 */
      String res = "";



      int indice = tabCommun.size();

      while (indice < tab.size()) {
          String contenu = (String) tab.get(indice);
          res += contenu + "/";
          indice++;
       }

       if (! res.equals(""))
          res = res.substring(0, res.length() - 1);

       return res;
  }

  /* construitTab */
  private ArrayList construitTab(String deb) {
    /* deb = id/rep/  ou id\rep/*/
   /* res = [id | rep] */
      int i = 0;
      String noeud = "";
      ArrayList array = new ArrayList();



        while (i < deb.length()) {
            char car = deb.charAt(i);
            if (car == '/' || car == '\\') {
                array.add(noeud);

                noeud = "";
            }
            else {
                noeud += car;

            }
            i++;
         }
       return array;
  }

 /* parseCodeSupprImage */
 private String parseCodeSupprImage(WebSiteSessionController scc, String code, HttpServletRequest request, SettingBundle settings, String currentPath) {
    String theCode = code;
    String avant;
    String apres;
    int index;
    int longueur;
    String finChemin;
	String image = "<IMG border=0 src=\"http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/";
	int longueurImage = 19 + ("http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/").length();
	index = code.indexOf(image);
	if (index == -1) return theCode;
	else {
      avant = theCode.substring(0, index + 19);
      finChemin = theCode.substring(index + longueurImage);

      int indexGuillemet = finChemin.indexOf("\"");
      String absolute = finChemin.substring(0, indexGuillemet);

      apres = finChemin.substring(indexGuillemet);
      int indexSlash = absolute.lastIndexOf("/");
      String fichier = absolute.substring(indexSlash + 1);

      String deb = absolute.substring(0, indexSlash);
      ArrayList tab = construitTab(deb+"/");

      /* id/rep1 */
      String cheminContexte = finNode(scc, currentPath);
      ArrayList tabContexte = construitTab(cheminContexte+"/");
      ArrayList tabCommun = sortCommun(tabContexte, tab);
      String reste = sortReste(tab, tabCommun);
      int nbPas = tabContexte.size() - tabCommun.size();
      String relatif = "";
      int i = 0;
      while (i < nbPas) {
        relatif += "../";
        i++;
      }

      if (reste.equals(""))
        relatif += fichier;
      else relatif += reste + "/" + fichier;
      apres = relatif + apres;
      return (avant + parseCodeSupprImage(scc, apres, request, settings, currentPath));
  }
 }


 /* parseCodeSupprHref */
 /* ex : code = ...<a href="rr:icones/fleche.html"> <a href="http://www.etc"> <a href="aa:REP1/page.html">: liens deja en relatif (rr:) ou url externe (http://) ou liens en abslu
         res = ...<a href="icones/fleche.html"> <a href="http://www.etc"> <a href="page.html"> : tous les liens en relatifs ou urlk externe */
 private String parseCodeSupprHref(WebSiteSessionController scc, String code, SettingBundle settings, String currentPath) {
    String theCode = code;
    String avant;
    String apres;
    int index;
    String href = "<A href=\""; /* longueur de chaine = 9 */
    String resultat = theCode;
    String finChemin;
    String fichier;
    String deb;
    String theReturn = "";


  index = theCode.indexOf(href);
  if (index == -1) theReturn = theCode;

  else {

        avant = theCode.substring(0, index + 9);


        apres = theCode.substring(index + 9);


        if (apres.substring(0, 7).equals("http://")) { /* lien externe */
              theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
        }
        else if (apres.substring(0, 6).equals("ftp://")) { /* lien externe */
              theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
        }
        else if (apres.substring(0, 3).equals("rr:")) { /* deja en relatif */

              apres = apres.substring(3);

              theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
        }
        else if (apres.substring(0, 3).equals("aa:")) { /* lien absolu a transformer en relatif */

            /* finChemin = rep/coucou.html">... */
            finChemin = theCode.substring(index + 9 + 3);
            SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "finChemin = "+finChemin);

            /* traitement */
            int indexGuillemet = finChemin.indexOf("\"");
	    SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "indexGuillemet = "+new Integer(indexGuillemet).toString());

            /* absolute = rep/coucou.html */
           String absolute = finChemin.substring(0, indexGuillemet);
           SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "absolute = "+absolute);

            /* apres = ">... */
            apres = finChemin.substring(indexGuillemet);
            SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "apres = "+apres);

            int indexSlash = absolute.lastIndexOf("\\");
	    SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "indexSlash = "+new Integer(indexSlash).toString());

            if (indexSlash == -1) { /* pas d'arborescence, le fichier du lien est sur la racine */
                fichier = absolute;
                deb = "";
            }
            else {
              /* fichier = coucou.html */
              fichier = absolute.substring(indexSlash + 1);
              deb = absolute.substring(0, indexSlash);
            }
            ArrayList tab = construitTab(deb+"/"); /* dans ce tableau il manque l'id */

          /* cheminContexte = id/rep */
	      int longueur = scc.getComponentId().length();
	      int index2 = currentPath.lastIndexOf(scc.getComponentId());
	      String chemin = currentPath.substring(index2 + longueur);

	      chemin = chemin.substring(1);
	      chemin = supprDoubleAntiSlash(chemin);
	      String cheminContexte = chemin;
            ArrayList tabContexte = construitTab(cheminContexte+"/");
            /* ajoute l'id dans le premier tableau */
            tab.add(0, tabContexte.get(0));

            /* tabCommun = [id | rep] */
            ArrayList tabCommun = sortCommun(tabContexte, tab);

            /* reste = vide */
            String reste = sortReste(tab, tabCommun);

            /* nbPas = 0 */
            int nbPas = tabContexte.size() - tabCommun.size();
            String relatif = "";
            int i = 0;
            while (i < nbPas) {
              relatif += "../";
              i++;
            }

            if (reste.equals(""))
              relatif += fichier;
            else relatif += reste + "/" + fichier;

	        /* relatif = vide */
            apres = relatif + apres;
            theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
         }
      }
      return theReturn;
}
%>