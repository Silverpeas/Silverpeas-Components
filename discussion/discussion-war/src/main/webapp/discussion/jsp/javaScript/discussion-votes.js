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
/* Native single-question vote creation; the message draft remains untouched. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const add = document.getElementById('discussion-add-vote');
    const dialog = document.getElementById('discussion-vote-dialog');
    const tools = discussion?.querySelector('.discussion-composer .discussion-editor-tools');
    if (!add || !dialog || !tools) { return; }
    tools.append(add);
    add.hidden = false;
    const form = document.getElementById('discussion-vote-form');
    const fields = document.getElementById('discussion-vote-fields');
    const submit = document.getElementById('discussion-vote-submit');
    const error = document.getElementById('discussion-vote-error');
    const loading = document.getElementById('discussion-vote-loading');
    const retry = document.getElementById('discussion-vote-retry');
    const status = document.getElementById('discussion-vote-status');
    const cancel = [...dialog.querySelectorAll('.discussion-vote-cancel')];
    let generation = 0;
    let creating = false;
    const opener = add;
    const load = async () => {
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
        const options = page.querySelector('.discussion-vote-options');
        if (!options) { throw new Error('Invalid vote options'); }
        const selected = form.elements.vote.value;
        form.elements.vote.replaceChildren(...[...options.querySelectorAll('option')].map(option => document.importNode(option, true)));
        if ([...form.elements.vote.options].some(option => option.value === selected)) { form.elements.vote.value = selected; }
        fields.disabled = form.elements.vote.options.length === 0;
        submit.disabled = fields.disabled;
        loading.textContent = fields.disabled ? dialog.dataset.unavailable : '';
        applyAnonymous();
        if (!fields.disabled) { form.elements.question.focus(); }
      } catch (failure) {
        if (token === generation && dialog.open) {
          error.textContent = dialog.dataset.error;
          error.hidden = false;
          retry.hidden = false;
          loading.textContent = '';
        }
      }
    };
    const applyAnonymous = () => {
      const forced = form.elements.vote.selectedOptions[0]?.dataset.anonymous === 'true';
      if (forced) { form.elements.anonymous.checked = true; }
      form.elements.anonymous.disabled = forced;
    };
    form.elements.vote.addEventListener('change', applyAnonymous);
    const validateAnswers = () => {
      const answers = form.elements.answers.value.split('\n').map(answer => answer.trim());
      const valid = answers.length >= 2 && answers.length <= 10 && answers.every(answer => answer && answer.length <= 200)
          && new Set(answers.map(answer => answer.toLocaleLowerCase())).size === answers.length;
      form.elements.answers.setCustomValidity(valid ? '' : dialog.dataset.answerError);
    };
    form.elements.answers.addEventListener('input', validateAnswers);
    add.addEventListener('click', () => { dialog.showModal(); load(); });
    retry.addEventListener('click', load);
    cancel.forEach(button => button.addEventListener('click', () => { if (!creating) { dialog.close(); } }));
    dialog.addEventListener('cancel', event => { if (creating) { event.preventDefault(); } });
    dialog.addEventListener('close', () => { generation++; if (opener.isConnected) { opener.focus(); } });
    form.addEventListener('submit', async event => {
      event.preventDefault();
      validateAnswers();
      if (creating || fields.disabled || !form.reportValidity()) { return; }
      creating = true;
      fields.disabled = true;
      submit.disabled = true;
      cancel.forEach(button => { button.disabled = true; });
      error.hidden = true;
      form.setAttribute('aria-busy', 'true');
      try {
        const response = await sp.ajaxRequest(form.action).byPostMethod()
            .withParam('vote', form.elements.vote.value).withParam('question', form.elements.question.value)
            .withParam('answers', form.elements.answers.value.split('\n').map(answer => answer.trim()).join('\n'))
            .withParam('multiple', String(form.elements.multiple.checked)).withParam('anonymous', String(form.elements.anonymous.checked))
            .withParam('threadRootId', discussion.dataset.threadRootId || '').send();
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const created = page.querySelector('.discussion-vote-created');
        if (!created?.dataset.voteId) { throw new Error('Invalid vote response'); }
        status.textContent = status.dataset.success;
        status.hidden = false;
        form.reset();
        dialog.close();
        document.dispatchEvent(new Event('discussion:vote-created'));
        document.dispatchEvent(new Event('discussion:changed'));
      } catch (failure) {
        error.textContent = dialog.dataset.error;
        error.hidden = false;
      } finally {
        creating = false;
        fields.disabled = false;
        submit.disabled = false;
        applyAnonymous();
        cancel.forEach(button => { button.disabled = false; });
        form.removeAttribute('aria-busy');
      }
    });
  });
})();
