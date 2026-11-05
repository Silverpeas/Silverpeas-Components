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
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const add = document.getElementById('discussion-add-visio');
    const dialog = document.getElementById('discussion-visio-dialog');
    const tools = discussion?.querySelector('.discussion-composer .discussion-editor-tools');
    if (!add || !dialog || !tools) { return; }
    tools.append(add);
    const form = document.getElementById('discussion-visio-form');
    const fields = document.getElementById('discussion-visio-fields');
    const submit = document.getElementById('discussion-visio-submit');
    const error = document.getElementById('discussion-visio-error');
    const count = document.getElementById('discussion-visio-count');
    const status = document.getElementById('discussion-visio-status');
    const join = document.getElementById('discussion-visio-join');
    const cancel = [...dialog.querySelectorAll('.discussion-visio-cancel')];
    let creating = false;
    let generation = 0;
    const load = async () => {
      const token = ++generation;
      fields.disabled = true;
      submit.disabled = true;
      error.hidden = true;
      try {
        const response = await sp.ajaxRequest(dialog.dataset.options).send();
        if (token !== generation) { return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const options = page.querySelector('.discussion-visio-options');
        if (!options) { throw new Error('Invalid meeting options'); }
        const available = options.dataset.available === 'true';
        add.hidden = !available;
        fields.disabled = !available;
        submit.disabled = !available;
        count.textContent = options.dataset.invitees;
        if (dialog.open && !available) {
          error.textContent = dialog.dataset.unavailable;
          error.hidden = false;
        }
        if (dialog.open && available) { form.elements.title.focus(); }
      } catch (failure) {
        if (token === generation) {
          add.hidden = !dialog.open;
          error.textContent = dialog.dataset.error;
          error.hidden = !dialog.open;
        }
      }
    };
    add.addEventListener('click', () => { dialog.showModal(); load(); });
    cancel.forEach(button => button.addEventListener('click', () => { if (!creating) { dialog.close(); } }));
    dialog.addEventListener('cancel', event => { if (creating) { event.preventDefault(); } });
    dialog.addEventListener('close', () => { generation++; add.focus(); });
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
            .withParam('title', form.elements.title.value)
            .withParam('threadRootId', discussion.dataset.threadRootId || '').send();
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const created = page.querySelector('.discussion-visio-created');
        if (!created?.dataset.id) { throw new Error('Invalid meeting response'); }
        const url = new URL(created.dataset.url, location.href);
        if (!['https:', 'http:'].includes(url.protocol)) { throw new Error('Invalid meeting URL'); }
        join.href = url.href;
        status.hidden = false;
        form.reset();
        dialog.close();
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
    load();
  });
})();
