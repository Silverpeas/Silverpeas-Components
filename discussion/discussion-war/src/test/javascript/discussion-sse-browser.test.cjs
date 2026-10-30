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
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const {chromium} = require('playwright');

test('a real SSE signal updates two browser pages without periodic requests', async () => {
  const clients = new Set();
  let last = 1;
  let requests = 0;
  const rows = () => Array.from({length:last}, (_,i) => `<article class="discussion-message" id="message-${i+1}" data-sequence="${i+1}"><p>Message ${i+1}</p></article>`).join('');
  const snapshot = () => `<div class="discussion-page" data-unread-count="0" data-last-sequence="${last}">${rows()}</div>`;
  const server = http.createServer((request, response) => {
    if(request.url === '/sse') {
      response.writeHead(200, {'Content-Type':'text/event-stream','Cache-Control':'no-cache'});
      response.write('event: OPEN_EVENT_SOURCE\ndata: connected\n\n');
      clients.add(response);
      request.on('close', () => clients.delete(response));
    } else if(request.url.startsWith('/updates')) {
      requests++;
      response.writeHead(200, {'Content-Type':'text/html; charset=utf-8'});
      response.end(snapshot());
    } else if(request.url === '/live.js') {
      response.writeHead(200, {'Content-Type':'text/javascript'});
      response.end(fs.readFileSync(path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-live.js')));
    } else {
      response.writeHead(200, {'Content-Type':'text/html; charset=utf-8'});
      response.end(`<!DOCTYPE html><main id="discussion" data-instance-id="discussion1" data-updates="/updates"><span class="discussion-unread-count">0</span><form class="discussion-read-form"><input name="sequence" value="1"></form><div id="discussion-scroll"><div id="discussion-history">${snapshot()}</div></div><p id="discussion-live-error" hidden>Error</p><textarea id="draft">Draft</textarea></main><script>
        const connection=new EventSource('/sse');
        window.seen=0;
        connection.addEventListener('DISCUSSION_CHANGED',()=>window.seen++);
        window.spServerEventSource={isEnabled:()=>true,addEventListener:(...args)=>connection.addEventListener(...args),removeEventListener:(...args)=>connection.removeEventListener(...args)};
        window.sp={ajaxRequest:url=>{
          const params=new URLSearchParams();
          const req={withParam:(key,value)=>{params.set(key,value);return req;},send:()=>fetch(url+'?'+params).then(async r=>({responseText:await r.text()}))};
          return req;
        }};
        </script><script src="/live.js" defer></script>`);
    }
  });
  await new Promise(resolve => server.listen(0,'127.0.0.1',resolve));
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,args:['--no-sandbox']});
  try {
    const first=await browser.newPage();
    const second=await browser.newPage();
    const url='http://127.0.0.1:'+server.address().port;
    await first.goto(url);
    await second.goto(url);
    await first.waitForFunction(()=>document.getElementById('discussion-live-error').hidden);
    await second.waitForFunction(()=>document.getElementById('discussion-live-error').hidden);
    assert.equal(clients.size,2);
    const emit=instance=>clients.forEach(client=>client.write('event: DISCUSSION_CHANGED\ndata: '+JSON.stringify({instanceId:instance})+'\n\n'));
    emit('discussion1');
    await first.waitForFunction(()=>window.seen>=1);
    await second.waitForFunction(()=>window.seen>=1);
    // Wait for the requests caused by the first signal to finish before comparing counts.
    await first.waitForFunction(()=>document.querySelector('#message-1'));
    await new Promise(resolve=>setTimeout(resolve,100));
    const idle=requests;
    emit('other-instance');
    await first.waitForFunction(()=>window.seen>=2);
    await second.waitForFunction(()=>window.seen>=2);
    await new Promise(resolve=>setTimeout(resolve,100));
    assert.equal(requests,idle);
    last=2;
    emit('discussion1');
    await first.waitForSelector('#message-2');
    await second.waitForSelector('#message-2');
    assert.equal(await first.locator('#message-2').count(),1);
    assert.equal(await second.locator('#message-2').count(),1);
    assert.equal(await first.locator('#draft').inputValue(),'Draft');
    assert.equal(await second.locator('#draft').inputValue(),'Draft');
  } finally {
    await browser.close();
    clients.forEach(client=>client.end());
    await new Promise(resolve=>server.close(resolve));
  }
});
