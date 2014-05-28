package org.silverpeas.components.quickinfo.notification;

import java.util.Collection;
import java.util.Map;

import org.silverpeas.components.quickinfo.model.News;

import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;

public class QuickInfoSubscriptionUserNotification extends AbstractNewsUserNotification {
  
  private final Map<SubscriberType, Collection<String>> subscriberIdsByTypes = SubscriptionUtil.
      indexSubscriberIdsByType(null);

  public QuickInfoSubscriptionUserNotification(News resource, NotifAction action) {
    super(resource, action);
  }
  
  @Override
  protected void initialize() {
    super.initialize();
    SubscriptionUtil.indexSubscriberIdsByType(subscriberIdsByTypes, getSubscribeBm().getSubscribers(
        ComponentSubscriptionResource.from(getResource().getComponentInstanceId())));
    subscriberIdsByTypes.get(SubscriberType.USER).remove(getSender());
  }

  @Override
  protected String getBundleSubjectKey() {
    return "GML.subscription";
  }
  
  @Override
  protected String getFileName() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "subscriptionNotificationOnCreate";
    }
    return "subscriptionNotificationOnUpdate";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER);
  }
}