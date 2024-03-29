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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.web.mvc.util.WysiwygEditorConfigRegistry;
import org.silverpeas.core.web.util.WysiwygEditorConfig;

/**
 * Initializes some resources required by infoLetter.
 * @author silveryocha
 */
public class InfoLetterInitialization implements Initialization {

  private static final String DEFAULT_COMPONENT_NAME = "infoLetter";

  @Override
  public void init() {
    WysiwygEditorConfig wysiwygEditorConfig = new WysiwygEditorConfig(DEFAULT_COMPONENT_NAME);
    wysiwygEditorConfig.setToolbar(DEFAULT_COMPONENT_NAME);
    WysiwygEditorConfigRegistry.get().register(DEFAULT_COMPONENT_NAME, wysiwygEditorConfig);
  }
}
  