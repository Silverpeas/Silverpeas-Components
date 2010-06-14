CREATE INDEX IND_SC_FormsOnline_UserRights_1
   ON SC_FormsOnline_UserRights (formId, instanceId, rightType);
   
CREATE INDEX IND_SC_FormsOnline_UserRights_2
   ON SC_FormsOnline_UserRights (rightType, userId);
   
CREATE INDEX IND_SC_FormsOnline_GroupRights_1
   ON SC_FormsOnline_GroupRights (formId, instanceId, rightType);
   
CREATE INDEX IND_SC_FormsOnline_GroupRights_2
   ON SC_FormsOnline_GroupRights (rightType, groupId);
   
   
