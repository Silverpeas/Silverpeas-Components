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
/* Browser-local display and last-channel preferences; explicit navigation remains authoritative. */
(() => {
  'use strict';
  const sections = ['discussion-participants-section', 'discussion-publications-section',
    'discussion-pins-section', 'discussion-files-section', 'discussion-notifications-section'];
  const key = () => {
    const discussion = document.getElementById('discussion');
    if (!discussion?.dataset.userId || !discussion.dataset.instanceId || window.DiscussionContext?.embedded) {
      return undefined;
    }
    return 'silverpeas:discussion:view:v1:' + encodeURIComponent(discussion.dataset.userId)
        + ':' + encodeURIComponent(discussion.dataset.instanceId);
  };
  const read = () => {
    try {
      const storageKey = key();
      const value = storageKey ? JSON.parse(localStorage.getItem(storageKey) || '{}') : {};
      if (!value || typeof value !== 'object' || Array.isArray(value)) { return {}; }
      const result = {};
      if (typeof value.channels === 'boolean') { result.channels = value.channels; }
      if (typeof value.selectedChannel === 'string' && /^[a-zA-Z0-9-]{1,40}$/.test(value.selectedChannel)) {
        result.selectedChannel = value.selectedChannel;
      }
      if (value.accordions && typeof value.accordions === 'object') {
        result.accordions = Object.fromEntries(sections.filter(id => typeof value.accordions[id] === 'boolean')
            .map(id => [id, value.accordions[id]]));
      }
      return result;
    } catch (unavailable) { return {}; }
  };
  const save = value => {
    try {
      const storageKey = key();
      if (storageKey) { localStorage.setItem(storageKey, JSON.stringify(value)); }
    } catch (unavailable) {
      // Storage may be disabled or full; navigation and native accordions must still work.
    }
  };
  window.DiscussionViewState = Object.freeze({
    channelsOpen: fallback => read().channels ?? fallback,
    rememberChannels: open => save({...read(), channels: open})
  });

  document.addEventListener('DOMContentLoaded', () => {
    if (!key()) { return; }
    const discussion = document.getElementById('discussion');
    const current = discussion.dataset.channelScope;
    const selected = read().selectedChannel;
    if (discussion.dataset.restoreChannel === 'true' && selected && selected !== current) {
      // Use a server-rendered active-channel link, never a URL obtained from browser storage.
      const row = [...discussion.querySelectorAll('#discussion-channel-list [data-channel-scope]')]
          .find(item => item.dataset.channelScope === selected && item.dataset.archived !== 'true');
      const link = row?.querySelector('a.discussion-channel-link');
      if (link && new URL(link.href).origin === location.origin) {
        location.replace(link.href);
        return;
      }
    }
    if (current && /^[a-zA-Z0-9-]{1,40}$/.test(current)) {
      save({...read(), selectedChannel: current});
    }
    const accordions = sections.map(id => document.getElementById(id)).filter(Boolean);
    const previous = read().accordions || {};
    accordions.forEach(section => {
      if (typeof previous[section.id] === 'boolean') { section.open = previous[section.id]; }
    });
    accordions.forEach(section => {
      section.querySelector('summary')?.addEventListener('click', event => {
        // Wait for the native toggle and exclusive group closure. Programmatic search/thread
        // changes never enter this handler and therefore cannot overwrite a user choice.
        setTimeout(() => {
          if (!event.defaultPrevented) {
            save({...read(), accordions: Object.fromEntries(accordions.map(item => [item.id, item.open]))});
          }
        }, 0);
      });
    });
  });
})();
