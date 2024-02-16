/*
 * Copyright (C) 2000 - 2024 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.websites.dao.SiteDAO;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.core.util.file.FileFolderManager;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;

/**
 * Deletes all the web site managed by the WebSites instance that is being deleted.
 * @author mmoquillon
 */
@Named
public class WebSitesInstancePreDestruction implements ComponentInstancePreDestruction {
  /**
   * Performs pre destruction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try {
      SiteDAO siteDAO = new SiteDAO(componentInstanceId);
      siteDAO.deleteAllWebSites();
      deleteAttachmentsAndImagesDirectory(componentInstanceId);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void deleteAttachmentsAndImagesDirectory(String componentId)
      throws java.lang.Exception {
    final SettingBundle uploadSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    FileFolderManager.deleteFolder(uploadSettings.getString("uploadsPath") + File.separator +
        componentId);
  }
}
