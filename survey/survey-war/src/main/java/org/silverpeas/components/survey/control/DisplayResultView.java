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

package org.silverpeas.components.survey.control;

import org.silverpeas.core.util.StringUtil;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Definition of the different views of result page a survey can display.
 * @author silveryocha
 */
public enum DisplayResultView {
  CLASSIC_GRAPHICAL(Constants.CLASSIC_MAIN_VIEW, Constants.GRAPHICAL_SEC_LEVEL_VIEW),
  CLASSIC_HORIZONTAL_BAR(Constants.CLASSIC_MAIN_VIEW, Constants.HORIZONTAL_BAR_SEC_LEVEL_VIEW),
  DETAIL(Constants.DETAIL_MAIN_VIEW, StringUtil.EMPTY);

  private final String mainLevelView;
  private final String secondaryLevelView;

  DisplayResultView(final String mainView, final String secondaryLevelView) {
    this.mainLevelView = mainView;
    this.secondaryLevelView = secondaryLevelView;
  }

  /**
   * Gets the {@link DisplayResultView} instance that matches the specified result view name or code.
   * @param name the enum name or the full name of a predefined result view in survey component.
   * @return the {@link DisplayResultView} instance having as name the specified role name or
   * {@link DisplayResultView#DETAIL} if no such role exists.
   */
  public static DisplayResultView fromString(String name) {
    if (StringUtil.isNotDefined(name)) {
      return DETAIL;
    }
    final String trimmedName = name.trim();
    return Stream.of(values())
        .filter(r -> r.name().equalsIgnoreCase(trimmedName) || r.getIdentifier().equals(trimmedName))
        .findFirst()
        .orElse(DETAIL);
  }

  /**
   * Gets a list of {@link DisplayResultView} instance from a main view part of identifier.
   * @param mainView a main view part of identifier.
   * @return a list of {@link DisplayResultView} instance.
   */
  public static List<DisplayResultView> fromMainViewOnly(String mainView) {
    final String trimmedMainCode = mainView.trim();
    return Stream.of(values())
        .filter(r -> r.mainLevelView.equalsIgnoreCase(trimmedMainCode))
        .collect(toList());
  }

  /**
   * The code of the view is a combination of the main and sub view separated by an underscore.
   * @return a code as string.
   */
  public String getIdentifier() {
    return mainLevelView + "_" + secondaryLevelView;
  }

  /**
   * Gets the main view.
   * @return a string.
   */
  public String getMainView() {
    return mainLevelView;
  }

  /**
   * Gets the secondary level view.
   * @return a string.
   */
  public String getSecondaryLevelView() {
    return secondaryLevelView;
  }

  /**
   * Gets the bundle key of main view.
   * @return a string representing a bundle key.
   */
  public String getMainViewBundleKey() {
    return String.format("survey.result.view.%s", mainLevelView);
  }

  /**
   * Gets the bundle key of secondary level view.
   * @return a string representing a bundle key.
   */
  public String getSecondaryViewBundleKey() {
    return String.format("survey.result.view.%s", secondaryLevelView);
  }

  public static class Constants {
    public static final String CLASSIC_MAIN_VIEW = "classic";
    public static final String DETAIL_MAIN_VIEW = "detail";
    public static final String GRAPHICAL_SEC_LEVEL_VIEW = "graphical";
    public static final String HORIZONTAL_BAR_SEC_LEVEL_VIEW = "horizontal-bar";

    private Constants() {
    }
  }
}
