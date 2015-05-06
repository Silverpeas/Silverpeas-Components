package org.silverpeas.components.quickinfo.notification;

import com.silverpeas.notification.builder.UserSubscriptionNotificationBehavior;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ResourceSubscriptionProvider;
import com.silverpeas.subscribe.util.SubscriptionSubscriberMapBySubscriberType;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import org.silverpeas.components.quickinfo.model.News;

import java.util.Collection;

public class QuickInfoSubscriptionUserNotification extends AbstractNewsUserNotification
    implements UserSubscriptionNotificationBehavior {

  private final SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
      new SubscriptionSubscriberMapBySubscriberType();

  public QuickInfoSubscriptionUserNotification(News resource, NotifAction action) {
    super(resource, action);
  }
  
  @Override
  protected void initialize() {
    super.initialize();
    subscriberIdsByTypes.addAll(ResourceSubscriptionProvider
        .getSubscribersOfComponent(getResource().getComponentInstanceId()));
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
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }
}