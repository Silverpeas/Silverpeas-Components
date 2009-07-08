package com.stratelia.webactiv.yellowpages.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class GroupDAO
{
	public static Collection getGroupIds(Connection con, String fatherId, String instanceId) throws SQLException
	{
		ArrayList groupIds = new ArrayList();

		String 				query 		= "select groupId from SC_Contact_GroupFather where fatherId = ? and instanceId = ? ";
		PreparedStatement 	prepStmt 	= null;
		ResultSet 			rs 			= null;
		String 				groupId 	= null;
		try
		{
			prepStmt = con.prepareStatement(query);
			prepStmt.setInt(1, Integer.parseInt(fatherId));
			prepStmt.setString(2, instanceId);
			rs = prepStmt.executeQuery();
			while (rs.next())
			{
				groupId = rs.getString(1);
				groupIds.add(groupId);
			}
		}
		finally
		{
			// fermeture
			DBUtil.close(rs, prepStmt);
		}
		return groupIds;
	}
	
	public static void addGroup(Connection con, String groupId, String fatherId, String instanceId) throws SQLException, UtilException
	{
		PreparedStatement prepStmt = null;
		try
		{
			String query = "insert into SC_Contact_GroupFather values (?,?,?)";
			prepStmt = con.prepareStatement(query);
			prepStmt.setInt(1, Integer.parseInt(groupId));
			prepStmt.setInt(2, Integer.parseInt(fatherId));
			prepStmt.setString(3, instanceId);
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	}

	public static void removeGroup(Connection con, String groupId) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			String query = "delete from SC_Contact_GroupFather where groupId = ? ";
			prepStmt = con.prepareStatement(query);
			prepStmt.setInt(1, Integer.parseInt(groupId));
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	}
	
	public static void removeGroup(Connection con, String groupId, String fatherId, String instanceId) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			String query = "delete from SC_Contact_GroupFather where groupId = ? and fatherId = ? and instanceId = ? ";
			prepStmt = con.prepareStatement(query);
			prepStmt.setInt(1, Integer.parseInt(groupId));
			prepStmt.setInt(2, Integer.parseInt(fatherId));
			prepStmt.setString(3, instanceId);
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	}
}
