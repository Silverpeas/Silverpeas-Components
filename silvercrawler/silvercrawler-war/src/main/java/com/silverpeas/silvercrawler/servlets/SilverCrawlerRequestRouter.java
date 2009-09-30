package com.silverpeas.silvercrawler.servlets;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.FileFolder;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SilverCrawlerRequestRouter extends ComponentRequestRouter {
  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "SilverCrawler";
  }

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SilverCrawlerSessionController(mainSessionCtrl, componentContext);
  }

  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
    }
    return flag;
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    SilverCrawlerSessionController silverCrawlerSC = (SilverCrawlerSessionController) componentSC;
    SilverTrace.info("silverCrawler",
        "SilverCrawlerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId()
            + " Function=" + function);
    String rootDestination = "/silverCrawler/jsp/";

    String flag = getFlag(silverCrawlerSC.getUserRoles());
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", silverCrawlerSC.getUserId());

    try {
      if (function.startsWith("Main")) {
        // mise à jour du chemin initial
        silverCrawlerSC.setRootPath();

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("SubDirectory")) {
        // mise à jour du chemin courant
        String directory = request.getParameter("DirectoryPath");
        silverCrawlerSC.setCurrentPath(directory);

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("SubDirectoryFromResult")) {
        // mise à jour du chemin courant
        String directory = request.getParameter("DirectoryPath");

        silverCrawlerSC.setCurrentPathFromResult(directory);

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("ViewDirectory")) {
        // récupération du répertoire courant
        FileFolder currentFolder;
        if (flag.equals("admin"))
          currentFolder = silverCrawlerSC.getCurrentFolder(true);
        else
          currentFolder = silverCrawlerSC.getCurrentFolder(false);
        request.setAttribute("Folder", currentFolder);

        request.setAttribute("Path", silverCrawlerSC.getPath());
        request.setAttribute("IsDownload", silverCrawlerSC.isDownload());
        request.setAttribute("IsRootPath", new Boolean(silverCrawlerSC
            .isRootPath()));
        request.setAttribute("IsAllowedNav", silverCrawlerSC.isAllowedNav());
        request.setAttribute("RootPath", silverCrawlerSC.getRootPath());

        // récupération des paramètres pour la taille des tableaux (nb maxi de
        // répertoires et de fichiers à afficher par page)
        request.setAttribute("MaxDirectories", silverCrawlerSC
            .getNbMaxDirectoriesByPage());
        request.setAttribute("MaxFiles", silverCrawlerSC.getNbMaxFilesByPage());

        destination = rootDestination + "viewDirectory.jsp";
      } else if (function.equals("GoToDirectory")) {
        String directory = request.getParameter("DirectoryPath");
        silverCrawlerSC.goToDirectory(directory);

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("DownloadFolder")) {
        // récupération du chemin courant et mise à jour du chemin courant
        String folderName = (String) request.getParameter("FolderName");

        String[] zipInfo = silverCrawlerSC.zipFolder(folderName);

        String fileZip = zipInfo[0];

        long sizeZip = Long.parseLong(zipInfo[1]);
        Long sizeZipP = new Long(sizeZip);

        long sizeMax = Long.parseLong(zipInfo[2]);
        Long sizeMaxP = new Long(sizeMax);

        String url = zipInfo[3];

        request.setAttribute("Name", fileZip);
        request.setAttribute("ZipURL", url);
        request.setAttribute("Size", sizeZipP);
        request.setAttribute("SizeMax", sizeMaxP);

        destination = rootDestination + "download.jsp";
      } else if (function.equals("ViewDownloadHistory")) {
        String name = (String) request.getParameter("Name");

        request.setAttribute("Downloads", silverCrawlerSC
            .getHistoryByFolder(name));
        request.setAttribute("Name", name);

        destination = rootDestination + "history.jsp";
      } else if (function.equals("ViewDownloadHistoryFromResult")) {
        String path = (String) request.getParameter("Name");

        request.setAttribute("Downloads", silverCrawlerSC
            .getHistoryByFolderFromResult(path));

        // si on viens de la recherche on a tout le chemin dans "name"
        // extraction du nom pour le passer en paramètre à history.jsp
        String name = silverCrawlerSC.getNameFromPath(path);

        request.setAttribute("Name", name);

        destination = rootDestination + "history.jsp";
      } else if (function.equals("ViewHistoryByUser")) {
        String userId = (String) request.getParameter("UserId");
        String userName = (String) request.getParameter("UserName");
        String folderName = (String) request.getParameter("FolderName");

        request.setAttribute("DownloadsByUser", silverCrawlerSC
            .getHistoryByUser(folderName, userId));
        request.setAttribute("UserName", userName);
        request.setAttribute("UserId", userId);
        request.setAttribute("FolderName", folderName);

        destination = rootDestination + "historyByUser.jsp";
      } else if (function.equals("IndexPath")) {
        String folderName = (String) request.getParameter("FolderName");

        silverCrawlerSC.indexPath(folderName);

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("IndexFile")) {
        String fileName = (String) request.getParameter("FileName");

        silverCrawlerSC.indexFile(fileName);

        destination = getDestination("ViewDirectory", silverCrawlerSC, request);
      } else if (function.equals("IndexDirSelected")) {
        SilverTrace.debug("silverCrawler",
            "SilverCrawlerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "IndexSelected");

        if (flag.equals("admin")) {
          String[] checkdirectories = request.getParameterValues("checkedDir");
          SilverTrace.info("silverCrawler",
              "SilverCrawlerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "checkdirectories = "
                  + checkdirectories.length);
          Collection listDirToIndex = new ArrayList();
          if (checkdirectories != null) {
            for (int i = 0; i < checkdirectories.length; i++) {
              String dirName = checkdirectories[i];
              SilverTrace.info("silverCrawler",
                  "SilverCrawlerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "dirName = " + dirName);
              listDirToIndex.add(dirName);
            }
          }
          silverCrawlerSC.indexPathSelected(listDirToIndex);
          destination = getDestination("ViewDirectory", silverCrawlerSC,
              request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      } else if (function.equals("IndexFileSelected")) {
        SilverTrace.debug("silverCrawler",
            "SilverCrawlerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "IndexSelected");

        if (flag.equals("admin")) {
          String[] checkfiles = request.getParameterValues("checkedFile");
          SilverTrace.info("silverCrawler",
              "SilverCrawlerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "checkfiles = " + checkfiles.length);
          Collection listFileToIndex = new ArrayList();
          if (checkfiles != null) {
            for (int i = 0; i < checkfiles.length; i++) {
              String fileName = checkfiles[i];
              SilverTrace.info("silverCrawler",
                  "SilverCrawlerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "fileName = " + fileName);
              listFileToIndex.add(fileName);
            }
          }
          silverCrawlerSC.filePathSelected(listFileToIndex);
          destination = getDestination("ViewDirectory", silverCrawlerSC,
              request);
        } else
          destination = "/admin/jsp/errorpage.jsp";
      } else if (function.equals("Search")) {
        String wordSearch = (String) request.getParameter("WordSearch");
        SilverTrace.info("silverCrawler",
            "SilverCrawlerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "word =" + wordSearch);

        Collection docs = silverCrawlerSC.getResultSearch(wordSearch);

        request.setAttribute("Path", silverCrawlerSC.getPath());
        request.setAttribute("Docs", docs);
        request.setAttribute("Word", wordSearch);

        destination = rootDestination
            + "viewResultSearch.jsp?ArrayPaneAction=ChangePage&ArrayPaneTarget=docs&ArrayPaneIndex=0";
      } else if (function.equals("ViewResult")) {
        String wordSearch = (String) request.getParameter("WordSearch");

        Collection docs = silverCrawlerSC.getCurrentResultSearch();

        request.setAttribute("Path", silverCrawlerSC.getPath());
        request.setAttribute("Docs", docs);
        request.setAttribute("Word", wordSearch);

        destination = rootDestination + "viewResultSearch.jsp";
      } else if (function.equals("portlet")) {
        // récupération du répertoire courant
        silverCrawlerSC.setRootPath();
        request.setAttribute("Folder", silverCrawlerSC.getCurrentFolder());

        request.setAttribute("Path", silverCrawlerSC.getPath());
        request.setAttribute("IsDownload", silverCrawlerSC.isDownload());
        request.setAttribute("IsRootPath", new Boolean(silverCrawlerSC
            .isRootPath()));
        request.setAttribute("IsAllowedNav", silverCrawlerSC.isAllowedNav());
        request.setAttribute("RootPath", silverCrawlerSC.getRootPath());

        destination = rootDestination + "portlet.jsp";
      } else {
        destination = rootDestination + "viewDirectory.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("silverCrawler",
        "SilverCrawlerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }
}
