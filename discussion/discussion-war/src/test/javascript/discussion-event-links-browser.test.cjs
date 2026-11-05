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
const sharedEditor = require('./shared-editor-assets.cjs');
const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const {chromium} = require('playwright');
const script = path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-editor.js');
const encoded = Buffer.from('almanach22:CalendarEvent:16a8a453-2166-4e31-b5b4-ec723e8491d2').toString('base64');
const eventUrl = 'http://discussion.test/silverpeas/Contribution/'+encoded;
const original = '📅 Réunion de projet\n4 nov. 2026 11:01 – 4 nov. 2026 12:01 (Europe/Paris)\n'+eventUrl;
async function setup() {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage();
  await page.setContent(`<base href="http://discussion.test/"><main id="discussion" data-contribution-base="/silverpeas/Contribution/" data-view-event-label="Voir l’événement" data-view-idea-label="Voir l’idée" data-view-vote-label="Participer au vote" data-profile-base="/Rprofil/jsp/Main" data-icons="/icons.svg" data-emoji-label="Emoji" data-mention-label="Mention"><p id="body" class="discussion-body"></p><form><textarea name="body" maxlength="2000"></textarea></form><section id="later"></section></main>`);
  return {browser,page};
}
async function initialize(page) {
  await sharedEditor.load(page);
  await page.addScriptTag({path:script});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
}

test('stored event announcements display a short localized link without changing its destination or editor source',async()=>{
  const {browser,page}=await setup();
  try {
    await page.locator('#body').evaluate((node,text)=>{node.textContent=text;},original);
    await page.locator('textarea').fill(original);
    await initialize(page);
    const link=page.locator('#body a');
    assert.equal(await link.textContent(),'Voir l’événement');
    assert.equal(await link.getAttribute('href'),eventUrl);
    assert.equal(await link.getAttribute('title'),eventUrl);
    assert.equal(await link.getAttribute('target'),'_self');
    assert.equal(await link.evaluate(n=>n.classList.contains('sp-link')),true);
    assert.equal(await link.getAttribute('rel'),null);
    assert.equal(await page.locator('#body').textContent(),original.replace(eventUrl,'Voir l’événement'));
    assert.equal(await page.locator('textarea').inputValue(),original);
    assert.equal(await page.locator('input[name=body]').inputValue(),original);
  } finally {await browser.close();}
});

test('history and live fragments get the same short link while foreign, malformed and non-event links stay explicit',async()=>{
  const {browser,page}=await setup();
  try {
    await initialize(page);
    const publication='http://discussion.test/silverpeas/Contribution/'+Buffer.from('kmelia22:Publication:123').toString('base64');
    const external='https://external.test/silverpeas/Contribution/'+encoded;
    const otherContext='http://discussion.test/another-app/Contribution/'+encoded;
    const malformed='http://discussion.test/silverpeas/Contribution/%zz';
    const credentials='http://user:password@discussion.test/silverpeas/Contribution/'+encoded;
    await page.evaluate(urls=>{
      const section=document.getElementById('later');
      urls.forEach(url=>{const body=document.createElement('p');body.className='discussion-body';body.textContent=url;section.append(body);});
      DiscussionEditor.enhance(section);
      DiscussionEditor.enhance(section);
    },[eventUrl,publication,external,otherContext,malformed,credentials]);
    assert.equal(await page.locator('#later .discussion-event-link').count(),1);
    const texts=await page.locator('#later a').allTextContents();
    assert.deepEqual(texts,['Voir l’événement',publication,external,otherContext,malformed,credentials]);
    assert.equal(await page.locator('#later a').count(),6);
  } finally {await browser.close();}
});


test('native suggestion links use the short idea label while foreign links remain explicit',async()=>{
  const {browser,page}=await setup();
  try {
    const url='http://discussion.test/silverpeas/Contribution/'+Buffer.from('suggestionBox1:Suggestion:idea1').toString('base64');
    await page.locator('#body').evaluate((node,text)=>{node.textContent=text;},url);
    await initialize(page);
    assert.equal(await page.locator('#body a').textContent(),'Voir l’idée');
    assert.equal(await page.locator('#body a').getAttribute('href'),url);
    assert.equal(await page.locator('#body a').getAttribute('title'),url);
  } finally {await browser.close();}
});


test('only same-origin native Vote links have the participation label',async()=>{
  const {browser,page}=await setup();
  try {
    const url='http://discussion.test/silverpeas/RpollingStation/pollingStation1/searchResult?Type=QuestionContainer&Id=123';
    await page.locator('#body').evaluate((node,text)=>{node.textContent=text;},url);
    await initialize(page);
    assert.equal(await page.locator('#body a').textContent(),'Participer au vote');
    assert.equal(await page.locator('#body a').getAttribute('href'),url);
    await page.evaluate(urls=>{const section=document.getElementById('later');urls.forEach(url=>{const p=document.createElement('p');p.className='discussion-body';p.textContent=url;section.append(p);});DiscussionEditor.enhance(section);},[url.replace('discussion.test','external.test'),url.replace('RpollingStation/pollingStation1','Rsurvey/survey1')]);
    assert.equal((await page.locator('#later a').allTextContents()).includes('Participer au vote'),false);
  } finally {await browser.close();}
});


test('publications and native contributions use the Silverpeas navigation hook while foreign links keep a new tab',async()=>{
  const {browser,page}=await setup();
  try {
    const root='http://discussion.test/silverpeas/';
    const links=[root+'Publication/123',root+'Rkmelia/kmelia2/ViewPublication?PubId=123',root+'Rtoolbox/toolbox2/searchResult?Type=Publication&Id=123',root+'Contribution/'+Buffer.from('kmelia2:Publication:123').toString('base64'),eventUrl,root+'Contribution/'+Buffer.from('suggestionBox1:Suggestion:idea1').toString('base64'),root+'RpollingStation/pollingStation1/searchResult?Type=QuestionContainer&Id=123'];
    const external=['https://external.test/silverpeas/Publication/123',root.replace('/silverpeas/','/other/')+'Publication/123','http://user:pass@discussion.test/silverpeas/Publication/123',root+'attached_file/file1',root+'Rkmelia/kmelia2/searchResult?Type=Folder&Id=123'];
    await page.locator('#body').evaluate((n,text)=>{n.textContent=text;},[...links,...external].join('\n'));
    await initialize(page);
    const state=await page.locator('#body a').evaluateAll(nodes=>nodes.map(n=>({internal:n.classList.contains('sp-link'),target:n.target})));
    assert.deepEqual(state,[...links.map(()=>({internal:true,target:'_self'})),...external.map(()=>({internal:false,target:'_blank'}))]);
    await page.route(links[0],route=>route.fulfill({contentType:'text/html',body:'<p>Publication</p>'}));
    await page.locator('#body a').first().click();
    await page.waitForURL(links[0]);
    assert.equal(page.context().pages().length,1);
  } finally {await browser.close();}
});


test('authorized publication titles replace raw URLs on initial rendering, history and sidebar refresh without changing destinations',async()=>{
  const {browser,page}=await setup();
  try {
    const url='http://discussion.test/silverpeas/Publication/12';
    const alias='http://discussion.test/silverpeas/Rkmelia/kmelia1/ViewPublication?PubId=12';
    const foreign='http://external.test/silverpeas/Publication/12';
    await page.evaluate(({url,alias,foreign})=>{
      document.getElementById('body').textContent=[url,alias,foreign].join('\n');
      const list=document.createElement('div');list.className='discussion-publications-list';
      const a=document.createElement('a');a.dataset.publicationId='12';a.textContent='Architecture <img src=x>';
      list.append(a);document.getElementById('discussion').append(list);
    },{url,alias,foreign});
    await page.locator('textarea').fill(url);
    await initialize(page);
    assert.deepEqual(await page.locator('#body a').allTextContents(),['Architecture <img src=x>','Architecture <img src=x>',foreign]);
    assert.equal(await page.locator('#body img').count(),0);
    assert.equal(await page.locator('#body a').first().getAttribute('href'),url);
    assert.equal(await page.locator('#body a').first().getAttribute('title'),url);
    assert.equal(await page.locator('input[name=body]').inputValue(),url);
    await page.evaluate(url=>{
      document.querySelector('.discussion-publications-list a').textContent='Architecture mise à jour';
      document.dispatchEvent(new Event('discussion:publications-updated'));
      const p=document.createElement('p');p.className='discussion-body';p.textContent=url;
      document.getElementById('later').append(p);DiscussionEditor.enhance(document.getElementById('later'));
    },url);
    assert.equal(await page.locator('#body a').first().textContent(),'Architecture mise à jour');
    assert.equal(await page.locator('#later a').textContent(),'Architecture mise à jour');
    await page.evaluate(()=>{document.querySelector('.discussion-publications-list').replaceChildren();document.dispatchEvent(new Event('discussion:publications-updated'));});
    assert.equal(await page.locator('#body a').first().textContent(),url);
  } finally {await browser.close();}
});

test('pinned previews resolve publication titles before truncating and update safely with permissions and refreshed pins', async () => {
  const {browser, page} = await setup();
  try {
    const url = 'http://discussion.test/silverpeas/Publication/12';
    await page.evaluate(url => {
      const discussion = document.getElementById('discussion');
      discussion.insertAdjacentHTML('beforeend', '<div class="discussion-publications-list"><a data-publication-id="12">Publication &lt;img src=x&gt;</a></div><a id="pin" class="discussion-pin-link" href="/Rdiscussion/discussion1/messages/message1"><span class="discussion-pin-excerpt"></span></a>');
      document.querySelector('#pin .discussion-pin-excerpt').dataset.pinText = 'Réunion ici : ' + url;
    }, url);
    await initialize(page);
    assert.equal(await page.locator('#pin span').textContent(), 'Réunion ici : Publication <img src=x>');
    assert.equal(await page.locator('#pin img, #pin a').count(), 0);
    assert.equal(await page.locator('#pin').getAttribute('href'), '/Rdiscussion/discussion1/messages/message1');
    await page.evaluate(() => {
      document.querySelector('.discussion-publications-list a').textContent = 'Nouveau titre';
      document.dispatchEvent(new Event('discussion:publications-updated'));
    });
    assert.equal(await page.locator('#pin span').textContent(), 'Réunion ici : Nouveau titre');
    await page.evaluate(url => {
      const node = document.createElement('span');
      node.id = 'new-pin';
      node.className = 'discussion-pin-excerpt';
      node.dataset.pinText = '🚀'.repeat(165) + ' ' + url;
      document.getElementById('later').append(node);
      DiscussionEditor.enhance(document.getElementById('later'));
    }, url);
    assert.equal(await page.locator('#new-pin').textContent(), '🚀'.repeat(165) + ' Nouveau titre');
    await page.evaluate(() => {
      document.querySelector('#pin .discussion-pin-excerpt').dataset.pinText = '🚀'.repeat(181);
      DiscussionEditor.enhance(document.getElementById('discussion'));
    });
    assert.equal(await page.locator('#pin span').textContent(), '🚀'.repeat(180) + '…');
    await page.evaluate(url => {
      document.querySelector('#pin .discussion-pin-excerpt').dataset.pinText = url;
      document.querySelector('.discussion-publications-list').replaceChildren();
      document.dispatchEvent(new Event('discussion:publications-updated'));
    }, url);
    assert.equal(await page.locator('#pin span').textContent(), url);
  } finally {await browser.close();}
});
