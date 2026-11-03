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
/* Search the entire native index, independently of currently loaded history. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('discussion-search');
    if (!form) { return; }
    const input = document.getElementById('discussion-search-query');
    const section = document.getElementById('discussion-search-section');
    const results = document.getElementById('discussion-search-results');
    const status = document.getElementById('discussion-search-status');
    const count = document.getElementById('discussion-search-count');
    const more = document.getElementById('discussion-search-more');
    const close = document.getElementById('discussion-search-close');
    let generation = 0;
    let query = '';
    let next = 0;
    let busy = false;
    const reset = () => {
      generation++;
      query = '';
      busy = false;
      more.disabled = false;
      section.hidden = true;
      section.open = false;
      results.replaceChildren();
      results.removeAttribute('aria-busy');
      more.hidden = true;
      count.textContent = '';
      status.textContent = '';
      input.value = '';
    };
    const load = async append => {
      if (append && busy) { return; }
      const text = append ? query : input.value.trim();
      if (!text) { reset(); return; }
      const token = ++generation;
      query = text;
      busy = true;
      if (!append) {
        next = 0;
        results.replaceChildren();
        count.textContent = '';
      }
      section.hidden = false;
      section.open = true;
      results.setAttribute('aria-busy', 'true');
      status.textContent = status.dataset.loading;
      more.disabled = true;
      more.hidden = !append;
      try {
        const response = await sp.ajaxRequest(form.action).withParam('q', text).withParam('offset', String(next)).withParam('allChannels', String(document.getElementById('discussion-search-all-channels')?.checked === true)).send();
        if (token !== generation) { return; }
        const page = new DOMParser().parseFromString(response.responseText, 'text/html');
        const fragment = page.querySelector('.discussion-search-page');
        if (!fragment) { throw new Error('Invalid search response'); }
        next = Number(fragment.dataset.nextOffset);
        count.textContent = fragment.dataset.total;
        more.hidden = fragment.dataset.hasMore !== 'true';
        // All message text was escaped by JSP. Import only the search result fragment.
        results.append(document.importNode(fragment, true));
        status.textContent = '';
      } catch (failure) {
        if (token === generation) { status.textContent = status.dataset.error; }
      } finally {
        if (token === generation) {
          busy = false;
          more.disabled = false;
          results.removeAttribute('aria-busy');
        }
      }
    };
    form.addEventListener('submit', event => { event.preventDefault(); load(false); });
    input.addEventListener('input', () => { if (!input.value.trim()) { reset(); } });
    document.getElementById('discussion-search-scope')?.addEventListener('change', () => {
      if (input.value.trim()) { load(false); }
    });
    more.addEventListener('click', () => load(true));
    close.addEventListener('click', () => { reset(); input.focus(); });
    window.addEventListener('pagehide', reset);
  });
})();
