/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums.forumsManager.ejb.mock;

import com.silverpeas.annotation.Service;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import org.mockito.Mockito;

import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
@Service
public class SubscriptionServiceMock implements SubscriptionService {

  private final SubscriptionService mock = Mockito.mock(SubscriptionService.class);

  public SubscriptionService getMock() {
    return mock;
  }

  @Override
  public void subscribe(Subscription subscription) {
    mock.subscribe(subscription);
  }

  @Override
  public void subscribe(Collection<? extends Subscription> subscriptions) {
    mock.subscribe(subscriptions);
  }

  @Override
  public void unsubscribe(Subscription subscription) {
    mock.unsubscribe(subscription);
  }

  @Override
  public void unsubscribe(Collection<? extends Subscription> subscriptions) {
    mock.unsubscribe(subscriptions);
  }

  @Override
  public void unsubscribeBySubscriber(SubscriptionSubscriber subscriber) {
    mock.unsubscribeBySubscriber(subscriber);
  }

  @Override
  public void unsubscribeBySubscribers(Collection<? extends SubscriptionSubscriber> subscribers) {
    mock.unsubscribeBySubscribers(subscribers);
  }

  @Override
  public void unsubscribeByResource(SubscriptionResource resource) {
    mock.unsubscribeByResource(resource);
  }

  @Override
  public void unsubscribeByResources(Collection<? extends SubscriptionResource> resources) {
    mock.unsubscribeByResources(resources);
  }

  @Override
  public boolean existsSubscription(Subscription subscription) {
    return mock.existsSubscription(subscription);
  }

  @Override
  public Collection<Subscription> getByResource(SubscriptionResource resource) {
    return mock.getByResource(resource);
  }

  @Override
  public Collection<Subscription> getByResource(SubscriptionResource resource,
      SubscriptionMethod method) {
    return mock.getByResource(resource, method);
  }

  @Override
  public Collection<Subscription> getByUserSubscriber(String userId) {
    return mock.getByUserSubscriber(userId);
  }

  @Override
  public Collection<Subscription> getBySubscriber(SubscriptionSubscriber subscriber) {
    return mock.getBySubscriber(subscriber);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndComponent(SubscriptionSubscriber subscriber,
      String instanceId) {
    return mock.getBySubscriberAndComponent(subscriber, instanceId);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource) {
    return mock.getBySubscriberAndResource(subscriber, resource);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(SubscriptionResource resource) {
    return mock.getSubscribers(resource);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(SubscriptionResource resource,
      SubscriptionMethod method) {
    return mock.getSubscribers(resource, method);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      Collection<? extends SubscriptionResource> resources) {
    return mock.getSubscribers(resources);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      Collection<? extends SubscriptionResource> resources, SubscriptionMethod method) {
    return mock.getSubscribers(resources, method);
  }

  @Override
  public boolean isSubscriberSubscribedToResource(SubscriptionSubscriber subscriber,
      SubscriptionResource resource) {
    return mock.isSubscriberSubscribedToResource(subscriber, resource);
  }

  @Override
  public boolean isUserSubscribedToResource(String user, SubscriptionResource resource) {
    return mock.isUserSubscribedToResource(user, resource);
  }
}
