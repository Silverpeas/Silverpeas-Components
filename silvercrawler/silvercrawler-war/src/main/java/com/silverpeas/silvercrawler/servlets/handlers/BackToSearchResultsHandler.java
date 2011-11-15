package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.FileDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Handler for use case : user goes back to last search results.
 *
 * @author Ludovic Bertin
 *
 */
public class BackToSearchResultsHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves search query
    String wordSearch = (String) request.getParameter("WordSearch");
    SilverTrace.debug("silverCrawler", "SearchHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "wordSearch : "+wordSearch);

    // Retrieves last results
    Collection<FileDetail> docs = sessionController.getCurrentResultSearch();

    // Stores objects in request as attributes
    request.setAttribute("Path", sessionController.getPath());
    request.setAttribute("Docs", docs);
    request.setAttribute("Word", wordSearch);

    // returns page to redirect to
    return "viewResultSearch.jsp";
  }

}
