-- Bug #15422: the publications out of their visibility period weren't sent to the basket when
-- their topic was deleted (nor moved with their topic into another application). They are then
-- still located in a topic that doesn't exist anymore. Such publications are now sent to the
-- basket as it should have been done: they aren't anymore visible in the PdC, their location is
-- moved to the basket and their orphan aliases are removed.

UPDATE SB_ContentManager_Content SET isVisible = 0
WHERE EXISTS (SELECT 1
              FROM SB_Publication_PubliFather f, SB_ContentManager_Instance i
              WHERE (f.instanceId LIKE 'kmelia%' OR f.instanceId LIKE 'toolbox%')
                AND f.aliasUserId IS NULL
                AND NOT EXISTS (SELECT 1 FROM SB_Node_Node n
                                WHERE n.nodeId = f.nodeId AND n.instanceId = f.instanceId)
                AND EXISTS (SELECT 1 FROM SB_Node_Node b
                            WHERE b.nodeId = 1 AND b.instanceId = f.instanceId)
                AND i.componentId = f.instanceId
                AND SB_ContentManager_Content.contentInstanceId = i.instanceId
                AND SB_ContentManager_Content.internalContentId = CAST(f.pubId AS VARCHAR(100)));

UPDATE SB_Publication_PubliFather SET nodeId = 1, pubOrder = 0
WHERE (instanceId LIKE 'kmelia%' OR instanceId LIKE 'toolbox%')
  AND aliasUserId IS NULL
  AND NOT EXISTS (SELECT 1 FROM SB_Node_Node n
                  WHERE n.nodeId = SB_Publication_PubliFather.nodeId
                    AND n.instanceId = SB_Publication_PubliFather.instanceId)
  AND EXISTS (SELECT 1 FROM SB_Node_Node b
              WHERE b.nodeId = 1 AND b.instanceId = SB_Publication_PubliFather.instanceId)
  AND NOT EXISTS (SELECT 1 FROM SB_Publication_PubliFather p
                  WHERE p.pubId = SB_Publication_PubliFather.pubId
                    AND p.nodeId = 1
                    AND p.instanceId = SB_Publication_PubliFather.instanceId);

-- the remaining orphan locations are either aliases or locations of publications already in the
-- basket
DELETE FROM SB_Publication_PubliFather
WHERE (instanceId LIKE 'kmelia%' OR instanceId LIKE 'toolbox%')
  AND NOT EXISTS (SELECT 1 FROM SB_Node_Node n
                  WHERE n.nodeId = SB_Publication_PubliFather.nodeId
                    AND n.instanceId = SB_Publication_PubliFather.instanceId)
  AND EXISTS (SELECT 1 FROM SB_Node_Node b
              WHERE b.nodeId = 1 AND b.instanceId = SB_Publication_PubliFather.instanceId);
