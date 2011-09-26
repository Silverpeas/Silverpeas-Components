package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.FileDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Handler for use case : user submitted search query.
 *
 * @author Ludovic Bertin
 *
 */
public class SearchHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Retrieves search query
    String wordSearch = (String) request.getParameter("WordSearch");
    SilverTrace.debug("silverCrawler", "SearchHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE", "wordSearch : "+wordSearch);

    // Performs search
    Collection<FileDetail> docs = sessionController.getResultSearch(wordSearch);

    // Stores results in request as attributes
    request.setAttribute("Path", sessionController.getPath());
    request.setAttribute("Docs", docs);
    request.setAttribute("Word", wordSearch);

    // returns page to redirect to
    return "viewResultSearch.jsp?ArrayPaneAction=ChangePage&ArrayPaneTarget=docs&ArrayPaneIndex=0";
  }

}
