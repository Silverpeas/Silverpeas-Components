package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.HashMap;
import java.util.Map;

public class HandlerProvider {

  /**
   * Map the function name to the function handler
   */
  static private Map<String, FunctionHandler> handlerMap = null;

  /**
   * Inits the function handler
   */
  static {
    handlerMap = new HashMap<String, FunctionHandler>();

    handlerMap.put("Main", new InitHandler());

    handlerMap.put("SubDirectory", new SubDirectoryHandler());
    handlerMap.put("SubDirectoryFromResult", new SubDirectoryFromResultHandler());
    handlerMap.put("ViewDirectory", new ViewDirectoryHandler());
    handlerMap.put("GoToDirectory", new GoToDirectoryHandler());
    handlerMap.put("DownloadFolder", new DownloadFolderHandler());
    handlerMap.put("ViewDownloadHistory", new ViewDownloadHistoryHandler());
    handlerMap.put("ViewDownloadHistoryFromResult", new ViewDownloadHistoryFromResultHandler());
    handlerMap.put("ViewHistoryByUser", new ViewHistoryByUserHandler());
    handlerMap.put("IndexPath", new IndexPathHandler());
    handlerMap.put("IndexFile", new IndexFileHandler());
    handlerMap.put("IndexDirSelected", new IndexSelectedFoldersHandler());
    handlerMap.put("IndexFileSelected", new IndexSelectedFilesHandler());
    handlerMap.put("Search", new SearchHandler());
    handlerMap.put("ViewResult", new BackToSearchResultsHandler());
    handlerMap.put("portlet", new PortletHandler());
    handlerMap.put("ActivateRWaccess", new ActivateReadWriteAccessHandler());
    handlerMap.put("UnactivateRWaccess", new UnActivateReadWriteAccessHandler());
    handlerMap.put("RemoveFolder", new RemoveFolderHandler());
    handlerMap.put("RenameFolderForm", new RenameFolderFormHandler());
    handlerMap.put("RenameFolder", new RenameFolderHandler());
    handlerMap.put("RemoveFile", new RemoveFileHandler());
    handlerMap.put("RenameFileForm", new RenameFileFormHandler());
    handlerMap.put("RenameFile", new RenameFileHandler());
    handlerMap.put("CreateFolderForm", new CreateFolderFormHandler());
    handlerMap.put("CreateFolder", new CreateFolderHandler());
    handlerMap.put("UploadFileForm", new UploadFileFormHandler());
    handlerMap.put("UploadFile", new UploadFileHandler());
    handlerMap.put("RemoveSelectedFiles", new RemoveSelectedFilesHandler());
    handlerMap.put("RemoveSelectedFolders", new RemoveSelectedFoldersHandler());
    handlerMap.put("ProcessDragAndDrop", new ProcessDragAndDropHandler());
    handlerMap.put("ResolveConflicts", new ResolveConflictsHandler());
  }

  /**
   * Get specific handler for given use case
   *
   * @param useCase the use case
   *
   * @return  ready to use handler
   */
  public static FunctionHandler getHandler(String useCase) {
    if ( !handlerMap.containsKey(useCase) ) {
      return getHandler("Main");
    }
    return handlerMap.get(useCase);
  }
}
