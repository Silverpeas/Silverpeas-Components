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
const path = require('node:path');
const {chromium} = require('playwright');
const script = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript/discussion-pins.js');

async function setup(page) {
  await page.route('https://discussion.test/**', route => route.fulfill({contentType:'text/html; charset=utf-8', body:`
    <main id="discussion" data-pins="/pins">
      <article class="discussion-message"><form class="discussion-pin-form" action="/pin" data-pin-label="Épingler" data-unpin-label="Désépingler">
        <input name="pinned" value="true" type="hidden"><button aria-pressed="false">Épingler</button><span class="discussion-pin-error" hidden>Erreur</span>
      </form><input name="version" value="1" type="hidden"></article>
      <details id="discussion-pins-section"><summary>Messages épinglés <span id="discussion-pin-count">0</span></summary>
        <div id="discussion-pins"></div><p id="discussion-pins-error" hidden>Erreur de chargement</p>
      </details>
    </main>`}));
  await page.goto('https://discussion.test/');
  await page.evaluate(() => {
    window.pinned = false; window.fail = false; window.requests = []; window.listeners = {};
    window.whenSilverpeasReady = callback => callback();
    window.spServerEventSource = {isEnabled:()=>true,addEventListener:(name,fn,id)=>{if(!id)throw Error('Missing native listener ID');window.listeners[name]=fn;},removeEventListener:name=>delete window.listeners[name]};
    window.sp = {ajaxRequest:url=>{
      const request = {url};
      const api = {byPostMethod:()=>api,withParam:(name,value)=>{request[name]=value;return api;},send:async()=>{
        window.requests.push(request);
        if(window.fail)throw Error('Unavailable');
        if(url.endsWith('/pin')) {
          window.pinned=request.pinned==='true';
          return {responseText:`<span class="discussion-pin-state" data-pinned="${window.pinned}" data-version="2"></span>`};
        }
        return {responseText:`<div class="discussion-pins-list" data-count="${window.pinned?1:0}">${window.pinned?'<a href="/messages/older-message">Alice : Ancien message</a>':'Aucun message épinglé'}</div>`};
      }};
      return api;
    }};
  });
  await page.addScriptTag({path:script});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  await page.waitForFunction(()=>document.querySelector('.discussion-pins-list'));
}

test('pin and unpin update the shared sidebar and preserve editor versions without navigating',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage(); await setup(page);
    await page.locator('.discussion-pin-form button').click();
    await page.waitForFunction(()=>document.getElementById('discussion-pin-count').textContent==='1');
    assert.equal(await page.locator('.discussion-pin-form button').getAttribute('aria-pressed'),'true');
    assert.equal(await page.locator('input[name="version"]').inputValue(),'2');
    assert.equal(await page.locator('#discussion-pins a').getAttribute('href'),'/messages/older-message');
    assert.equal(await page.locator('#discussion-pins-section').getAttribute('open'),null);
    await page.locator('.discussion-pin-form button').click();
    await page.waitForFunction(()=>document.getElementById('discussion-pin-count').textContent==='0');
    assert.equal(await page.locator('.discussion-pin-form button').getAttribute('aria-pressed'),'false');
    assert.equal(page.url(),'https://discussion.test/');
  } finally {await browser.close();}
});

test('remote changes refresh pins and failed mutations retain the previous state',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage(); await setup(page);
    await page.evaluate(()=>{window.pinned=true;document.dispatchEvent(new Event('discussion:changed'));});
    await page.waitForFunction(()=>document.getElementById('discussion-pin-count').textContent==='1');
    await page.evaluate(()=>{window.fail=true;});
    await page.locator('.discussion-pin-form button').click();
    await page.waitForFunction(()=>!document.querySelector('.discussion-pin-error').hidden);
    assert.equal(await page.locator('.discussion-pin-form button').getAttribute('aria-pressed'),'false');
    assert.equal(await page.locator('#discussion-pin-count').textContent(),'1');
    assert.equal(await page.locator('.discussion-pin-form button').isEnabled(),true);
  } finally {await browser.close();}
});
