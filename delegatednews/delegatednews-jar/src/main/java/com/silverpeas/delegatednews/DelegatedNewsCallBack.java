/**
 * Copyright (C) 2000 - 2009 Silverpeas

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
package com.silverpeas.delegatednews;

import java.util.Calendar;
import java.util.Date;

import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DelegatedNewsCallBack implements CallBack {

  private DelegatedNewsService delegatedNewsService = null;
  private OrganizationController organizationController = null;

  public DelegatedNewsCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int pubId, String instanceId,
      Object extraParam) {
    SilverTrace.info("delegatednews", "DelegatedNewsCallBack.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", pubId = "
            + pubId + ", instanceId = " + instanceId + ", extraParam = "
            + extraParam.toString());

    if (pubId == -1) {
      SilverTrace.info("delegatednews", "DelegatedNewsCallBack.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "pubId is null. Callback stopped ! action = " + action
              + ", instanceId = " + instanceId + ", extraParam = "
              + extraParam.toString());
      return;
    }

    if (action == CallBackManager.ACTION_HEADER_PUBLICATION_UPDATE) {

      PublicationDetail pubDetail = (PublicationDetail) extraParam;
      DelegatedNews delegatedNews = getDelegatedNewsService().getDelegatedNews(pubId);
      if (delegatedNews != null) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        Date dateHourBegin = DateUtil.getDate(pubDetail.getBeginDate(), pubDetail.getBeginHour());
        Date dateHourEnd = DateUtil.getDate(pubDetail.getEndDate(), pubDetail.getEndHour());
     
        if (dateHourBegin != null && dateHourBegin.after(today)) {
          pubDetail.setNotYetVisible(true);
        } else if (dateHourEnd != null && dateHourEnd.before(today)) {
          pubDetail.setNoMoreVisible(true);
        }
        
        // supprime l'actualité si la publication n'est plus visible (les dates de visibilité ont été modifiées sur la publication) 
        if (! pubDetail.isVisible()) {
          getDelegatedNewsService().deleteDelegatedNews(pubId);
        } 
        else {
        
          // met à jour l'actualité
          getDelegatedNewsService().updateDelegatedNews(pubId, instanceId,
              DelegatedNews.NEWS_TO_VALIDATE, pubDetail.getUpdaterId(), null, new Date(),
              dateHourBegin, dateHourEnd);
  
          // alerte l'équipe éditoriale
          String[] tabInstanceId = getOrganizationController().getCompoId("delegatednews");
          String delegatednewsInstanceId = null;
          for (String element : tabInstanceId) {
            delegatednewsInstanceId = element;
            break;
          }
  
          UserDetail updaterUserDetail =
              getOrganizationController().getUserDetail(pubDetail.getUpdaterId());
          String updaterUserName = "";
          if (updaterUserDetail.getFirstName() != null) {
            updaterUserName = updaterUserDetail.getFirstName() + " ";
          }
          if (updaterUserDetail.getLastName() != null) {
            updaterUserName += updaterUserDetail.getLastName();
          }
  
          getDelegatedNewsService().notifyDelegatedNewsToValidate(Integer.toString(pubId),
              pubDetail.getName(DisplayI18NHelper.getDefaultLanguage()), pubDetail.getUpdaterId(),
              updaterUserName, delegatednewsInstanceId);
        }
      }
    } else if (action == CallBackManager.ACTION_PUBLICATION_REMOVE) {
        getDelegatedNewsService().deleteDelegatedNews(pubId);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_HEADER_PUBLICATION_UPDATE, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_PUBLICATION_REMOVE, this);
  }

  /**
   * @return
   */
  private DelegatedNewsService getDelegatedNewsService() {
    if (this.delegatedNewsService == null) {
      this.delegatedNewsService = ServicesFactory.getDelegatedNewsService();
    }
    return this.delegatedNewsService;
  }

  /**
   * @return
   */
  public OrganizationController getOrganizationController() {
    if (this.organizationController == null) {
      this.organizationController = new OrganizationController();
    }
    return this.organizationController;
  }

}