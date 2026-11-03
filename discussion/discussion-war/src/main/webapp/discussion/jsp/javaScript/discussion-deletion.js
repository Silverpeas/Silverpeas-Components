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
/* Confirm every deletion, with an explicit choice for a linked native contribution. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const dialog = document.getElementById('discussion-delete-event-dialog');
    if (!discussion || !dialog) { return; }
    const approved = new WeakSet();
    const title = document.getElementById('discussion-delete-event-title');
    const both = document.getElementById('discussion-delete-message-and-event');
    const only = document.getElementById('discussion-delete-message-only');
    let active;
    let generation = 0;
    let kind = 'message';
    const settle = form => {
      form.querySelector('button[type=submit]').disabled = false;
      form.dispatchEvent(new CustomEvent('discussion-deletion-settled', {bubbles:true}));
    };
    const submit = (form, deleteEvent) => {
      if (!form.isConnected) { settle(form); return; }
      for (const name of ['deleteEvent', 'deleteIdea', 'deleteVote']) {
        let input = form.elements[name];
        if (!input) {
          input = document.createElement('input');
          input.type = 'hidden';
          input.name = name;
          form.append(input);
        }
        input.value = String(deleteEvent && kind === ({deleteEvent:'event', deleteIdea:'idea', deleteVote:'vote'})[name]);
      }
      // A fast response can still be inside the first native submit event's dispatch.
      window.setTimeout(() => {
        if (!form.isConnected) { settle(form); return; }
        approved.add(form);
        form.requestSubmit();
      }, 0);
    };
    discussion.addEventListener('submit', async event => {
      const form = event.target;
      if (!form.matches('.discussion-delete')) { return; }
      if (approved.has(form)) { approved.delete(form); return; }
      event.preventDefault();
      const button = form.querySelector('button[type=submit]');
      if (button.disabled || dialog.open) { return; }
      button.disabled = true;
      form.querySelector('.discussion-delete-error')?.remove();
      const token = ++generation;
      try {
        const url = form.dataset.deletionInfo || form.action.replace(/\/delete$/, '/deletion');
        const response = await sp.ajaxRequest(url).withParam('version', form.elements.version.value).send();
        if (token !== generation || !form.isConnected) { settle(form); return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const state = page.querySelector('.discussion-event-deletion');
        if (!state || !['true','false'].includes(state.dataset.linked)
            || !['true','false'].includes(state.dataset.canDeleteEvent)) { throw new Error('Invalid deletion response'); }
        kind = state.dataset.kind || (state.dataset.linked === 'true' ? 'event' : 'message');
        if (!['event', 'idea', 'vote', 'message'].includes(kind)) { throw new Error('Invalid deletion kind'); }
        active = form;
        title.textContent = state.dataset.title || '';
        title.hidden = !title.textContent;
        const linked = kind !== 'message';
        both.hidden = !linked || state.dataset.canDeleteEvent !== 'true';
        const question = document.getElementById('discussion-delete-question');
        question.textContent = question.dataset[kind];
        const denied = document.getElementById('discussion-delete-event-denied');
        denied.hidden = !linked || !both.hidden;
        denied.textContent = denied.dataset[kind] || '';
        only.textContent = linked ? only.dataset.linked : only.dataset.message;
        document.getElementById('discussion-delete-event-recurrent').hidden = kind !== 'event' || state.dataset.recurrent !== 'true';
        dialog.showModal();
        document.getElementById('discussion-delete-event-cancel').focus();
      } catch (failure) {
        const error = document.createElement('span');
        error.className = 'discussion-delete-error';
        error.setAttribute('role', 'alert');
        error.textContent = dialog.dataset.error;
        form.append(error);
        settle(form);
      }
    });
    const confirm = deleteEvent => {
      const form = active;
      active = undefined;
      dialog.close();
      if (form) { submit(form, deleteEvent); }
    };
    only.addEventListener('click', () => confirm(false));
    both.addEventListener('click', () => { if (!both.hidden) { confirm(true); } });
    for (const id of ['discussion-delete-event-cancel', 'discussion-delete-event-close']) {
      document.getElementById(id).addEventListener('click', () => dialog.close());
    }
    dialog.addEventListener('close', () => {
      if (active) {
        const form = active;
        active = undefined;
        settle(form);
        if (form.isConnected) { form.querySelector('button[type=submit]').focus(); }
      }
    });
    document.addEventListener('discussion:thread-hidden', () => { generation++; if (dialog.open) { dialog.close(); } });
    window.addEventListener('pagehide', () => { generation++; });
  });
})();
