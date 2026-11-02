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
const root = path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript');
const launch = () => chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
const message = id => ({instanceId:'discussion1',messageId:id,userId:'2',title:'Équipe',text:'Alice : Bonjour',url:'/silverpeas/Rdiscussion/discussion1/messages/'+id});
async function setup(page) {
  await page.route('https://silverpeas.test/**', route=>route.fulfill({contentType:'text/html; charset=utf-8',body:`
    <main id="discussion" data-instance-id="discussion1" data-user-id="2">
      <form id="discussion-notification-settings" action="/notification-preferences" data-enabled="true" data-revision="1" data-denied="Bloquées" data-unsupported="Indisponibles">
        <label><input type="checkbox" id="discussion-notifications-enabled">Notifications de bureau</label>
        <button type="button" id="discussion-desktop-enable" hidden>Autoriser</button><p id="discussion-notification-status" hidden></p><p id="discussion-notification-error" hidden>Erreur</p>
      </form>
    </main>`}));
  await page.goto('https://silverpeas.test/');
  await page.evaluate(()=>{
    window.handlers={};window.requests=[];window.notifications=[];window.fail=false;window.server={enabled:true,revision:1};
    window.whenSilverpeasReady=fn=>fn();
    window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn,id)=>{if(!id)throw Error('Missing native listener ID');(window.handlers[name]||=[]).push(fn);},removeEventListener:(name,fn)=>{window.handlers[name]=(window.handlers[name]||[]).filter(f=>f!==fn);}};
    window.emit=(name,data)=>(window.handlers[name]||[]).forEach(fn=>fn({data:JSON.stringify(data)}));
    function Notification(){} Notification.permission='granted';window.Notification=Notification;
    window.spUserNotification={notifyOnDesktop:(title,options)=>window.notifications.push({title,options})};
    window.sp={ajaxRequest:url=>{
      const request={url,method:'GET'};
      const api={byPostMethod:()=>{request.method='POST';return api;},withParam:(name,value)=>{request[name]=value;return api;},send:async()=>{
        window.requests.push(request);if(window.fail)throw Error('Unavailable');
        if(request.method==='POST')window.server={enabled:request.enabled==='true',revision:window.server.revision+1};
        return {responseText:`<span class="discussion-notification-preference" data-enabled="${window.server.enabled}" data-revision="${window.server.revision}"></span>`};
      }};return api;
    }};
  });
  await page.addScriptTag({path:path.join(root,'discussion-desktop.js')});
  await page.addScriptTag({path:path.join(root,'discussion-notification-settings.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}

test('saving the personal preference suppresses desktop alerts outside Discussion and newer settings synchronize other sessions',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    assert.equal(await page.locator('#discussion-notifications-enabled').isChecked(),true);
    await page.locator('#discussion-notifications-enabled').click();
    await page.waitForFunction(()=>!document.querySelector('input').disabled);
    assert.deepEqual(await page.evaluate(()=>window.requests[0]),{url:'https://silverpeas.test/notification-preferences',method:'POST',enabled:'false'});
    await page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m1'));
    await page.waitForTimeout(250);
    assert.equal(await page.evaluate(()=>window.notifications.length),0);
    await page.evaluate(()=>{
      window.emit('DISCUSSION_NOTIFICATION_PREFERENCE',{instanceId:'discussion1',userId:'other',enabled:true,revision:20});
    });
    assert.equal(await page.locator('#discussion-notifications-enabled').isChecked(),false);
    await page.evaluate(()=>{
      window.emit('DISCUSSION_NOTIFICATION_PREFERENCE',{instanceId:'discussion1',userId:'2',enabled:true,revision:3});
      window.emit('DISCUSSION_NOTIFICATION_PREFERENCE',{instanceId:'discussion1',userId:'2',enabled:false,revision:2});
    });
    assert.equal(await page.locator('#discussion-notifications-enabled').isChecked(),true);
    await page.evaluate(data=>window.emit('DISCUSSION_NEW_MESSAGE',data),message('m2'));
    await page.waitForFunction(()=>window.notifications.length===1);
  } finally {await browser.close();}
});

test('failed saves restore the previous preference and reconnects fetch the current server setting',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.evaluate(()=>{window.fail=true;});
    await page.locator('#discussion-notifications-enabled').click();
    await page.waitForFunction(()=>!document.getElementById('discussion-notification-error').hidden);
    assert.equal(await page.locator('#discussion-notifications-enabled').isChecked(),true);
    await page.evaluate(()=>{window.fail=false;window.server={enabled:false,revision:8};window.emit('OPEN_EVENT_SOURCE',{});});
    await page.waitForFunction(()=>!document.getElementById('discussion-notifications-enabled').checked);
    assert.equal(await page.evaluate(()=>window.requests.filter(r=>r.method==='GET').length),1);
  } finally {await browser.close();}
});

test('browser permission feedback does not alter the stored preference',async()=>{
  const browser=await launch();
  try {
    const page=await browser.newPage();await setup(page);
    await page.evaluate(()=>{window.Notification.permission='denied';window.dispatchEvent(new Event('focus'));});
    assert.equal(await page.locator('#discussion-notification-status').textContent(),'Bloquées');
    assert.equal(await page.locator('#discussion-notifications-enabled').isChecked(),true);
    await page.evaluate(()=>{window.Notification=undefined;window.dispatchEvent(new Event('focus'));});
    assert.equal(await page.locator('#discussion-notification-status').textContent(),'Indisponibles');
    assert.equal(await page.evaluate(()=>window.requests.length),0);
  } finally {await browser.close();}
});
