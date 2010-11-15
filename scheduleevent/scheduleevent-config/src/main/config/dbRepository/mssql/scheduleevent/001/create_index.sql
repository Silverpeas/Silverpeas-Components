CREATE INDEX ind_sc_scheduleevent_contributor_1
   ON sc_scheduleevent_contributor (scheduleeventid);
   
CREATE INDEX ind_sc_scheduleevent_contributor_2
   ON sc_scheduleevent_contributor (userid);
      
CREATE INDEX ind_sc_scheduleevent_response_1
   ON sc_scheduleevent_response (scheduleeventid, userid);