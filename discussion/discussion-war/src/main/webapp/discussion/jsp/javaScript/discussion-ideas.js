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
/* Native suggestion proposal; the message draft remains untouched. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const add = document.getElementById('discussion-add-idea');
    const dialog = document.getElementById('discussion-idea-dialog');
    const tools = discussion?.querySelector('.discussion-composer .discussion-editor-tools');
    if (!add || !dialog || !tools) { return; }
    tools.append(add);
    add.hidden = false;
    const form = document.getElementById('discussion-idea-form');
    const fields = document.getElementById('discussion-idea-fields');
    const submit = document.getElementById('discussion-idea-submit');
    const error = document.getElementById('discussion-idea-error');
    const loading = document.getElementById('discussion-idea-loading');
    const retry = document.getElementById('discussion-idea-retry');
    const status = document.getElementById('discussion-idea-status');
    const cancel = [...dialog.querySelectorAll('.discussion-idea-cancel')];
    let generation = 0;
    let creating = false;
    let sourceId = '';
    let opener = add;
    let initialized = false;
    const load = async () => {
      const token = ++generation;
      fields.disabled = true;
      submit.disabled = true;
      error.hidden = true;
      retry.hidden = true;
      loading.textContent = dialog.dataset.loading;
      try {
        const response = await sp.ajaxRequest(dialog.dataset.options).withParam('sourceId', sourceId).send();
        if (token !== generation || !dialog.open) { return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const options = page.querySelector('.discussion-idea-options');
        if (!options) { throw new Error('Invalid idea options'); }
        const selected = form.elements.box.value;
        form.elements.box.replaceChildren(...[...options.querySelectorAll('option')].map(option => document.importNode(option, true)));
        if ([...form.elements.box.options].some(option => option.value === selected)) { form.elements.box.value = selected; }
        if (!initialized) {
          form.elements.description.value = options.querySelector('.discussion-idea-source')?.value || '';
          initialized = true;
        }
        fields.disabled = form.elements.box.options.length === 0;
        submit.disabled = fields.disabled;
        loading.textContent = fields.disabled ? dialog.dataset.unavailable : '';
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
    discussion.addEventListener('click', event => {
      const button = event.target.closest('#discussion-add-idea, .discussion-propose-idea');
      if (!button || dialog.open || creating) { return; }
      const nextSource = button.dataset.sourceId || '';
      if (nextSource !== sourceId) { form.reset(); initialized = false; }
      sourceId = nextSource;
      opener = button;
      dialog.showModal();
      load();
    });
    retry.addEventListener('click', load);
    cancel.forEach(button => button.addEventListener('click', () => { if (!creating) { dialog.close(); } }));
    dialog.addEventListener('cancel', event => { if (creating) { event.preventDefault(); } });
    dialog.addEventListener('close', () => { generation++; if (opener.isConnected) { opener.focus(); } });
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
            .withParam('box', form.elements.box.value).withParam('title', form.elements.title.value)
            .withParam('description', form.elements.description.value).withParam('sourceId', sourceId)
            .withParam('threadRootId', discussion.dataset.threadRootId || '').send();
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const created = page.querySelector('.discussion-idea-created');
        if (!created?.dataset.ideaId) { throw new Error('Invalid idea response'); }
        status.textContent = created.dataset.pending === 'true' ? status.dataset.pending : status.dataset.success;
        status.hidden = false;
        form.reset();
        initialized = false;
        dialog.close();
        document.dispatchEvent(new Event('discussion:idea-created'));
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
