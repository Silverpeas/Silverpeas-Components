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
const script = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript/discussion-user-zoom.js');
async function setup() {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page = await browser.newPage();
  await page.setContent('<main id="discussion"><article><span class="discussion-author"><span class="userToZoom" rel="2">Alice</span></span></article><aside id="participants"><span class="userToZoom" rel="3">Bernard</span></aside><span id="current-user">Administrateur</span></main>');
  await page.evaluate(() => {
    const data = new WeakMap();
    window.calls = [];
    window.whenSilverpeasReady = fn => fn();
    window.jQuery = node => ({
      data:key => data.get(node)?.[key],
      userZoom:options => {
        if (data.has(node)) { throw Error('Duplicate native initialization'); }
        data.set(node, {userZoom:true});
        calls.push({id:options.id, label:node.textContent});
        node.addEventListener('mouseover', () => { node.dataset.nativeCard = options.id; });
      }
    });
  });
  await page.addScriptTag({path:script});
  await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}

test('native userZoom activates author and participant names once, including realtime and history fragments', async () => {
  const {browser,page} = await setup();
  try {
    assert.deepEqual(await page.evaluate(()=>calls.map(call=>call.id)),['2','3']);
    await page.locator('.discussion-author .userToZoom').hover();
    assert.equal(await page.locator('.discussion-author .userToZoom').getAttribute('data-native-card'),'2');
    await page.evaluate(()=>{
      const article=document.createElement('article');
      article.innerHTML='<span class="discussion-author"><span class="userToZoom" rel="4">Claire</span></span>';
      document.getElementById('discussion').append(article);
      document.getElementById('participants').innerHTML='<span class="userToZoom" rel="5">David</span>';
    });
    await page.waitForFunction(()=>calls.length===4);
    assert.deepEqual(await page.evaluate(()=>calls.map(call=>call.id)),['2','3','4','5']);
    await page.evaluate(()=>{
      const user=document.querySelector('.discussion-author .userToZoom');
      const status=document.createElement('span');status.textContent='Connecté';user.append(status);
      document.getElementById('participants').append(user);
    });
    await page.waitForTimeout(30);
    assert.equal(await page.evaluate(()=>calls.length),4);
    assert.equal(await page.locator('#current-user').getAttribute('data-native-card'),null);
  } finally {await browser.close();}
});

test('native activation resumes after browser cache restoration and supports directly inserted names', async () => {
  const {browser,page} = await setup();
  try {
    await page.evaluate(()=>{
      window.dispatchEvent(new PageTransitionEvent('pagehide',{persisted:true}));
      const name=document.createElement('span');name.className='userToZoom';name.setAttribute('rel','6');name.textContent='Emma';
      document.getElementById('participants').append(name);
    });
    assert.equal(await page.evaluate(()=>calls.length),2);
    await page.evaluate(()=>window.dispatchEvent(new PageTransitionEvent('pageshow',{persisted:true})));
    await page.waitForFunction(()=>calls.length===3);
    assert.equal(await page.evaluate(()=>calls[2].id),'6');
    await page.evaluate(()=>{
      const name=document.createElement('span');name.className='userToZoom';name.setAttribute('rel','7');name.textContent='Fabien';
      document.getElementById('participants').append(name);
    });
    await page.waitForFunction(()=>calls.length===4);
    assert.equal(await page.evaluate(()=>calls[3].id),'7');
  } finally {await browser.close();}
});
