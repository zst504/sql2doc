package com.zst.dbtools;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

public class JdbcUtil {
	private static final Logger log = Logger.getLogger(JdbcUtil.class);

	public static void rollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * ִ��sql���
	 *
	 * @param conn
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 */
	public static int execSql(Connection conn, String sql) throws SQLException {
		if (sql == null || sql.equals("")) return 0;
		Statement stmt = conn.createStatement();
		int i = stmt.executeUpdate(sql);
		closeStmt(stmt);
		return i;
	}

	/**
	 * ִ��sql���
	 *
	 * @param conn
	 * @param sql
	 * @return
	 * @throws java.sql.SQLException
	 */
	public static boolean execSql(Connection conn, String sql, List params) throws SQLException {
		if (sql == null || sql.equals("")) return false;
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for (int i = 0; i < params.size(); i++) {
			pstmt.setObject(i + 1, params.get(i));
		}
		boolean i = pstmt.execute();
		closeStmt(pstmt);
		return i;
	}


	/**
	 * �ر�����
	 *
	 * @param con
	 */
	public static void closeConn(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.error("�ر�Connection����");
			}
		}
	}

	public static void closeResource(ResultSet rs, Statement stmt) {
		closeRs(rs);
		closeStmt(stmt);
	}

	public static void closeStmt(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error("�ر�Statement����");
			}
		}
	}

	public static void closeRs(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error("�ر�ResultSet����");
			}
		}
	}


	/**
	 * ִ�д�ռλ����SQL��ѯ,���ز�ѯ����ļ�¼����
	 *
	 * @param sql  SQL
	 * @param args ��ռλ����Ӧ�Ĳ���
	 * @return ��¼����
	 */
	public static Integer getDataCountBySQL(Connection conn, String sql, Object[] args) {
		StringBuffer sqlCount = new StringBuffer(sql.length() + 300);
		sqlCount.append("select count(*) from ( ");
		int indexOf = sql.toLowerCase().lastIndexOf(" order by");
		if (indexOf > -1) {
			sqlCount.append(sql.substring(0, indexOf));
		} else {
			sqlCount.append(sql);
		}
		sqlCount.append(" )");
		log.debug(sqlCount.toString());
		Integer count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sqlCount.toString(),
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					pstmt.setObject(i + 1, args[i]);
				}
			}
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResource(rs, pstmt);
		}
		return count;
	}

	/**
	 * ��ѯ����
	 *
	 * @param conn		 ���ݿ�����
	 * @param sql		 ��ѯSQL
	 * @param args		��ѯ����
	 * @param firstResult
	 * @param maxResults
	 * @return List[]{0-�ֶ��б�,1-�����б�}
	 */
	public static List[] getDataListBySQL(Connection conn, String sql, Object[] args, Integer firstResult, Integer maxResults) {
		List<Object> values = new ArrayList<Object>();
		if (args != null) {
			for (Object arg : args) {
				values.add(arg);
			}
		}
		StringBuffer pagingSelect = new StringBuffer(sql.length() + 300);
		if (firstResult > 0) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		} else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		if (firstResult > 0) {
			pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
			values.add(maxResults);
			values.add(firstResult);
		} else {
			pagingSelect.append(" ) where rownum <= ?");
			values.add(maxResults);
		}
		log.debug(pagingSelect.toString());
		List colList = new ArrayList();
		List mapList = new ArrayList();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(pagingSelect.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (values != null) {
				for (int i = 0; i < values.size(); i++) {
					pstmt.setObject(i + 1, values.get(i));
				}
			}
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				colList.add(rsmd.getColumnName(i + 1).toLowerCase());
			}
			while (rs.next()) {
				Map map = new ListOrderedMap();
				for (int i = 0; i < columnCount; i++) {
					map.put(rsmd.getColumnName(i + 1).toLowerCase(), rs.getObject(i + 1));
				}
				mapList.add(map);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResource(rs, pstmt);
		}
		return new List[]{colList, mapList};
	}
	
	/**
	 * ��ѯ����
	 *
	 * @param conn		 ���ݿ�����
	 * @param sql		 ��ѯSQL
	 * @param args		��ѯ����
	 * @return List[]{0-�ֶ��б�,1-�����б�}
	 */
	public static List[] getDataListBySQL(Connection conn, String sql, Object[] args) {
		log.debug(sql);
		List colList = new ArrayList();
		List mapList = new ArrayList();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					pstmt.setObject(i + 1, args[i]);
				}
			}
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				colList.add(rsmd.getColumnName(i + 1).toLowerCase());
			}
			while (rs.next()) {
				Map map = new ListOrderedMap();
				for (int i = 0; i < columnCount; i++) {
					map.put(rsmd.getColumnName(i + 1).toLowerCase(), rs.getObject(i + 1));
				}
				mapList.add(map);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResource(rs, pstmt);
		}
		return new List[]{colList, mapList};
	}
	
	/**
	 * ִ�д�ռλ����SQL��ѯ-245
	 * ��ѯ���ΪList<String[]>
	 *
	 * @param sql  SQL
	 * @param args ��ռλ����Ӧ�Ĳ���
	 * @return ��ѯ���
	 */
	@SuppressWarnings("unchecked")
	public List<String[]> getStringArrayListBySQL(Connection conn, String sql, Object[] args) {
		List arrayList = new ArrayList();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					pstmt.setObject(i + 1, args[i]);
				}
			}
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				String[] result = new String[columnCount];
				for (int i = 0; i < columnCount; i++) {
					result[i] = rs.getString(i + 1);
				}
				arrayList.add(result);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			JdbcUtil.closeResource(rs, pstmt);
		}
		return arrayList;
	}

}
