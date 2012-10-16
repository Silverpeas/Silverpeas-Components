/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.silvercrawler.servlets;

import com.silverpeas.silvercrawler.control.ProfileHelper;
import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.servlets.handlers.FunctionHandler;
import com.silverpeas.silvercrawler.servlets.handlers.HandlerProvider;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;

public class SilverCrawlerRequestRouter extends ComponentRequestRouter<SilverCrawlerSessionController> {
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
   *
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param silverCrawlerSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      SilverCrawlerSessionController silverCrawlerSC, HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("silverCrawler", "SilverCrawlerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + silverCrawlerSC.getUserId()
            + " Function=" + function);

    String flag = getFlag(silverCrawlerSC.getUserRoles());
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", silverCrawlerSC.getUserId());
    request.setAttribute("Language", silverCrawlerSC.getLanguage());

    // Delegate to specific Handler
    FunctionHandler handler = HandlerProvider.getHandler(function);
    destination = handler.computeDestination(silverCrawlerSC, request);

    SilverTrace.info("silverCrawler",
        "SilverCrawlerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);

    return destination;
  }
}
