package org.silverpeas.components.quickinfo.notification;

import java.util.Collection;

import org.silverpeas.components.quickinfo.model.News;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class NewsManualUserNotification extends AbstractNewsUserNotification {

  private UserDetail sender;
  
  public NewsManualUserNotification(News resource, final UserDetail sender) {
    super(resource, NotifAction.REPORT);
    this.sender = sender;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "GML.st.notification.from";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return null;
  }
  
  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }
  
  @Override
  protected String getFileName() {
    return "notification";
  }
  
  @Override
  protected String getSender() {
    return sender.getId();
  }
}
