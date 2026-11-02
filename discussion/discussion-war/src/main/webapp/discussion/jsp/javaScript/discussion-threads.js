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
/* Each document has distinct listeners on the shared native event channel. */
(() => {
  'use strict';
  const embedded = window.frameElement?.classList.contains('discussion-thread-frame') || false;
  const scope = String(Date.now()) + '_' + Math.random().toString(36).slice(2);
  window.DiscussionContext = Object.freeze({embedded, eventId: name => name + '_' + scope});
  if (embedded) { document.documentElement.classList.add('discussion-embedded'); }
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) { return; }
    if (embedded) {
      // Retain native scripts and hidden popup containers; remove only the surrounding chrome.
      for (let node = discussion; node.parentElement; node = node.parentElement) {
        [...node.parentElement.children].filter(sibling => sibling !== node).forEach(sibling => {
          const style = getComputedStyle(sibling);
          if (style.display !== 'none' && style.visibility !== 'hidden'
              && sibling.getBoundingClientRect().height > 0) {
            sibling.classList.add('discussion-embedded-hidden');
          }
        });
        node.parentElement.classList.add('discussion-embedded-wrapper');
        if (node.parentElement === document.body) { break; }
      }
      document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !event.defaultPrevented
            && !document.querySelector('dialog[open], details[open]')
            && !event.target.closest('textarea, input, [contenteditable]')) {
          window.frameElement.dispatchEvent(new CustomEvent('discussion:close-thread', {bubbles:true}));
        }
      });
      return;
    }
    if (discussion.dataset.threadRootId) { return; }
    const sidebar = discussion.querySelector('.discussion-sidebar');
    const accordions = document.getElementById('discussion-sidebar-accordions');
    const panel = document.getElementById('discussion-thread-panel');
    const frame = document.getElementById('discussion-thread-frame');
    const closeButton = document.getElementById('discussion-thread-close');
    const status = document.getElementById('discussion-thread-status');
    const fallback = document.getElementById('discussion-thread-fallback');
    if (!sidebar || !accordions || !panel || !frame) { return; }
    const media = window.matchMedia('(max-width: 760px)');
    const header = discussion.querySelector('.discussion-header');
    const stream = discussion.querySelector('.discussion-stream');
    let current = '';
    let trigger;
    let ready = false;
    const notifyFrame = hidden => {
      try {
        if (hidden) { frame.contentDocument?.dispatchEvent(new Event('discussion:thread-hidden')); }
        frame.contentDocument?.dispatchEvent(new Event('visibilitychange'));
        frame.contentWindow?.dispatchEvent(new Event('resize'));
      } catch (error) { /* A session redirect can temporarily replace the same-origin document. */ }
    };
    const responsive = () => {
      const modal = media.matches && !panel.hidden;
      discussion.classList.toggle('discussion-thread-overlay', modal);
      panel.setAttribute('role', modal ? 'dialog' : 'region');
      if (modal) { panel.setAttribute('aria-modal', 'true'); }
      else { panel.removeAttribute('aria-modal'); }
      if (header) { header.inert = modal; }
      if (stream) { stream.inert = modal; }
      document.dispatchEvent(new Event('visibilitychange'));
      window.dispatchEvent(new Event('resize'));
    };
    const anchor = () => {
      const top = header?.getBoundingClientRect().bottom || 0;
      const row = [...discussion.querySelectorAll('#discussion-history .discussion-message')]
          .find(node => !node.hidden && node.getBoundingClientRect().bottom > top);
      return row ? {row, top:row.getBoundingClientRect().top} : null;
    };
    const restoreAnchor = saved => {
      window.requestAnimationFrame(() => window.requestAnimationFrame(() => {
        if (saved?.row.isConnected) {
          window.scrollBy(0, saved.row.getBoundingClientRect().top - saved.top);
        }
      }));
    };
    const close = () => {
      if (panel.hidden) { return; }
      const saved = anchor();
      panel.hidden = true;
      accordions.hidden = false;
      accordions.inert = false;
      discussion.classList.remove('discussion-thread-open');
      responsive();
      notifyFrame(true);
      trigger?.setAttribute('aria-expanded', 'false');
      const focus = trigger?.isConnected ? trigger : discussion.querySelector('.discussion-composer textarea');
      focus?.focus({preventScroll:true});
      restoreAnchor(saved);
    };
    frame.addEventListener('load', () => {
      if (!current) { return; }
      try {
        const root = frame.contentDocument?.querySelector('#discussion');
        if (root?.dataset.threadRootId !== new URL(current).pathname.split('/').pop()) {
          throw new Error('Thread unavailable');
        }
        ready = true;
        status.textContent = '';
        status.hidden = true;
        fallback.hidden = true;
        frame.classList.remove('is-loading');
        frame.setAttribute('aria-busy', 'false');
        notifyFrame(panel.hidden);
      } catch (error) {
        ready = false;
        status.textContent = status.dataset.error;
        status.hidden = false;
        fallback.hidden = false;
        frame.classList.add('is-loading');
        frame.setAttribute('aria-busy', 'false');
      }
    });
    const open = (url, link) => {
      const saved = anchor();
      trigger?.setAttribute('aria-expanded', 'false');
      trigger = link;
      trigger?.setAttribute('aria-expanded', 'true');
      trigger?.setAttribute('aria-controls', panel.id);
      accordions.hidden = true;
      accordions.inert = true;
      panel.hidden = false;
      discussion.classList.add('discussion-thread-open');
      responsive();
      closeButton.focus({preventScroll:true});
      const sameThread = current && new URL(current).pathname === url.pathname;
      if (!sameThread || !ready || (url.searchParams.has('message') && current !== url.href)) {
        current = url.href;
        ready = false;
        fallback.href = current;
        fallback.hidden = true;
        status.textContent = status.dataset.loading;
        status.hidden = false;
        frame.classList.add('is-loading');
        frame.setAttribute('aria-busy', 'true');
        frame.src = current;
      } else { notifyFrame(false); }
      restoreAnchor(saved);
    };
    discussion.addEventListener('click', event => {
      const link = event.target.closest('a.discussion-thread-link');
      if (!link || event.defaultPrevented || event.button !== 0
          || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) { return; }
      const url = new URL(link.href, window.location.href);
      if (url.origin !== window.location.origin || !/\/threads\/[a-zA-Z0-9-]{1,40}$/.test(url.pathname)) { return; }
      event.preventDefault();
      open(url, link);
    });
    if (discussion.dataset.initialThreadUrl) {
      const url = new URL(discussion.dataset.initialThreadUrl, window.location.href);
      if (url.origin === window.location.origin && /\/threads\/[a-zA-Z0-9-]{1,40}$/.test(url.pathname)) {
        if (url.searchParams.has('message')) {
          url.hash = 'message-' + url.searchParams.get('message');
        }
        // Let the main history first position itself on the original message.
        window.requestAnimationFrame(() => window.requestAnimationFrame(() => open(url)));
      }
    }
    closeButton.addEventListener('click', close);
    panel.addEventListener('discussion:close-thread', close);
    document.addEventListener('keydown', event => {
      if (event.key === 'Escape' && !panel.hidden && !event.defaultPrevented
          && !document.querySelector('dialog[open], details.discussion-edit[open]')) {
        event.preventDefault();
        close();
      }
    });
    media.addEventListener('change', responsive);
  });
})();
