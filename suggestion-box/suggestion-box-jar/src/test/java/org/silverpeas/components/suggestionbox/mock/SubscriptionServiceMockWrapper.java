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
package org.silverpeas.components.suggestionbox.mock;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import org.mockito.Mockito;

import java.util.Collection;

/**
 * @author: Yohann Chastagnier
 */
public class SubscriptionServiceMockWrapper implements SubscriptionService {

  private final SubscriptionService mock = Mockito.mock(SubscriptionService.class);

  public SubscriptionService getMock() {
    return mock;
  }

  @Override
  public void subscribe(final Subscription subscription) {
    mock.subscribe(subscription);
  }

  @Override
  public void subscribe(final Collection<? extends Subscription> subscriptions) {
    mock.subscribe(subscriptions);
  }

  @Override
  public void unsubscribe(final Subscription subscription) {
    mock.unsubscribe(subscription);
  }

  @Override
  public void unsubscribe(final Collection<? extends Subscription> subscriptions) {
    unsubscribe(subscriptions);
  }

  @Override
  public void unsubscribeBySubscriber(final SubscriptionSubscriber subscriber) {
    mock.unsubscribeBySubscriber(subscriber);
  }

  @Override
  public void unsubscribeBySubscribers(
      final Collection<? extends SubscriptionSubscriber> subscribers) {
    mock.unsubscribeBySubscribers(subscribers);
  }

  @Override
  public void unsubscribeByResource(final SubscriptionResource resource) {
    mock.unsubscribeByResource(resource);
  }

  @Override
  public void unsubscribeByResources(final Collection<? extends SubscriptionResource> resources) {
    mock.unsubscribeByResources(resources);
  }

  @Override
  public boolean existsSubscription(final Subscription subscription) {
    return mock.existsSubscription(subscription);
  }

  @Override
  public Collection<Subscription> getByResource(final SubscriptionResource resource) {
    return mock.getByResource(resource);
  }

  @Override
  public Collection<Subscription> getByResource(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    return mock.getByResource(resource, method);
  }

  @Override
  public Collection<Subscription> getByUserSubscriber(final String userId) {
    return mock.getByUserSubscriber(userId);
  }

  @Override
  public Collection<Subscription> getBySubscriber(final SubscriptionSubscriber subscriber) {
    return mock.getBySubscriber(subscriber);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndComponent(
      final SubscriptionSubscriber subscriber, final String instanceId) {
    return mock.getBySubscriberAndComponent(subscriber, instanceId);
  }

  @Override
  public Collection<Subscription> getBySubscriberAndResource(
      final SubscriptionSubscriber subscriber, final SubscriptionResource resource) {
    return mock.getBySubscriberAndResource(subscriber, resource);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(final SubscriptionResource resource) {
    return mock.getSubscribers(resource);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(final SubscriptionResource resource,
      final SubscriptionMethod method) {
    return mock.getSubscribers(resource, method);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      final Collection<? extends SubscriptionResource> resources) {
    return mock.getSubscribers(resources);
  }

  @Override
  public SubscriptionSubscriberList getSubscribers(
      final Collection<? extends SubscriptionResource> resources, final SubscriptionMethod method) {
    return mock.getSubscribers(resources, method);
  }

  @Override
  public boolean isSubscriberSubscribedToResource(final SubscriptionSubscriber subscriber,
      final SubscriptionResource resource) {
    return mock.isSubscriberSubscribedToResource(subscriber, resource);
  }

  @Override
  public boolean isUserSubscribedToResource(final String user,
      final SubscriptionResource resource) {
    return mock.isUserSubscribedToResource(user, resource);
  }
}
