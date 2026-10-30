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
/* Synchronize only loaded messages, plus a bounded batch of new messages. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion?.dataset.updates) {
      return;
    }
    const history = document.getElementById('discussion-history');
    const scroller = window.DiscussionScroll || document.getElementById('discussion-scroll');
    const failure = document.getElementById('discussion-live-error');
    const articles = () => Array.from(history.querySelectorAll('.discussion-message'));
    const latest = () => Math.max(0, ...articles().map(row => Number(row.dataset.sequence)));
    // Opening an older message must not follow new messages during the initial catch-up.
    let followLatest = !discussion.dataset.messageId && !window.location.hash.startsWith('#message-');
    const enableFollowing = () => { followLatest = true; };
    scroller.addEventListener('wheel', enableFollowing, {passive: true});
    scroller.addEventListener('touchmove', enableFollowing, {passive: true});
    document.addEventListener('keydown', event => {
      if (['PageDown', 'PageUp', 'Home', 'End', 'ArrowUp', 'ArrowDown'].includes(event.key)
          && !event.target.closest('textarea, input, [contenteditable]')) {
        enableFollowing();
      }
    });
    const fingerprints = new Map();
    let loading = false;
    let stopped = false;
    let dirty = false;
    let retryTimer;
    let retryDelay = 1000;
    let source;
    const deferred = new Set();
    let selectionPending = false;

    const merge = page => {
      // Anchor the current row rather than the total height: edits can resize older messages.
      const bottom = followLatest && scroller.scrollHeight - scroller.scrollTop - scroller.clientHeight < 48;
      const anchor = articles().find(row => row.getBoundingClientRect().bottom > scroller.getBoundingClientRect().top);
      const anchorTop = anchor?.getBoundingClientRect().top;
      page.querySelectorAll('.discussion-message').forEach(row => {
        const existing = document.getElementById(row.id);
        const fingerprint = row.outerHTML;
        if (existing && fingerprints.get(row.id) === fingerprint) {
          return;
        }
        const selected = existing?.contains(window.getSelection()?.anchorNode);
        // Reconcile deferred rows when the interaction ends, without periodic polling.
        if (existing?.querySelector('details[open], .discussion-like-button:disabled') ||
            existing?.contains(document.activeElement) || selected) {
          deferred.add(row.id);
          selectionPending ||= selected;
          return;
        }
        deferred.delete(row.id);
        fingerprints.set(row.id, fingerprint);
        const fresh = document.importNode(row, true);
        if (existing && history.contains(existing)) {
          existing.replaceWith(fresh);
        } else {
          history.querySelector('.discussion-page:last-child').append(fresh);
        }
      });
      if (articles().length) {
        history.querySelector('.discussion-empty')?.remove();
      }
      const divider = page.querySelector('.discussion-unread-divider') || history.querySelector('.discussion-unread-divider');
      history.querySelectorAll('.discussion-unread-divider').forEach(node => node.remove());
      const firstUnread = history.querySelector('.discussion-message--unread');
      if (firstUnread && divider) {
        firstUnread.before(document.importNode(divider, true));
      }
      const count = discussion.querySelector('.discussion-unread-count');
      count.textContent = page.dataset.unreadCount;
      const read = discussion.querySelector('.discussion-read-form');
      read.hidden = Number(page.dataset.unreadCount) === 0;
      read.querySelector('[name="sequence"]').value = latest();
      if (typeof applyTokenSecurity === 'function') {
        applyTokenSecurity(history);
      }
      window.DiscussionEditor?.enhance(history);
      if (bottom) {
        scroller.scrollTop = scroller.scrollHeight;
      } else if (anchor) {
        const current = document.getElementById(anchor.id);
        scroller.scrollTop += current.getBoundingClientRect().top - anchorTop;
      }
    };

    const synchronize = async () => {
      const ids = articles().map(row => row.id.slice('message-'.length));
      for (let offset = 0; offset < Math.max(1, ids.length); offset += 100) {
        const response = await sp.ajaxRequest(discussion.dataset.updates)
            .withParam('after', latest()).withParam('ids', ids.slice(offset, offset + 100).join(',')).send();
        if (stopped) {
          return;
        }
        const fragment = new DOMParser().parseFromString(response.responseText, 'text/html');
        const page = fragment.querySelector('.discussion-page');
        if (!page || !page.hasAttribute('data-unread-count')) {
          throw new Error('Invalid discussion update');
        }
        merge(page);
        // A reconnect can require several bounded batches to catch up completely.
        dirty ||= Number(page.dataset.lastSequence) > latest();
      }
    };
    const refresh = async () => {
      dirty = true;
      if (document.hidden || loading || stopped) {
        return;
      }
      window.clearTimeout(retryTimer);
      loading = true;
      try {
        do {
          dirty = false;
          await synchronize();
        } while (dirty && !document.hidden && !stopped);
        failure.hidden = Boolean(source?.isEnabled());
        retryDelay = 1000;
      } catch (error) {
        dirty = true;
        failure.hidden = false;
        // Retry a failed synchronization only, never poll an unchanged conversation.
        retryTimer = window.setTimeout(refresh, retryDelay);
        retryDelay = Math.min(retryDelay * 2, 30000);
      } finally {
        loading = false;
      }
    };
    const changed = event => {
      try {
        if (JSON.parse(event.data).instanceId === discussion.dataset.instanceId) {
          document.dispatchEvent(new Event('discussion:changed'));
          refresh();
        }
      } catch (error) {
        // An unrelated or malformed event must not interrupt the shared event source.
      }
    };
    const disconnected = () => { failure.hidden = false; };
    const connect = () => {
      if (stopped || source) {
        return;
      }
      source = window.top.spServerEventSource || window.spServerEventSource;
      if (!source?.isEnabled()) {
        failure.hidden = false;
        return;
      }
      source.addEventListener('DISCUSSION_CHANGED', changed);
      source.addEventListener('OPEN_EVENT_SOURCE', refresh);
      source.addEventListener('error', disconnected);
      // Close the gap between rendering the JSP and subscribing to the event source.
      refresh();
    };
    if (typeof whenSilverpeasReady === 'function') {
      whenSilverpeasReady(connect);
    } else {
      connect();
    }
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) {
        refresh();
      }
    });
    window.addEventListener('focus', refresh);
    history.addEventListener('toggle', event => {
      if (!event.target.open && deferred.size) {
        refresh();
      }
    }, true);
    history.addEventListener('focusout', () => {
      if (deferred.size) {
        queueMicrotask(refresh);
      }
    });
    document.addEventListener('selectionchange', () => {
      if (selectionPending && window.getSelection()?.isCollapsed) {
        selectionPending = false;
        refresh();
      }
    });
    discussion.addEventListener('discussion-reaction-settled', event => {
      fingerprints.delete(event.target.closest('.discussion-message')?.id);
      refresh();
    });
    window.addEventListener('pagehide', () => {
      stopped = true;
      window.clearTimeout(retryTimer);
      if (source?.isEnabled()) {
        source.removeEventListener('DISCUSSION_CHANGED', changed);
        source.removeEventListener('OPEN_EVENT_SOURCE', refresh);
        source.removeEventListener('error', disconnected);
      }
      source = undefined;
    });
    window.addEventListener('pageshow', () => {
      if (stopped) {
        stopped = false;
        connect();
      }
    });
  });
})();
