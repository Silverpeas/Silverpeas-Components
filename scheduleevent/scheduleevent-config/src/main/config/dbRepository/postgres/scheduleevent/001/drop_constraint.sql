alter table sc_scheduleevent_response
        drop constraint fk_response_optionid;
alter table sc_scheduleevent_response
        drop constraint fk_response_scheduleeventid;
alter table sc_scheduleevent_contributor
        drop constraint fk_contributor_scheduleeventid;
alter table sc_scheduleevent_options
        drop constraint fk_options_eventid;
        
alter table sc_scheduleevent_response
        drop constraint pk_scheduleevent_response;
alter table sc_scheduleevent_contributor
        drop constraint pk_scheduleevent_contrib;
alter table sc_scheduleevent_options
        drop constraint pk_scheduleevent_options;
alter table sc_scheduleevent_list
        drop constraint pk_scheduleevent_list;