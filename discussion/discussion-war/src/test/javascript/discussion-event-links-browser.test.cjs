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
    assert.equal(await link.getAttribute('target'),'_blank');
    assert.equal(await link.getAttribute('rel'),'noopener noreferrer');
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
