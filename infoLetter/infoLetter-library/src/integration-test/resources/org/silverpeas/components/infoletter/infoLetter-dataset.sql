INSERT INTO sc_il_letter (id, name, description, periode, instanceid)
    VALUES (1, 'Lettres d''information', '', '', 'infoLetter36');

INSERT INTO sc_il_letter (id, name, description, periode, instanceid)
    VALUES (2, 'Lettres d''information', '', '', 'infoLetter37');

INSERT INTO sc_il_publication (id, title, description, parutiondate, publicationstate, letterid,
                               instanceid)
    VALUES (1, 'Le Cambridge CXU', 'Le Cambridge CXU concurrence durement l''Oppo BDP 105D',
            '2016/02/03', 2, 1, 'infoLetter36');

INSERT INTO sc_il_publication (id, title, description, parutiondate, publicationstate, letterid,
                               instanceid)
    VALUES (2, 'L''Oppo BDP-105D', 'Un lecteur blue-ray haut de gamme et hyper-polyvalent. ',
        '2016/02/03', 2, 1, 'infoLetter36');

INSERT INTO sc_il_publication (id, title, description, parutiondate, publicationstate, letterid,
                               instanceid)
    VALUES (3, 'Le Cambridge CXU', 'Le Cambridge CXU concurrence durement l''Oppo BDP 105D',
        '2016/02/03', 2, 1, 'infoLetter37');

INSERT INTO sc_il_extsus(letter, email, instanceid)
    VALUES (1, 'miguel.moquillon@gmail.com', 'infoLetter36');

INSERT INTO sc_il_extsus(letter, email, instanceid)
    VALUES (1, 'miguel.moquillon@free.com', 'infoLetter36');

INSERT INTO sc_il_extsus(letter, email, instanceid)
    VALUES (2, 'miguel.moquillon@free.com', 'infoLetter36');