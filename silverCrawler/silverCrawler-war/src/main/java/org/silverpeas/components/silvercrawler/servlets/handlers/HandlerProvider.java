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

package org.silverpeas.components.silvercrawler.servlets.handlers;

import jakarta.annotation.PostConstruct;
import org.silverpeas.core.annotation.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class HandlerProvider {

  /**
   * Map the function name to the function handler
   */
  private final Map<String, FunctionHandler> handlerMap = new HashMap<>();

  /**
   * Inits the function handler
   */
  @PostConstruct
  void setUpHandlers() {
    handlerMap.put("Main", new InitHandler(this));
    handlerMap.put("SubDirectory", new SubDirectoryHandler(this));
    handlerMap.put("SubDirectoryFromResult", new SubDirectoryFromResultHandler(this));
    handlerMap.put("ViewDirectory", new ViewDirectoryHandler(this));
    handlerMap.put("GoToDirectory", new GoToDirectoryHandler(this));
    handlerMap.put("DownloadFolder", new DownloadFolderHandler(this));
    handlerMap.put("ViewDownloadHistory", new ViewDownloadHistoryHandler(this));
    handlerMap.put("ViewDownloadHistoryFromResult", new ViewDownloadHistoryFromResultHandler(this));
    handlerMap.put("ViewHistoryByUser", new ViewHistoryByUserHandler(this));
    handlerMap.put("IndexPath", new IndexPathHandler(this));
    handlerMap.put("IndexFile", new IndexFileHandler(this));
    handlerMap.put("IndexDirSelected", new IndexSelectedFoldersHandler(this));
    handlerMap.put("IndexFileSelected", new IndexSelectedFilesHandler(this));
    handlerMap.put("Search", new SearchHandler(this));
    handlerMap.put("searchResult", new SearchHandler(this));
    handlerMap.put("ViewResult", new BackToSearchResultsHandler(this));
    handlerMap.put("portlet", new PortletHandler(this));
    handlerMap.put("ActivateRWaccess", new ActivateReadWriteAccessHandler(this));
    handlerMap.put("UnactivateRWaccess", new UnActivateReadWriteAccessHandler(this));
    handlerMap.put("RemoveFolder", new RemoveFolderHandler(this));
    handlerMap.put("RenameFolderForm", new RenameFolderFormHandler(this));
    handlerMap.put("RenameFolder", new RenameFolderHandler(this));
    handlerMap.put("RemoveFile", new RemoveFileHandler(this));
    handlerMap.put("RenameFileForm", new RenameFileFormHandler(this));
    handlerMap.put("RenameFile", new RenameFileHandler(this));
    handlerMap.put("CreateFolderForm", new CreateFolderFormHandler(this));
    handlerMap.put("CreateFolder", new CreateFolderHandler(this));
    handlerMap.put("UploadFileForm", new UploadFileFormHandler(this));
    handlerMap.put("UploadFile", new UploadFileHandler(this));
    handlerMap.put("RemoveSelectedFiles", new RemoveSelectedFilesHandler(this));
    handlerMap.put("RemoveSelectedFolders", new RemoveSelectedFoldersHandler(this));
    handlerMap.put("ProcessDragAndDrop", new ProcessDragAndDropHandler(this));
    handlerMap.put("ResolveConflicts", new ResolveConflictsHandler(this));
  }

  /**
   * Get specific handler for given use case
   * @param useCase the use case
   * @return ready to use handler
   */
  public FunctionHandler getHandler(String useCase) {
    if (!handlerMap.containsKey(useCase)) {
      return getHandler("Main");
    }
    return handlerMap.get(useCase);
  }

  private HandlerProvider() {

  }
}
