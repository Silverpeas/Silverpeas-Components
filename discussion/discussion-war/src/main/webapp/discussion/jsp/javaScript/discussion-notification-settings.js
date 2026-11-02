/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* Keep personal desktop notification preferences synchronized between sessions. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const form = document.getElementById('discussion-notification-settings');
    const checkbox = document.getElementById('discussion-notifications-enabled');
    const error = document.getElementById('discussion-notification-error');
    const status = document.getElementById('discussion-notification-status');
    const permission = document.getElementById('discussion-desktop-enable');
    if (!discussion || !form || !checkbox) {
      return;
    }
    const root = window.top;
    let revision = -1;
    let current = true;
    let source;
    let stopped = false;
    const browserStatus = () => {
      let text = '';
      if (checkbox.checked && !root.Notification) {
        text = form.dataset.unsupported;
      } else if (checkbox.checked && root.Notification.permission === 'denied') {
        text = form.dataset.denied;
      }
      status.textContent = text;
      status.hidden = !text;
      permission.hidden = !checkbox.checked || !root.Notification || root.Notification.permission !== 'default';
    };
    const apply = value => {
      if (typeof value.enabled !== 'boolean' || !Number.isSafeInteger(value.revision) || value.revision < revision) {
        return;
      }
      revision = value.revision;
      current = value.enabled;
      checkbox.checked = current;
      browserStatus();
    };
    const synchronize = value => {
      const actual = root.DiscussionDesktop?.setPreference?.(discussion.dataset.instanceId, discussion.dataset.userId, value.enabled, value.revision);
      apply(actual || value);
    };
    const parse = response => {
      const page = new DOMParser().parseFromString(response.responseText, 'text/html');
      const state = page.querySelector('.discussion-notification-preference');
      if (!state || !['true','false'].includes(state.dataset.enabled) || !/^-?\d+$/.test(state.dataset.revision)) {
        throw new Error('Invalid notification setting');
      }
      const parsedRevision = Number(state.dataset.revision);
      if (!Number.isSafeInteger(parsedRevision) || parsedRevision < -1) {
        throw new Error('Invalid notification revision');
      }
      return {enabled:state.dataset.enabled === 'true', revision:parsedRevision};
    };
    const receive = event => {
      const value = event.detail;
      if (value.instanceId === discussion.dataset.instanceId && value.userId === discussion.dataset.userId) {
        apply(value);
      }
    };
    const refresh = async () => {
      if (stopped || checkbox.disabled) {
        return;
      }
      try {
        synchronize(parse(await sp.ajaxRequest(form.action).send()));
      } catch (failure) { error.hidden = false; }
    };
    const save = async () => {
      if (checkbox.disabled) {
        return;
      }
      checkbox.disabled = true;
      error.hidden = true;
      try {
        const response = await sp.ajaxRequest(form.action).byPostMethod().withParam('enabled', String(checkbox.checked)).send();
        synchronize(parse(response));
      } catch (failure) {
        checkbox.checked = current;
        error.hidden = false;
      } finally {
        checkbox.disabled = false;
        browserStatus();
      }
    };
    checkbox.addEventListener('change', save);
    form.addEventListener('submit', event => { event.preventDefault(); save(); });
    document.addEventListener('discussion:notification-permission', browserStatus);
    window.addEventListener('focus', browserStatus);
    const connect = () => {
      stopped = false;
      root.addEventListener('discussion:notification-setting', receive);
      source = root.spServerEventSource || window.spServerEventSource;
      if (source?.isEnabled()) {
        source.addEventListener('OPEN_EVENT_SOURCE', refresh, 'discussionNotificationSettingsOpen');
      }
      synchronize({enabled:form.dataset.enabled === 'true', revision:Number(form.dataset.revision)});
    };
    apply({enabled:form.dataset.enabled === 'true', revision:Number(form.dataset.revision)});
    window.addEventListener('pagehide', () => {
      stopped = true;
      root.removeEventListener('discussion:notification-setting', receive);
      source?.removeEventListener('OPEN_EVENT_SOURCE', refresh);
    });
    window.addEventListener('pageshow', event => { if (event.persisted) { connect(); refresh(); } });
    if (typeof whenSilverpeasReady === 'function') {
      whenSilverpeasReady(connect);
    } else {
      connect();
    }
  });
})();
