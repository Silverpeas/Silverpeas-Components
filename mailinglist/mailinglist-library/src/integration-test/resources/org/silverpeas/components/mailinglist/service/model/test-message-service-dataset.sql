INSERT INTO sc_mailinglist_message (id, version, title, attachmentsSize,
                                    summary,
                                    sender, sentdate, mailid, referenceid, moderated, componentid,
                                    messageyear, messagemonth, contenttype,
                                    body)
VALUES (1, 1, 'Simple database message', 10000,
  'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans',
  'bart.simpson@silverpeas.com', '2008-03-01 10:34:15.0000', '0000001747b40c8d', NULL, 1, 'componentId', 2008, 2, 'text/plain',
  'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans la prochaine émission.Bart');

INSERT INTO sc_mailinglist_message (id, version, title, attachmentsSize,
                                    summary,
                                    sender, sentdate, mailid, referenceid, moderated, componentid,
                                    messageyear, messagemonth, contenttype,
                                    body)
VALUES (2, 1, 'Simple database message 2', 0,
'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans',
'bart.simpson@silverpeas.com', '2008-02-01 10:34:15.0000', '0000001747b40c83', NULL, 1, 'componentId', 2008, 1, 'text/plain',
'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans la prochaine émission.Bart');

INSERT INTO sc_mailinglist_message (id, version, title, attachmentsSize,
                                    summary,
                                    sender, sentdate, mailid, referenceid, moderated, componentid,
                                    messageyear, messagemonth, contenttype,
                                    body)
VALUES (3, 1, 'Simple database message 3', 0,
'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans',
'bart.simpson@silverpeas.com', '2008-03-02 10:34:15.0000', '0000001747b40c95', NULL, 0, 'componentId', 2008, 2, 'text/plain',
'Bonjour famille Simpson, j''espère que vous allez bien. Ici tout se passe bien et Krusty est très sympathique. Surtout depuis que Tahiti Bob est retourné en prison. Je dois remplacer l''homme canon dans la prochaine émission.Bart');

INSERT INTO sc_mailinglist_attachment (id, version, filename, attachmentsize, messageid, contenttype, attachmentpath)
VALUES (1, 1, 'lemonde.html', 10000, 1, 'text/html', 'c:\tmp\uploads\componentId\mailId@silverpeas.com\lemonde.html');
