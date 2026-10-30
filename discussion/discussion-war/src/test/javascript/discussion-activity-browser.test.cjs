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
const test=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const {chromium}=require('playwright');
const scripts=path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript');
const launch=()=>chromium.launch({executablePath:process.env.CHROME_PATH||undefined,args:['--no-sandbox']});
async function setup(page,{activity=false,focused=false,permission='granted'}={}) {
  await page.route('https://silverpeas.test/**',route=>route.fulfill({contentType:'text/html; charset=utf-8',body:activity
    ? '<main id="discussion" data-instance-id="discussion1" data-user-id="2" data-typing="/typing" data-typing-one="{0} écrit…" data-typing-many="{0} écrivent…"><button id="discussion-desktop-enable" hidden>Activer</button><form class="discussion-composer"><p id="discussion-typing" hidden></p><textarea></textarea><button type="submit">Publier</button></form></main>'
    : '<main>Another Silverpeas application</main>'}));
  await page.goto('https://silverpeas.test/silverpeas/test');
  await page.evaluate(({focused,permission})=>{
    window.webContext='/silverpeas';
    window.focused=focused;
    document.hasFocus=()=>window.focused;
    window.handlers={};window.notifications=[];window.loaded=[];window.requests=[];window.permissions=0;
    window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn,id)=>{if(!id&&!window.handlers[name])throw new TypeError('Native SSE removal before first registration');(window.handlers[name]||=[]).push(fn);},removeEventListener:(name,fn)=>{window.handlers[name]=(window.handlers[name]||[]).filter(f=>f!==fn);}};
    window.emit=(name,data)=>(window.handlers[name]||[]).forEach(fn=>fn({data:JSON.stringify(data)}));
    window.whenSilverpeasReady=fn=>fn();
    function MockNotification(){}
    MockNotification.permission=permission;
    MockNotification.requestPermission=async()=>{window.permissions++;MockNotification.permission='granted';return 'granted';};
    window.Notification=MockNotification;
    window.spUserNotification={notifyOnDesktop:(title,options,callback)=>{
      window.notifications.push({title,options});
      const notification={close:()=>{}};
      callback(notification);window.clickNotification=()=>notification.onclick();
    }};
    window.spWindow={loadLink:url=>window.loaded.push(url)};
    window.focus=()=>{};
    window.sp={ajaxRequest:url=>{
      const request={url};
      const api={byPostMethod:()=>api,withParam:(name,value)=>{request[name]=value;return api;},send:async()=>{window.requests.push(request);return {responseText:'<span></span>'};}};
      return api;
    }};
  },{focused,permission});
  await page.addScriptTag({path:path.join(scripts,'discussion-desktop.js')});
  if(activity) await page.addScriptTag({path:path.join(scripts,'discussion-activity.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}
const message=id=>({instanceId:'discussion1',messageId:id,userId:'2',title:'Projet IA',text:'Bernard a publié un nouveau message.',url:'/silverpeas/Rdiscussion/discussion1/messages/'+id});

test('typing signals contain no draft and received indicators expire or stop without stale updates',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();
    await setup(page,{activity:true,focused:true});
    await page.locator('textarea').fill('Private draft that must not leave the editor');
    await page.waitForFunction(()=>window.requests.length===1);
    assert.equal(await page.evaluate(()=>window.requests[0].active),'true');
    assert.equal((await page.evaluate(()=>JSON.stringify(window.requests))).includes('Private draft'),false);
    await page.locator('textarea').fill('More private draft');
    assert.equal(await page.evaluate(()=>window.requests.length),1);
    await page.evaluate(()=>{
      window.emit('DISCUSSION_TYPING',{instanceId:'discussion1',userId:'3',name:'Bernard',revision:2,ttl:350});
      window.emit('DISCUSSION_TYPING',{instanceId:'discussion1',userId:'3',name:'Bernard',revision:1,ttl:0});
    });
    assert.equal(await page.locator('#discussion-typing').textContent(),'Bernard écrit…');
    await page.waitForFunction(()=>document.getElementById('discussion-typing').hidden);
    await page.evaluate(()=>window.emit('DISCUSSION_TYPING',{instanceId:'discussion1',userId:'2',name:'Self',revision:3,ttl:8000}));
    assert.equal(await page.locator('#discussion-typing').isVisible(),false);
    await page.evaluate(()=>{
      window.dispatchEvent(new Event('blur'));
      document.dispatchEvent(new Event('visibilitychange'));
    });
    assert.equal(await page.evaluate(()=>window.requests.some(r=>r.active==='false')),false);
    await page.waitForFunction(()=>window.requests.some(r=>r.active==='false'),null,{timeout:7000});
    assert.equal(await page.locator('textarea').inputValue(),'More private draft');
  } finally {await browser.close();}
});

test('a global desktop event works outside Discussion, deduplicates messages and opens the right message',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();
    await setup(page);
    await page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m1'));
    await page.waitForFunction(()=>window.notifications.length===1);
    assert.equal(await page.evaluate(()=>window.notifications[0].title),'Projet IA');
    await page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m1'));
    await page.waitForTimeout(250);
    assert.equal(await page.evaluate(()=>window.notifications.length),1);
    await page.evaluate(()=>window.clickNotification());
    assert.deepEqual(await page.evaluate(()=>window.loaded),['/silverpeas/Rdiscussion/discussion1/messages/m1']);
    await page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),{...message('evil'),url:'https://evil.test/Rdiscussion/discussion1/messages/evil'});
    assert.equal(await page.evaluate(()=>window.notifications.length),1);
  } finally {await browser.close();}
});

test('desktop alerts are suppressed when the discussion is active in another tab and claimed only once otherwise',async()=>{
  const browser=await launch();
  const context=await browser.newContext();
  try {
    const first=await context.newPage();const second=await context.newPage();
    await setup(first,{focused:true});await setup(second);
    await first.evaluate(()=>window.DiscussionDesktop.register('discussion1','2',()=>window.focused));
    await Promise.all([first,second].map(page=>page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m1'))));
    await second.waitForTimeout(300);
    assert.equal(await first.evaluate(()=>window.notifications.length),0);
    assert.equal(await second.evaluate(()=>window.notifications.length),0);
    await first.evaluate(()=>{window.focused=false;});
    await Promise.all([first,second].map(page=>page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m2'))));
    await first.waitForFunction(()=>JSON.parse(localStorage.getItem('silverpeas.discussion.'+window.webContext+'.desktop.seen')||'[]').includes('2:m2'));
    const counts=await Promise.all([first,second].map(page=>page.evaluate(()=>window.notifications.length)));
    assert.equal(counts[0]+counts[1],1);
  } finally {await browser.close();}
});

test('permission is requested only by the explicit enable button',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();
    await setup(page,{activity:true,focused:true,permission:'default'});
    assert.equal(await page.locator('#discussion-desktop-enable').isVisible(),true);
    assert.equal(await page.evaluate(()=>window.permissions),0);
    await page.locator('#discussion-desktop-enable').click();
    assert.equal(await page.evaluate(()=>window.permissions),1);
    assert.equal(await page.locator('#discussion-desktop-enable').isVisible(),false);
    await page.evaluate(()=>window.dispatchEvent(new Event('pagehide')));
    assert.equal(await page.evaluate(()=>(window.handlers.DISCUSSION_TYPING||[]).length),0);
    assert.equal(await page.evaluate(()=>(window.handlers.DISCUSSION_NEW_MESSAGE||[]).length),1);
  } finally {await browser.close();}
});
