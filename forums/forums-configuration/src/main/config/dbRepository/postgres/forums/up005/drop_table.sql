-- Copy message subscriptions of forum into new structure
INSERT INTO subscribe
(subscriberId, subscriberType, subscriptionMethod, resourceId, resourceType, space, instanceId, creatorId, creationDate)
  SELECT
    sfs.userid,
    'USER',
    'SELF_CREATION',
    sfs.messageid,
    'FORUM_MESSAGE',
    '-',
    sff.instanceid,
    sfs.userid,
    '1970-01-01 00:00:00.0'
  FROM sc_forums_subscription sfs
    JOIN sc_forums_message sfm
      ON sfm.messageid = cast(sfs.messageid AS NUMERIC)
    JOIN sc_forums_forum sff
      ON sff.forumid = sfm.forumid;

-- Drop unnecessary tables of forum component
DROP TABLE sc_forums_subscription;