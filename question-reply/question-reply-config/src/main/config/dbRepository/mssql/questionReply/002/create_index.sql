CREATE INDEX QuestionReply_InstanceId
ON SC_QuestionReply_Question (instanceId);

CREATE INDEX QuestionReply_UserId
ON SC_QuestionReply_Recipient (userId);
