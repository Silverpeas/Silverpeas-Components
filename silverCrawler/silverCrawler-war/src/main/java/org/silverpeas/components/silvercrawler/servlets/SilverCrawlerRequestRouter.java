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
package org.silverpeas.components.silvercrawler.servlets;

import org.silverpeas.components.silvercrawler.control.ProfileHelper;
import org.silverpeas.components.silvercrawler.control.SilverCrawlerSessionController;
import org.silverpeas.components.silvercrawler.servlets.handlers.FunctionHandler;
import org.silverpeas.components.silvercrawler.servlets.handlers.HandlerProvider;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

public class SilverCrawlerRequestRouter
    extends ComponentRequestRouter<SilverCrawlerSessionController> {

  private static final long serialVersionUID = 3258391347331914529L;

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
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   *
   */
  public SilverCrawlerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SilverCrawlerSessionController(mainSessionCtrl, componentContext);
  }

  public String getFlag(String[] profiles) {
    return ProfileHelper.getBestProfile(profiles);
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param silverCrawlerSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, SilverCrawlerSessionController silverCrawlerSC,
      HttpRequest request) {
    String destination = "";
    String flag = getFlag(silverCrawlerSC.getUserRoles());
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", silverCrawlerSC.getUserId());
    request.setAttribute("Language", silverCrawlerSC.getLanguage());

    // Delegate to specific Handler
    FunctionHandler handler = HandlerProvider.getHandler(function);
    destination = handler.computeDestination(silverCrawlerSC, request);



    return destination;
  }
}
