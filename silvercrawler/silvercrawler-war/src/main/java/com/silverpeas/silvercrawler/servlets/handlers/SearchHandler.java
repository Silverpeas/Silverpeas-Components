package com.silverpeas.silvercrawler.servlets.handlers;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.FileDetail;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Handler for use case : user submitted search query.
 * @author Ludovic Bertin
 */
public class SearchHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request) throws Exception {

    String searchResult = request.getParameter("Id");
    if (StringUtil.isDefined(searchResult)) {
      sessionController.setCurrentPathFromResult(searchResult);
      // Go back to main page
      return HandlerProvider.getHandler("ViewDirectory").getDestination(sessionController, request);
    }

    // Retrieves search query
    String wordSearch = request.getParameter("WordSearch");
    SilverTrace.debug("silverCrawler", "SearchHandler.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "wordSearch : " + wordSearch);

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
