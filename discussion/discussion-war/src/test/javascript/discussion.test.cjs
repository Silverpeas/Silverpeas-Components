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
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname,
    '../../main/webapp/discussion/jsp/javaScript/discussion.js'), 'utf8');

function browser({ more = true, fail = false } = {}) {
  const pages = [{ dataset: { before: '31', more: String(more) } }];
  const listeners = {};
  const older = { hidden: false, disabled: false,
    addEventListener: (name, handler) => { listeners[name] = handler; } };
  const error = { hidden: true };
  const history = { querySelector: () => pages.at(-1), appendChild: page => pages.push(page) };
  const nodes = { discussion: { dataset: { history: '/Rdiscussion/discussion1/history' } },
    'discussion-history': history, 'discussion-older': older, 'discussion-error': error };
  const requests = [];
  let stamped = 0;
  let intersect;
  const context = {
    document: { addEventListener: (name, handler) => { listeners[name] = handler; },
      getElementById: id => nodes[id], importNode: node => node },
    DOMParser: class {
      parseFromString() {
        return { querySelector: () => ({ dataset: { before: '1', more: 'false' } }) };
      }
    },
    IntersectionObserver: class {
      constructor(callback) { intersect = callback; }
      observe() { /* The test controls viewport changes explicitly. */ }
    },
    applyTokenSecurity: () => { stamped++; },
    sp: { ajaxRequest: url => {
      const request = { url };
      requests.push(request);
      return { withParam: (name, value) => {
        request[name] = value;
        return { send: async () => {
          if (fail) { throw new Error('network unavailable'); }
          return { responseText: 'server-rendered fragment' };
        } };
      } };
    } }
  };
  vm.runInNewContext(source, context);
  listeners.DOMContentLoaded();
  return { pages, requests, older, error, click: () => listeners.click(),
    intersect: () => intersect([{ isIntersecting: true }]), stamps: () => stamped };
}

test('entering the viewport loads one older page using the exclusive cursor and stamps its forms', async () => {
  const view = browser();
  view.intersect();
  view.intersect();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(view.requests.length, 1);
  assert.equal(view.requests[0].before, '31');
  assert.equal(view.pages.length, 2);
  assert.equal(view.stamps(), 1);
  assert.equal(view.older.hidden, true);
});

test('the end of history never requests another page', () => {
  const view = browser({ more: false });
  view.intersect();
  assert.equal(view.requests.length, 0);
});

test('a failed request keeps the cursor and allows an explicit retry without an automatic loop', async () => {
  const view = browser({ fail: true });
  view.intersect();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(view.error.hidden, false);
  assert.equal(view.older.disabled, false);
  assert.equal(view.pages.length, 1);
  view.intersect();
  assert.equal(view.requests.length, 1);
  await view.click();
  assert.equal(view.requests.length, 2);
  assert.equal(view.requests[1].before, '31');
});
