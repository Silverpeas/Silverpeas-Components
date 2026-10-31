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
const script = path.join(__dirname, '../../main/webapp/discussion/jsp/javaScript/discussion-live.js');
const css = path.join(__dirname, '../../main/webapp/discussion/jsp/css/discussion.css');
const row = (id, body = 'Message ' + id, unread = false) => `<article id="message-${id}" class="discussion-message ${unread ? 'discussion-message--unread' : ''}" data-sequence="${id}"><p class="discussion-body">${body}</p><details><summary>Edit</summary><textarea name="body">${body}</textarea></details><button class="discussion-like-button">0</button></article>`;
const fragment = rows => `<div class="discussion-page" data-unread-count="1">${rows}<div class="discussion-unread-divider">Unread</div></div>`;
const launch = () => chromium.launch({executablePath: process.env.CHROME_PATH || undefined, args: ['--no-sandbox']});
async function open(browser, count = 30, focus = '') {
  const page = await browser.newPage();
  await page.setContent(`<main id="discussion" data-updates="/updates" data-instance-id="discussion1" data-message-id="${focus}"><span class="discussion-unread-count">0</span><form class="discussion-read-form" hidden><input name="sequence" value="${count}"></form><div id="discussion-scroll" class="discussion-scroll"><section id="discussion-history"><div class="discussion-page" data-before="1" data-more="false">${Array.from({length: count}, (_, i) => row(i+1)).join('')}</div></section></div><p id="discussion-live-error">Error</p><textarea id="draft">Draft</textarea></main>`);
  await page.addStyleTag({path: css});
  await page.addStyleTag({content: '#discussion #discussion-scroll{height:250px;min-height:0}.discussion-message{min-height:100px}'});
  await page.evaluate(() => {
    window.calls = [];
    window.intervals = 0;
    window.setInterval = () => { window.intervals++; };
    window.listeners = {};
    window.spServerEventSource = {
      isEnabled: () => true,
      addEventListener: (name, fn, id) => {
        if (!id && !window.listeners[name]) throw new TypeError('Native SSE removal before first registration');
        window.listeners[name] = fn;
      },
      removeEventListener: name => { delete window.listeners[name]; }
    };
    window.tick = () => window.listeners.OPEN_EVENT_SOURCE();
    window.initializing = true;
    window.DiscussionEditor = {enhance: () => {}};
    window.sp = {ajaxRequest: () => {
      const params = {};
      const request = {withParam: (name, value) => { params[name] = value; return request; }, send: () => {
        window.calls.push(params);
        if (window.initializing) {
          const snapshot = document.querySelector('.discussion-page').cloneNode(true);
          snapshot.dataset.unreadCount = '0';
          return Promise.resolve({responseText: snapshot.outerHTML});
        }
        return new Promise((resolve, reject) => { window.complete = text => resolve({responseText: text}); window.fail = reject; });
      }};
      return request;
    }};
    document.getElementById('discussion-scroll').scrollTop = 100000;
  });
  await page.addScriptTag({path: script});
  await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
  await page.waitForFunction(() => document.getElementById('discussion-live-error').hidden);
  await page.evaluate(() => { window.initializing = false; window.calls = []; });
  return page;
}
async function refresh(page, html) {
  await page.evaluate(() => { window.pending = window.tick(); });
  await page.evaluate(html => window.complete(html), html);
  await page.evaluate(() => window.pending);
}

test('live updates append, edit, delete and change reactions without reload or duplicate rows', async () => {
  const browser = await launch();
  try {
    const page = await open(browser);
    assert.equal(await page.evaluate(() => window.intervals), 0);
    await refresh(page, fragment(row(1, 'Updated') + row(2, 'Deleted') + row(3, 'Liked once') + row(31, 'New message', true)));
    assert.equal(await page.locator('#message-1 .discussion-body').textContent(), 'Updated');
    assert.equal(await page.locator('#message-2 .discussion-body').textContent(), 'Deleted');
    assert.equal(await page.locator('#message-3 .discussion-body').textContent(), 'Liked once');
    assert.equal(await page.locator('.discussion-unread-count').textContent(), '1');
    assert.equal(await page.evaluate(() => {const s=document.getElementById('discussion-scroll'); return s.scrollHeight-s.scrollTop-s.clientHeight<2;}), true);
    await refresh(page, fragment(row(31, 'New message', true)));
    assert.equal(await page.locator('#message-31').count(), 1);
    assert.equal(await page.locator('.discussion-unread-divider').count(), 1);
    await page.evaluate(() => { window.sameRow = document.getElementById('message-31'); });
    await refresh(page, fragment(row(31, 'New message', true)));
    assert.equal(await page.evaluate(() => window.sameRow === document.getElementById('message-31')), true);
    assert.equal(await page.locator('#draft').inputValue(), 'Draft');
    assert.equal(await page.evaluate(() => window.calls[1].after), 31);
    await page.addStyleTag({content: ':root{--bgColor-color-im-info:rgb(255,255,0)}'});
    const color = await page.locator('#message-31').evaluate(node => getComputedStyle(node).backgroundColor);
    assert.notEqual(color, 'rgb(255, 255, 0)');
  } finally { await browser.close(); }
});

test('updates preserve the reading anchor, open edit drafts and pending reactions', async () => {
  const browser = await launch();
  try {
    const page = await open(browser);
    await page.evaluate(() => {
      document.getElementById('discussion-scroll').scrollTop=900;
      document.querySelector('#message-8 details').open=true;
      document.querySelector('#message-8 textarea').value='Unfinished edit';
      document.querySelector('#message-9 button').disabled=true;
    });
    const anchor = await page.locator('#message-10').boundingBox();
    await refresh(page, fragment(row(1, 'Long update<br>'.repeat(30)) + row(8, 'Remote edit') + row(9, 'Reaction in flight') + row(31, 'New')));
    assert.equal(await page.locator('#message-8 textarea').inputValue(), 'Unfinished edit');
    assert.equal(await page.locator('#message-9 .discussion-body').textContent(), 'Message 9');
    const current = await page.locator('#message-10').boundingBox();
    assert.ok(Math.abs(anchor.y-current.y)<2);
  } finally { await browser.close(); }
});

test('refresh requests are bounded, resume after errors and pause while hidden or leaving', async () => {
  const browser = await launch();
  try {
    const page = await open(browser, 205);
    await page.evaluate(() => { window.pending=window.tick(); });
    await page.evaluate(() => window.tick());
    assert.equal(await page.evaluate(() => window.calls.length), 1);
    await page.evaluate(() => window.fail(new Error('Offline')));
    await page.evaluate(() => window.pending);
    assert.equal(await page.locator('#discussion-live-error').isVisible(), true);
    await page.evaluate(() => { Object.defineProperty(document, 'hidden', {configurable:true,value:true}); });
    await page.evaluate(() => window.tick());
    assert.equal(await page.evaluate(() => window.calls.length), 1);
    await page.evaluate(() => { Object.defineProperty(document, 'hidden', {configurable:true,value:false}); window.pending=window.tick(); });
    for(let i=0;i<3;i++) {
      await page.waitForFunction(n => window.calls.length >= n, i+2);
      await page.evaluate(html => window.complete(html), fragment(''));
    }
    await page.evaluate(() => window.pending);
    assert.deepEqual(await page.evaluate(() => window.calls.slice(1).map(call => call.ids.split(',').length)), [100,100,5]);
    assert.equal(await page.locator('#discussion-live-error').isVisible(), false);
    await page.evaluate(() => { window.pending=window.tick(); window.dispatchEvent(new Event('pagehide')); });
    await page.evaluate(html => window.complete(html), fragment(row(206)));
    await page.evaluate(() => window.pending);
    assert.equal(await page.locator('#message-206').count(), 0);
    assert.deepEqual(await page.evaluate(() => Object.keys(window.listeners)), []);
    const calls = await page.evaluate(() => window.calls.length);
    await page.evaluate(() => window.dispatchEvent(new Event('pageshow')));
    assert.equal(await page.evaluate(() => window.calls.length), calls+1);
  } finally { await browser.close(); }
});


test('an initially empty conversation receives messages automatically', async () => {
  const browser = await launch();
  try {
    const page = await open(browser, 0);
    await page.locator('#discussion-history').evaluate(node => {
      const empty = document.createElement('div');
      empty.className='discussion-empty';
      empty.textContent='No messages';
      node.prepend(empty);
    });
    await refresh(page, fragment(row(1, 'First message', true)));
    assert.equal(await page.locator('#message-1').count(), 1);
    assert.equal(await page.locator('.discussion-empty').count(), 0);
    assert.deepEqual(await page.evaluate(() => window.calls), [{after:0,ids:''}]);
  } finally { await browser.close(); }
});


test('only matching instance events request synchronization and bursts queue one follow-up', async () => {
  const browser = await launch();
  try {
    const page = await open(browser);
    await page.evaluate(() => {
      window.listeners.DISCUSSION_CHANGED({data: '{"instanceId":"other"}'});
      window.listeners.DISCUSSION_CHANGED({data: 'invalid'});
    });
    assert.equal(await page.evaluate(() => window.calls.length), 0);
    await page.evaluate(() => {
      window.pending=window.tick();
      window.listeners.DISCUSSION_CHANGED({data: '{"instanceId":"discussion1"}'});
      window.listeners.DISCUSSION_CHANGED({data: '{"instanceId":"discussion1"}'});
    });
    await page.evaluate(html => window.complete(html), fragment(row(31)));
    await page.waitForFunction(() => window.calls.length === 2);
    await page.evaluate(html => window.complete(html), fragment(row(32)));
    await page.evaluate(() => window.pending);
    assert.equal(await page.locator('#message-32').count(), 1);
    assert.equal(await page.evaluate(() => window.calls.length), 2);
  } finally { await browser.close(); }
});

test('catch-up drains multiple batches without waiting for another event', async () => {
  const browser = await launch();
  try {
    const page = await open(browser,0);
    await page.evaluate(() => { window.pending=window.tick(); });
    await page.evaluate(html => window.complete(html), fragment(Array.from({length:100},(_,i)=>row(i+1)).join('')).replace('data-unread-count="1"','data-unread-count="1" data-last-sequence="101"'));
    await page.waitForFunction(() => window.calls.length === 2);
    assert.equal(await page.evaluate(() => window.calls[1].after), 100);
    await page.evaluate(html => window.complete(html), fragment(row(101)).replace('data-unread-count="1"','data-unread-count="1" data-last-sequence="101"'));
    await page.evaluate(() => window.pending);
    assert.equal(await page.locator('.discussion-message').count(),101);
  } finally { await browser.close(); }
});

test('a deferred edit is reconciled on closing its editor without another server event', async () => {
  const browser = await launch();
  try {
    const page = await open(browser);
    await page.locator('#message-8 details').evaluate(node => {node.open=true;});
    await refresh(page, fragment(row(8,'Changed remotely')));
    assert.equal(await page.locator('#message-8 .discussion-body').textContent(),'Message 8');
    await page.locator('#message-8 details').evaluate(node => {node.open=false;});
    await page.waitForFunction(() => window.calls.length === 2);
    await page.evaluate(html => window.complete(html), fragment(row(8,'Changed remotely')));
    await page.waitForFunction(() => document.querySelector('#message-8 .discussion-body').textContent==='Changed remotely');
  } finally { await browser.close(); }
});


test('initial catch-up preserves a notification target instead of jumping to the newest messages', async () => {
  const browser = await launch();
  try {
    const page = await open(browser,30,'30');
    const before = await page.locator('#message-30').boundingBox();
    await refresh(page,fragment(row(31)));
    const after = await page.locator('#message-30').boundingBox();
    assert.ok(Math.abs(before.y-after.y)<2);
    await page.locator('#discussion-scroll').dispatchEvent('wheel');
    await page.locator('#discussion-scroll').evaluate(node=>{node.scrollTop=node.scrollHeight;});
    await refresh(page,fragment(row(32)));
    assert.equal(await page.evaluate(()=>{const s=document.getElementById('discussion-scroll');return s.scrollHeight-s.scrollTop-s.clientHeight<2;}),true);
  } finally {await browser.close();}
});


test('private read events update the stream without refreshing citation content',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser);
    await page.evaluate(()=>{
      window.contentSignals=0;
      document.addEventListener('discussion:changed',()=>window.contentSignals++);
      window.listeners.DISCUSSION_CHANGED({data:'{"instanceId":"discussion1","contentChanged":false}'});
    });
    await page.waitForFunction(()=>window.calls.length===1);
    assert.equal(await page.evaluate(()=>window.contentSignals),0);
    await page.evaluate(html=>window.complete(html),fragment(row(31)));
    await page.waitForSelector('#message-31');
    await page.evaluate(()=>window.listeners.DISCUSSION_CHANGED({data:'{"instanceId":"discussion1","contentChanged":true}'}));
    await page.waitForFunction(()=>window.calls.length===2);
    assert.equal(await page.evaluate(()=>window.contentSignals),1);
    await page.evaluate(html=>window.complete(html),fragment(row(32)));
    await page.waitForSelector('#message-32');
  } finally {await browser.close();}
});


test('deleted messages vanish immediately, even during editing, while cursors and scroll anchors remain stable', async () => {
  const browser = await launch();
  try {
    const page = await open(browser);
    await page.evaluate(() => {
      const scroller = document.getElementById('discussion-scroll');
      document.querySelector('#message-10 details').open = true;
      document.querySelector('#message-10 textarea').focus({preventScroll:true});
      scroller.scrollTop += document.getElementById('message-10').getBoundingClientRect().top - scroller.getBoundingClientRect().top + 2;
    });
    const before = await page.locator('#message-12').boundingBox();
    const deleted = id => `<article id="message-${id}" class="discussion-message" data-sequence="${id}" hidden></article>`;
    await refresh(page, fragment(deleted(10) + deleted(31)));
    assert.equal(await page.locator('#message-10').isVisible(), false);
    assert.equal(await page.locator('#message-10').textContent(), '');
    assert.equal(await page.locator('#message-10 textarea').count(), 0);
    assert.equal(await page.locator('#message-31').isVisible(), false);
    const after = await page.locator('#message-12').boundingBox();
    assert.ok(Math.abs(before.y - after.y) < 2, 'Deleting a row must preserve the surviving scroll anchor: '+before.y+' -> '+after.y);
    await refresh(page, fragment(''));
    const request = await page.evaluate(() => window.calls.at(-1));
    assert.equal(request.after, 31);
    assert.ok(request.ids.split(',').includes('10'));
  } finally { await browser.close(); }
});
