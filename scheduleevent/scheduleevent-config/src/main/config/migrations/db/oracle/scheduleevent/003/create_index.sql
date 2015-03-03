CREATE INDEX ind_sc_scheduleevent_contrib1
ON sc_scheduleevent_contributor (scheduleeventid);

CREATE INDEX ind_sc_scheduleevent_contrib2
ON sc_scheduleevent_contributor (userid);

CREATE INDEX ind_sc_scheduleevent_resp1
ON sc_scheduleevent_response (scheduleeventid, userid);