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
/* The channel selection is part of each URL, never shared session state. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const nav = document.getElementById('discussion-channels');
    const toggle = document.getElementById('discussion-channels-toggle');
    if (!discussion || !nav || !toggle || window.DiscussionContext?.embedded) { return; }
    const scope = discussion.dataset.channelScope || 'main';
    const list = document.getElementById('discussion-channel-list');
    const error = document.getElementById('discussion-channels-error');
    const media = window.matchMedia('(max-width: 760px)');
    const close = document.getElementById('discussion-channels-close');
    const showLabel = toggle.getAttribute('aria-label');
    const hideLabel = close.getAttribute('aria-label') || close.textContent;
    const setOpen = open => {
      nav.hidden = !open;
      discussion.classList.toggle('discussion-channels-open', open);
      toggle.setAttribute('aria-expanded', String(open));
      toggle.setAttribute('aria-label', open ? hideLabel : showLabel);
      toggle.title = open ? hideLabel : showLabel;
      discussion.querySelector('.discussion-workspace').inert = open && media.matches;
      window.dispatchEvent(new Event('resize'));
    };
    toggle.addEventListener('click', () => setOpen(nav.hidden));
    close.addEventListener('click', () => { setOpen(false); toggle.focus(); });
    document.addEventListener('keydown', event => {
      if (event.key === 'Escape' && !nav.hidden && !document.querySelector('dialog[open]')) {
        setOpen(false); toggle.focus();
      }
    });
    media.addEventListener('change', () => setOpen(!nav.hidden));
    setOpen(true);
    if (media.matches) { close.focus(); }
    const markCurrent = () => {
      let unread = 0;
      list.querySelectorAll('[data-channel-scope]').forEach(row => {
        const current = row.dataset.channelScope === scope;
        const link = row.querySelector('a');
        if (current) { link.setAttribute('aria-current', 'page'); }
        const settings = row.querySelector('.discussion-channel-settings');
        if (settings) { settings.hidden = !current; }
        row.classList.toggle('discussion-channel-unread', Number(row.dataset.unread) > 0);
        if (!current && row.dataset.archived !== 'true') { unread += Number(row.dataset.unread) || 0; }
      });
      const count = document.getElementById('discussion-channels-unread');
      count.hidden = unread === 0;
      count.textContent = unread > 99 ? '99+' : String(unread);
    };
    markCurrent();
    let refreshTimer;
    let source;
    let alive = true;
    const refresh = async () => {
      try {
        const response = await sp.ajaxRequest(discussion.dataset.channels).send();
        if (!alive) { return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const updated = page.querySelector('.discussion-channel-groups');
        if (!updated) { throw new Error('Invalid channel response'); }
        const focusedScope = list.contains(document.activeElement)
            ? document.activeElement.closest('[data-channel-scope]')?.dataset.channelScope : null;
        const archivesOpen = list.querySelector('#discussion-channel-archives')?.open === true;
        list.replaceChildren(document.importNode(updated, true));
        const archives = list.querySelector('#discussion-channel-archives');
        if (archives) { archives.open = archivesOpen; }
        error.hidden = true;
        markCurrent();
        if (focusedScope) {
          [...list.querySelectorAll('[data-channel-scope]')]
              .find(row => row.dataset.channelScope === focusedScope)?.querySelector('a').focus();
        }
      } catch (failure) { if (alive) { error.hidden = false; } }
    };
    const changed = event => {
      try {
        if (event && JSON.parse(event.data).instanceId !== discussion.dataset.instanceId) { return; }
      } catch (failure) { return; }
      window.clearTimeout(refreshTimer);
      refreshTimer = window.setTimeout(refresh, 150);
    };
    const connect = () => {
      source = window.top.spServerEventSource || window.spServerEventSource;
      if (source?.isEnabled()) {
        source.addEventListener('DISCUSSION_CHANGED', changed, window.DiscussionContext.eventId('discussionChannels'));
        source.addEventListener('OPEN_EVENT_SOURCE', refresh, window.DiscussionContext.eventId('discussionChannelsOpen'));
      }
    };
    if (typeof whenSilverpeasReady === 'function') { whenSilverpeasReady(connect); } else { connect(); }
    const dialog = document.getElementById('discussion-channel-dialog');
    if (dialog) {
      const form = dialog.querySelector('form');
      const archive = form.elements.archive;
      const openDialog = create => {
        form.action = create ? form.dataset.create : form.dataset.update;
        form.elements.name.value = create ? '' : form.elements.name.dataset.current || document.getElementById('discussion-channel-title').textContent;
        form.elements.description.value = create ? '' : form.elements.description.dataset.current;
        archive.closest('label').hidden = create;
        archive.disabled = create || archive.dataset.main === 'true';
        archive.checked = !create && archive.dataset.current === 'true';
        form.elements.archived.value = String(archive.checked);
        dialog.showModal();
      };
      document.getElementById('discussion-channel-create').addEventListener('click', () => openDialog(true));
      list.addEventListener('click', event => {
        if (event.target.closest('.discussion-channel-settings')) { openDialog(false); }
      });
      form.querySelector('.discussion-channel-cancel').addEventListener('click', () => dialog.close());
      archive.addEventListener('change', () => { form.elements.archived.value = String(archive.checked); });
    }
    const preference = document.getElementById('discussion-channel-notification-mode');
    if (preference) { preference.value = preference.dataset.current || 'ALL'; }
    // Retain File objects in the shell, together with text and reading position, per user/channel.
    // The native navigation reloads this iframe while the shell and its map remain alive.
    const shell = window.top;
    shell.DiscussionChannelDrafts ||= new Map();
    const drafts = shell.DiscussionChannelDrafts;
    const key = discussion.dataset.userId + ':' + discussion.dataset.instanceId + ':' + scope;
    const textarea = document.getElementById('discussion-body');
    const composer = textarea?.form;
    const save = () => {
      const headerBottom = discussion.querySelector('.discussion-header').getBoundingClientRect().bottom;
      const anchor = [...discussion.querySelectorAll('#discussion-history .discussion-message')]
          .find(row => !row.hidden && row.getBoundingClientRect().bottom > headerBottom);
      const uploads = ['images', 'files'].map(name => ({name, files:[...(composer?.elements[name]?.files || [])]}));
      drafts.delete(key);
      drafts.set(key, {body:textarea?.value || '', uploads, anchorId:anchor?.id, anchorTop:anchor?.getBoundingClientRect().top - headerBottom, scroll:window.DiscussionScroll?.scrollTop || 0});
      if (drafts.size > 30) { drafts.delete(drafts.keys().next().value); }
    };
    const saved = drafts.get(key);
    if (saved && textarea) {
      textarea.value = saved.body;
      window.requestAnimationFrame(() => {
        textarea.dispatchEvent(new Event('input', {bubbles:true}));
        saved.uploads.forEach(upload => {
          const input = composer.elements[upload.name];
          if (input && upload.files.length) {
            const transfer = new DataTransfer();
            upload.files.forEach(file => transfer.items.add(file));
            input.files = transfer.files;
            input.dispatchEvent(new Event('change', {bubbles:true}));
          }
        });
        window.requestAnimationFrame(() => {
          const restoringAnchor = !discussion.dataset.messageId || saved.anchorId === 'message-' + discussion.dataset.messageId;
          const anchor = restoringAnchor && saved.anchorId && document.getElementById(saved.anchorId);
          if (window.DiscussionScroll && anchor) {
            const top = discussion.querySelector('.discussion-header').getBoundingClientRect().bottom;
            window.DiscussionScroll.scrollTop += anchor.getBoundingClientRect().top - top - saved.anchorTop;
          } else if (window.DiscussionScroll && !discussion.dataset.messageId) {
            window.DiscussionScroll.scrollTop = saved.scroll;
          }
        });
      });
    }
    // Native navigation may follow a successful POST with a fresh page.
    composer?.addEventListener('submit', () => drafts.delete(key));
    document.addEventListener('click', event => {
      const link = event.target.closest('.discussion-channel-link');
      if (link && !event.ctrlKey && !event.metaKey && !event.shiftKey && !event.altKey && event.button === 0) {
        save();
        const targetScope = link.closest('[data-channel-scope]').dataset.channelScope;
        const targetDraft = drafts.get(discussion.dataset.userId + ':' + discussion.dataset.instanceId + ':' + targetScope);
        if (targetDraft?.anchorId?.startsWith('message-')) {
          const base = new URL(discussion.dataset.channels, window.location.href);
          base.pathname = base.pathname.replace(/\/channels$/, '/messages/' + targetDraft.anchorId.slice(8));
          base.hash = targetDraft.anchorId;
          link.href = base.href;
        }
        if (media.matches) { setOpen(false); }
      }
    });
    window.addEventListener('pagehide', () => {
      alive = false;
      window.clearTimeout(refreshTimer);
      source?.removeEventListener('DISCUSSION_CHANGED', changed);
      source?.removeEventListener('OPEN_EVENT_SOURCE', refresh);
    });
    window.addEventListener('pageshow', event => { if (event.persisted) { alive = true; connect(); refresh(); } });
  });
})();
