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
const path = require('node:path');
const {chromium} = require('playwright');
const script = fs.readFileSync(path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript/discussion-view-state.js'), 'utf8');
const ids = ['discussion-participants-section', 'discussion-publications-section', 'discussion-pins-section', 'discussion-files-section', 'discussion-notifications-section'];
const key = (user = '1', instance = 'discussion1') => 'silverpeas:discussion:view:v1:' + user + ':' + instance;
async function setup() {
  const browser = await chromium.launch({executablePath: process.env.CHROME_PATH, args: ['--no-sandbox']});
  const page = await browser.newPage();
  await page.route('http://discussion.test/**', route => {
    const url = new URL(route.request().url());
    if (url.pathname === '/state.js') {return route.fulfill({contentType: 'application/javascript', body: script});}
    return route.fulfill({contentType: 'text/html', body: `<!doctype html><script>window.DiscussionContext={embedded:${url.searchParams.has('embedded')}};</script><script src="/state.js" defer></script><main id="discussion" data-user-id="${url.searchParams.get('user') || '1'}" data-instance-id="${url.searchParams.get('instance') || 'discussion1'}"><div id="discussion-sidebar-accordions"><details id="search" name="sidebar" hidden><summary>Search</summary></details>${ids.map((id, index) => `<details id="${id}" name="sidebar" ${index === 0 ? 'open' : ''}><summary>${id}</summary><p>Content</p></details>`).join('')}</div></main>`});
  });
  await page.goto('http://discussion.test/');
  return {browser, page};
}
const state = page => page.evaluate(storageKey => JSON.parse(localStorage.getItem(storageKey)), key());
const openSection = (page, id) => page.locator('#' + id).evaluate(node => node.open);
async function choose(page, id) {
  await page.locator('#' + id + ' summary').click();
  await page.waitForFunction(({storageKey, id}) => {
    try {return JSON.parse(localStorage.getItem(storageKey))?.accordions?.[id] === document.getElementById(id).open;}
    catch (pending) {return false;}
  }, {storageKey: key(), id});
}

test('accordion preferences restore after navigation, including every accordion closed, without changing first-visit defaults', async () => {
  const {browser, page} = await setup();
  try {
    assert.equal(await openSection(page, ids[0]), true);
    assert.equal(await state(page), null);
    await choose(page, ids[2]);
    assert.equal(await openSection(page, ids[0]), false);
    await page.reload();
    assert.equal(await openSection(page, ids[2]), true);
    await page.locator('#' + ids[2] + ' summary').focus();
    await page.keyboard.press('Space');
    await page.waitForFunction(storageKey => JSON.parse(localStorage.getItem(storageKey)).accordions['discussion-pins-section'] === false, key());
    await page.reload();
    for (const id of ids) {assert.equal(await openSection(page, id), false);}
  } finally {await browser.close();}
});

test('display preferences are isolated by user and instance and shared by channels within the same instance', async () => {
  const {browser, page} = await setup();
  try {
    await choose(page, ids[3]);
    await page.evaluate(() => DiscussionViewState.rememberChannels(false));
    await page.goto('http://discussion.test/?channel=other');
    assert.equal(await openSection(page, ids[3]), true);
    assert.equal(await page.evaluate(() => DiscussionViewState.channelsOpen(true)), false);
    for (const suffix of ['?user=2', '?instance=discussion2']) {
      await page.goto('http://discussion.test/' + suffix);
      assert.equal(await openSection(page, ids[0]), true);
      assert.equal(await page.evaluate(() => DiscussionViewState.channelsOpen(true)), true);
    }
    await page.goto('http://discussion.test/');
    assert.equal(await openSection(page, ids[3]), true);
  } finally {await browser.close();}
});

test('temporary search, thread hiding and embedded thread views do not overwrite preferences', async () => {
  const {browser, page} = await setup();
  try {
    await choose(page, ids[1]);
    const saved = await state(page);
    await page.evaluate(() => {
      document.getElementById('discussion-sidebar-accordions').hidden = true;
      document.getElementById('discussion-sidebar-accordions').hidden = false;
      document.getElementById('search').open = true;
    });
    assert.deepEqual(await state(page), saved);
    await page.goto('http://discussion.test/?embedded=1');
    assert.equal(await openSection(page, ids[0]), true);
    await page.locator('#' + ids[4] + ' summary').click();
    assert.deepEqual(await state(page), saved);
    await page.goto('http://discussion.test/');
    assert.equal(await openSection(page, ids[1]), true);
  } finally {await browser.close();}
});

test('malformed preferences and unavailable browser storage leave native controls functional', async () => {
  const {browser, page} = await setup();
  try {
    await page.evaluate(storageKey => localStorage.setItem(storageKey, '{broken'), key());
    await page.reload();
    assert.equal(await openSection(page, ids[0]), true);
    await choose(page, ids[2]);
    assert.equal((await state(page)).accordions[ids[2]], true);
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.addInitScript(() => Object.defineProperty(window, 'localStorage', {get() {throw new DOMException('Blocked', 'SecurityError');}}));
    await page.reload();
    await page.locator('#' + ids[4] + ' summary').click();
    assert.equal(await openSection(page, ids[4]), true);
    assert.equal(await page.evaluate(() => {DiscussionViewState.rememberChannels(false); return DiscussionViewState.channelsOpen(true);}), true);
    assert.deepEqual(errors, []);
  } finally {await browser.close();}
});
