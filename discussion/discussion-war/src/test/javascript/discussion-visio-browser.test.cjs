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
const assets = path.join(__dirname, '../../main/webapp/discussion/jsp');
const render = text => text.replace(/<%[\s\S]*?%>/g, '').replace(/<view:setBundle[^>]*\/>/g, '')
    .replace(/<c:url var="visioBase"[^>]*\/>/g, '').replaceAll('${visioBase}', '/Rdiscussion/discussion1/channels/main/')
    .replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g, (_, key) => key);
const dialog = render(fs.readFileSync(path.join(assets, 'visio-dialog.jsp'), 'utf8'));
const button = render(fs.readFileSync(path.join(assets, 'discussion.jsp'), 'utf8')
    .match(/<button id="discussion-add-visio"[\s\S]*?<\/button>/)[0]);
async function setup(available = true) {
  const browser = await chromium.launch({executablePath: process.env.CHROME_PATH, args: ['--no-sandbox']});
  const page = await browser.newPage();
  await page.setContent(`<base href="https://discussion.test/"><main id="discussion" data-thread-root-id="root"><form class="discussion-composer"><textarea id="draft">Brouillon</textarea><div class="discussion-editor-tools"></div>${button}</form>${dialog}</main>`);
  await page.addStyleTag({content: fs.readFileSync(path.join(assets, 'css/discussion.css'), 'utf8')});
  await page.evaluate(enabled => {
    window.enabled = enabled;
    window.calls = [];
    window.changes = 0;
    document.addEventListener('discussion:changed', () => changes++);
    window.sp = {ajaxRequest: url => {
      const request = {url, params: {}};
      return {
        byPostMethod() {request.post = true; return this;},
        withParam(key, value) {request.params[key] = value; return this;},
        send() {
          calls.push(request);
          if (!request.post) {return Promise.resolve({responseText: `<span class="discussion-visio-options" data-available="${window.enabled}" data-invitees="2"></span>`});}
          return new Promise((resolve, reject) => {
            request.resolve = () => resolve({responseText: '<span class="discussion-visio-created" data-id="room" data-url="https://video.test/room"></span>'});
            request.reject = () => reject(new Error('offline'));
          });
        }
      };
    }};
  }, available);
  await page.addScriptTag({path: path.join(assets, 'javaScript/discussion-visio.js')});
  await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser, page};
}

test('video button remains hidden when native video is disabled', async () => {
  const {browser, page} = await setup(false);
  try {
    await page.waitForFunction(() => calls.length === 1);
    assert.equal(await page.locator('#discussion-add-visio').isVisible(), false);
    assert.equal(await page.evaluate(() => calls.some(call => call.post)), false);
  } finally {await browser.close();}
});

test('invites from the current thread, preserves draft, prevents duplicate submits and offers a safe join link', async () => {
  const {browser, page} = await setup();
  try {
    await page.locator('#discussion-add-visio').click();
    await page.locator('#discussion-visio-title').fill('Review');
    assert.equal(await page.locator('#discussion-visio-count').textContent(), '2');
    await page.locator('#discussion-visio-submit').click();
    await page.evaluate(() => document.getElementById('discussion-visio-form').dispatchEvent(new Event('submit', {cancelable: true})));
    const posts = await page.evaluate(() => calls.filter(call => call.post).map(({url, params}) => ({url, params})));
    assert.equal(posts.length, 1);
    assert.deepEqual(posts[0].params, {title: 'Review', threadRootId: 'root'});
    assert.ok(posts[0].url.endsWith('/channels/main/meetings'));
    await page.evaluate(() => calls.find(call => call.post).resolve());
    await page.waitForFunction(() => changes === 1);
    assert.equal(await page.locator('#draft').inputValue(), 'Brouillon');
    assert.equal(await page.locator('#discussion-visio-join').getAttribute('href'), 'https://video.test/room');
    assert.equal(await page.locator('#discussion-visio-join').getAttribute('rel'), 'noopener noreferrer');
    assert.equal(await page.locator('#discussion-visio-dialog').evaluate(element => element.open), false);
  } finally {await browser.close();}
});

test('creation error leaves the dialog and title available for correction; cancel sends nothing', async () => {
  const {browser, page} = await setup();
  try {
    await page.locator('#discussion-add-visio').click();
    await page.locator('.discussion-visio-cancel').first().click();
    assert.equal(await page.evaluate(() => calls.some(call => call.post)), false);
    await page.locator('#discussion-add-visio').click();
    await page.locator('#discussion-visio-title').fill('Review');
    await page.locator('#discussion-visio-submit').click();
    await page.evaluate(() => calls.find(call => call.post).reject());
    await page.locator('#discussion-visio-error').waitFor({state: 'visible'});
    assert.equal(await page.locator('#discussion-visio-title').inputValue(), 'Review');
    assert.equal(await page.locator('#discussion-visio-submit').isEnabled(), true);
    assert.equal(await page.evaluate(() => changes), 0);
  } finally {await browser.close();}
});
