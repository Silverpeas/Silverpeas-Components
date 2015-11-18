/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.yellowpages.control.ejb.YellowpagesBm;

/**
 * @author neysseri
 */
public class YellowpagesCallBack implements CallBack {

  public YellowpagesCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String componentId, Object extraParam) {
    SilverTrace.info("yellowpages", "YellowpagesCallBack.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
        "action = " + action + ", iParam = " + iParam + ", componentId = " + componentId);

    if (iParam == -1) {
      SilverTrace.info("yellowpages", "YellowpagesCallBack.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "groupId is null. Callback stopped ! action = " + action);
      return;
    }

    // remove group in all 'yellowpages' app
    getEJB().removeGroup(String.valueOf(iParam));
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_GROUP, this);
  }

  private YellowpagesBm getEJB() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.YELLOWPAGESBM_EJBHOME, YellowpagesBm.class);
  }
}