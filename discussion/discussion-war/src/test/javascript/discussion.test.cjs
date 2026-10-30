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

function browser({ more = true, fail = false, presence = false, unread = false } = {}) {
  const pages = [{ dataset: { before: '31', more: String(more) } }];
  const listeners = {};
  const older = { hidden: false, disabled: false,
    addEventListener: (name, handler) => { listeners[name] = handler; } };
  const error = { hidden: true };
  const scroll = { scrollTop: 0, scrollHeight: 1000,
    addEventListener: (name, handler) => { listeners[name] = handler; },
    getBoundingClientRect: () => ({top: 0}) };
  const unreadDivider = {getBoundingClientRect: () => ({top: 170})};
  const history = {
    querySelector: selector => selector === '.discussion-page:first-child' ? pages[0]
        : (unread && selector === '.discussion-unread-divider' ? unreadDivider : null),
    querySelectorAll: () => [], contains: () => false,
    prepend: page => { pages.unshift(page); scroll.scrollHeight += 200; }
  };
  const nodes = { discussion: { dataset: { history: '/Rdiscussion/discussion1/history' } },
    'discussion-history': history, 'discussion-scroll': scroll, 'discussion-older': older, 'discussion-error': error };
  const presenceError = { hidden: true };
  const classes = new Set();
  let refreshed = 0;
  const participants = { replaceChildren: () => { refreshed++; },
    classList: { add: value => classes.add(value), remove: value => classes.delete(value) } };
  if (presence) {
    nodes['discussion-participants'] = participants;
    nodes['discussion-presence-error'] = presenceError;
    nodes.discussion.dataset.participants = '/Rdiscussion/discussion1/participants';
  }
  const requests = [];
  let timer;
  let cleared = false;
  const windowEvents = {};

  let stamped = 0;
  let intersect;
  const context = {
    window: {location: {hash: ''}, requestAnimationFrame: handler => handler(), setInterval: (handler, delay) => {
      assert.equal(delay, 30000); timer = handler; return 1;
    }, clearInterval: () => { cleared = true; },
    addEventListener: (name, handler) => { (windowEvents[name] ||= []).push(handler); } },
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
      disconnect() { /* The test has no real observer to disconnect. */ }
    },
    applyTokenSecurity: () => { stamped++; },
    sp: { ajaxRequest: url => {
      const request = { url };
      requests.push(request);
      return { send: async () => {
        if (fail) { throw new Error('network unavailable'); }
        return { responseText: 'server-rendered participants' };
      }, withParam: (name, value) => {
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
    intersect: () => intersect([{ isIntersecting: true }]), stamps: () => stamped,
    refreshPresence: () => timer(), refreshed: () => refreshed, presenceError, classes,
    setHidden: value => { context.document.hidden = value; },
    setFailure: value => { fail = value; },
    leave: () => windowEvents.pagehide.forEach(handler => handler()), cleared: () => cleared,
    scroll, scrollUp: () => { scroll.scrollTop = 0; listeners.scroll(); } };
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


test('presence refresh only replaces the author list and leaves history and editing intact', async () => {
  const view = browser({ presence: true });
  const first = view.refreshPresence();
  await view.refreshPresence();
  await first;
  assert.equal(view.requests.length, 1);
  assert.equal(view.requests[0].url, '/Rdiscussion/discussion1/participants');
  assert.equal(view.refreshed(), 1);
  assert.equal(view.pages.length, 1);
  assert.equal(view.presenceError.hidden, true);
});

test('presence polling pauses for hidden pages and stops when leaving the page', async () => {
  const view = browser({ presence: true });
  view.setHidden(true);
  await view.refreshPresence();
  assert.equal(view.requests.length, 0);
  view.setHidden(false);
  await view.refreshPresence();
  assert.equal(view.requests.length, 1);
  view.leave();
  assert.equal(view.cleared(), true);
});

test('a failed presence refresh hides stale indicators until a successful retry', async () => {
  const view = browser({ presence: true, fail: true });
  await view.refreshPresence();
  assert.equal(view.presenceError.hidden, false);
  assert.equal(view.classes.has('is-stale'), true);
  assert.equal(view.refreshed(), 0);
  view.setFailure(false);
  await view.refreshPresence();
  assert.equal(view.presenceError.hidden, true);
  assert.equal(view.classes.has('is-stale'), false);
  assert.equal(view.refreshed(), 1);
});


test('new conversations open at the bottom and unread messages take priority when visible', () => {
  const recent = browser();
  assert.equal(recent.scroll.scrollTop, recent.scroll.scrollHeight);
  const unseen = browser({unread: true});
  assert.equal(unseen.scroll.scrollTop, 162);
});

test('scrolling up prepends older history and compensates its height without jumping', async () => {
  const view = browser();
  view.scrollUp();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(view.requests.length, 1);
  assert.equal(view.requests[0].before, '31');
  assert.equal(view.pages[0].dataset.before, '1');
  assert.equal(view.scroll.scrollTop, 200);
  assert.equal(view.older.hidden, true);
});
