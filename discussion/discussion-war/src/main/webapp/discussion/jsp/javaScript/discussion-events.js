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
/* The event is created by the server together with its native Discussion announcement. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const add = document.getElementById('discussion-add-event');
    const dialog = document.getElementById('discussion-event-dialog');
    const tools = discussion?.querySelector('.discussion-composer .discussion-editor-tools');
    if (!add || !dialog || !tools) { return; }
    tools.append(add);
    add.hidden = false;
    const form = document.getElementById('discussion-event-form');
    const fields = document.getElementById('discussion-event-fields');
    const submit = document.getElementById('discussion-event-submit');
    const error = document.getElementById('discussion-event-error');
    const loading = document.getElementById('discussion-event-loading');
    const retry = document.getElementById('discussion-event-retry');
    const status = document.getElementById('discussion-event-status');
    const cancel = [...dialog.querySelectorAll('.discussion-event-cancel')];
    let generation = 0;
    let creating = false;
    let endEdited = false;
    const periodValid = () => {
      const invalid = form.elements.start.value && form.elements.end.value
          && form.elements.end.value <= form.elements.start.value;
      form.elements.end.setCustomValidity(invalid ? dialog.dataset.periodError : '');
    };
    const loadOptions = async () => {
      const token = ++generation;
      fields.disabled = true;
      submit.disabled = true;
      error.hidden = true;
      retry.hidden = true;
      loading.textContent = dialog.dataset.loading;
      try {
        const response = await sp.ajaxRequest(dialog.dataset.options).send();
        if (token !== generation || !dialog.open) { return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const options = page.querySelector('.discussion-event-options');
        if (!options?.dataset.zone) { throw new Error('Invalid event options'); }
        const selected = form.elements.calendar.value;
        form.elements.calendar.replaceChildren(...[...options.querySelectorAll('option')].map(option => document.importNode(option, true)));
        if ([...form.elements.calendar.options].some(option => option.value === selected)) { form.elements.calendar.value = selected; }
        document.getElementById('discussion-event-zone').textContent = options.dataset.zone;
        document.getElementById('discussion-event-invites').textContent = dialog.dataset.invites.replace('{0}', options.dataset.inviteCount);
        form.elements.start.value ||= options.dataset.start;
        form.elements.end.value ||= options.dataset.end;
        loading.textContent = form.elements.calendar.options.length ? '' : dialog.dataset.unavailable;
        fields.disabled = form.elements.calendar.options.length === 0;
        submit.disabled = fields.disabled;
        periodValid();
        if (!fields.disabled) { form.elements.title.focus(); }
      } catch (failure) {
        if (token === generation && dialog.open) {
          error.textContent = dialog.dataset.error;
          error.hidden = false;
          retry.hidden = false;
          loading.textContent = '';
        }
      }
    };
    add.addEventListener('click', () => { dialog.showModal(); loadOptions(); });
    retry.addEventListener('click', loadOptions);
    cancel.forEach(button => button.addEventListener('click', () => { if (!creating) { dialog.close(); } }));
    dialog.addEventListener('cancel', event => { if (creating) { event.preventDefault(); } });
    dialog.addEventListener('close', () => { generation++; add.focus(); });
    form.elements.start.addEventListener('input', () => {
      if (!endEdited && form.elements.start.value) {
        // Work on wall-clock fields; their actual time zone is validated by the server.
        const start = new Date(form.elements.start.value + 'Z');
        if (Number.isFinite(start.getTime())) { form.elements.end.value = new Date(start.getTime() + (Number(discussion.dataset.eventDurationMinutes) || 60) * 60000).toISOString().slice(0,16); }
      }
      periodValid();
    });
    form.elements.end.addEventListener('input', () => { endEdited = true; periodValid(); });
    form.addEventListener('submit', async event => {
      event.preventDefault();
      if (creating || fields.disabled || !form.reportValidity()) { return; }
      creating = true;
      fields.disabled = true;
      submit.disabled = true;
      cancel.forEach(button => { button.disabled = true; });
      error.hidden = true;
      form.setAttribute('aria-busy', 'true');
      try {
        const response = await sp.ajaxRequest(form.action).byPostMethod()
            .withParam('calendar', form.elements.calendar.value).withParam('title', form.elements.title.value)
            .withParam('start', form.elements.start.value).withParam('end', form.elements.end.value)
            .withParam('description', form.elements.description.value)
            .withParam('threadRootId', discussion.dataset.threadRootId || '').send();
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        if (!page.querySelector('.discussion-event-created')?.dataset.eventId) { throw new Error('Invalid event creation response'); }
        status.textContent = status.dataset.success;
        status.hidden = false;
        form.reset();
        endEdited = false;
        form.elements.end.setCustomValidity('');
        dialog.close();
        document.dispatchEvent(new Event('discussion:event-created'));
        document.dispatchEvent(new Event('discussion:changed'));
      } catch (failure) {
        error.textContent = dialog.dataset.error;
        error.hidden = false;
      } finally {
        creating = false;
        fields.disabled = false;
        submit.disabled = false;
        cancel.forEach(button => { button.disabled = false; });
        form.removeAttribute('aria-busy');
      }
    });
  });
})();
