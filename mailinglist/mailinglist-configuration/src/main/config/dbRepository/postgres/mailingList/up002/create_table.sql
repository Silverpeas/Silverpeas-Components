alter table sc_mailinglist_internal_subscriber
        drop constraint fk_subscriber_mailinglist_id;

alter table sc_mailinglist_internal_subscriber
        drop constraint pk_mailinglist_internal_subscriber;

ALTER TABLE sc_mailinglist_internal_subscriber RENAME TO sc_mailinglist_internal_sub;