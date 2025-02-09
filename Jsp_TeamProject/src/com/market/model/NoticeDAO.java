package com.market.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class NoticeDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	String sql = null;

	private static NoticeDAO instance;

	private NoticeDAO() {}

	public static NoticeDAO getInstance() {

		if (instance == null) {
			instance = new NoticeDAO();
		}

		return instance;
	}

	// DB 연동
	public void openConn() {

		try {
			Context ctx = new InitialContext();

			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/oracle");

			con = ds.getConnection();

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // openConn() - End

	// 공지사항 목록 조회
	public List<NoticeDTO> getNoticeList(int page, int rowsize) {
		List<NoticeDTO> list = new ArrayList<NoticeDTO>();
		
		// 해당 페이지 시작 번호
		int startNo = (page * rowsize) - (rowsize - 1);

		// 해당 페이지 끝 번호
		int endNo = (page * rowsize);
		
		try {
			openConn();
			
			sql = "SELECT * FROM"
					+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B)"
					+ " WHERE RNUM >= ? AND RNUM <= ?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, startNo);
			pstmt.setInt(2, endNo);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				NoticeDTO dto = new NoticeDTO();
				
				dto.setNotice_no(rs.getInt("NOTICE_NO"));
				dto.setNotice_userId(rs.getString("NOTICE_USERID"));
				dto.setNotice_writer(rs.getString("NOTICE_WRITER"));
				dto.setNotice_title(rs.getString("NOTICE_TITLE"));
				dto.setNotice_content(rs.getString("NOTICE_CONTENT"));
				dto.setNotice_pwd(rs.getString("NOTICE_PWD"));
				dto.setNotice_hit(rs.getInt("NOTICE_HIT"));
				dto.setNotice_date(rs.getString("NOTICE_DATE"));
				dto.setNotice_update(rs.getString("NOTICE_UPDATE"));
				
				list.add(dto);
			}
			
			rs.close(); pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	} // getNoticeList() - End

	// 해당 제목의 공지내용 조회
	public NoticeDTO getNoticeDetail(int notice_no) {
		NoticeDTO list = new NoticeDTO();
		
		try {
			openConn();
			
			sql = "SELECT * FROM NOTICE_MARKET WHERE NOTICE_NO = ?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, notice_no);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				list.setNotice_no(rs.getInt("NOTICE_NO"));
				list.setNotice_userId(rs.getString("NOTICE_USERID"));
				list.setNotice_writer(rs.getString("NOTICE_WRITER"));
				list.setNotice_title(rs.getString("NOTICE_TITLE"));
				list.setNotice_content(rs.getString("NOTICE_CONTENT"));
				list.setNotice_pwd(rs.getString("NOTICE_PWD"));
				list.setNotice_hit(rs.getInt("NOTICE_HIT"));
				list.setNotice_date(rs.getString("NOTICE_DATE"));
				list.setNotice_update(rs.getString("NOTICE_UPDATE"));
			}
			
			rs.close(); pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	} // getNoticeDetail() - End

	// 조회수 증가
	public void updateHit(int notice_no) {
		
		try {
			openConn();
			
			sql = "UPDATE NOTICE_MARKET SET NOTICE_HIT = NOTICE_HIT + 1 WHERE NOTICE_NO = ?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, notice_no);
			
			pstmt.executeUpdate();
			
			pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
	} // updateHit() - End

	// 총 게시물 수
	public int getBoardCount() {
		int count = 0;
		
		try {
			openConn();
			
			sql = "SELECT COUNT(*) FROM NOTICE_MARKET";
			
			pstmt = con.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				count = rs.getInt(1);
			}
			
			rs.close(); pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	} // getBoardCount() - End

	// 검색 레코드 갯수
	public int searchListCount(String[] search_field, String search_keyword) {
		int count = 0;
		
		try {
			openConn();
			
			if(search_field.length == 1) { // 검색 항목 1개
				if(search_field[0].equals("writer")) {
					sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_WRITER LIKE ?"; 
				} else if(search_field[0].equals("title")) {
					sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_TITLE LIKE ?"; 
				} else {
					sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_CONTENT LIKE ?"; 
				} 
			} else if (search_field.length == 2) { // 검색 항목 2개
				if(search_field[0].equals("writer")) {
					if(search_field[1].equals("title")) { // 이름 + 제목
						sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_WRITER || NOTICE_TITLE LIKE ?"; 
					} else {	// 이름 + 내용
						sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_WRITER || NOTICE_CONTENT LIKE ?"; 
					}
				} else { // 제목 + 내용
					sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_TITLE || NOTICE_CONTENT LIKE ?";
				}
			} else if (search_field.length == 3){ // 검색 항목을 모두 체크
				sql = "SELECT COUNT(*) FROM NOTICE_MARKET WHERE NOTICE_WRITER || NOTICE_TITLE || NOTICE_CONTENT LIKE ?";
			}
			
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, "%" + search_keyword + "%");
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				count = rs.getInt(1);
			}
			
			rs.close(); pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}
	
	// 공지사항 검색 기능
	public List<NoticeDTO> searchNotice(String[] search_field, String search_keyword, int page, int rowsize) {
		List<NoticeDTO> list = new ArrayList<NoticeDTO>();
		
		// 해당 페이지에서 시작 번호
		int startNo = (page * rowsize) - (rowsize - 1);

		// 해당 페이지에서 끝 번호
		int endNo = (page * rowsize);
		
		try {
			openConn();
			
			if(search_field.length == 1) { // 검색 항목 1개
				if(search_field[0].equals("writer")) {
					sql = "SELECT * FROM"
							+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
							+ " WHERE NOTICE_WRITER LIKE ?)"
							+ " WHERE RNUM >= ? AND RNUM <= ?"; 
				} else if(search_field[0].equals("title")) {
					sql = "SELECT * FROM"
							+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
							+ " WHERE NOTICE_TITLE LIKE ?)"
							+ " WHERE RNUM >= ? AND RNUM <= ?";
				} else {
					sql = "SELECT * FROM"
							+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
							+ " WHERE NOTICE_CONTENT LIKE ?)"
							+ " WHERE RNUM >= ? AND RNUM <= ?"; 
				} 
			} else if (search_field.length == 2) { // 검색 항목 2개
				if(search_field[0].equals("writer")) {
					if(search_field[1].equals("title")) { // 이름 + 제목
						sql = "SELECT * FROM"
								+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
								+ " WHERE NOTICE_WRITER || NOTICE_TITLE LIKE ?)"
								+ " WHERE RNUM >= ? AND RNUM <= ?"; 
					} else {	// 이름 + 내용
						sql = "SELECT * FROM"
								+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
								+ " WHERE NOTICE_WRITER || NOTICE_CONTENT LIKE ?)"
								+ " WHERE RNUM >= ? AND RNUM <= ?"; 
					}
				} else { // 제목 + 내용
					sql = "SELECT * FROM"
							+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
							+ " WHERE NOTICE_TITLE || NOTICE_CONTENT LIKE ?)"
							+ " WHERE RNUM >= ? AND RNUM <= ?"; 
				}
			} else if (search_field.length == 3){ // 검색 항목을 모두 체크
				sql = "SELECT * FROM"
						+ " (SELECT ROW_NUMBER() OVER(ORDER BY NOTICE_NO DESC) RNUM, B.* FROM NOTICE_MARKET B"
						+ " WHERE NOTICE_WRITER || NOTICE_TITLE || NOTICE_CONTENT LIKE ?)"
						+ " WHERE RNUM >= ? AND RNUM <= ?"; 
			}
			
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, "%" + search_keyword + "%");
			pstmt.setInt(2, startNo);
			pstmt.setInt(3, endNo);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				NoticeDTO dto = new NoticeDTO();
				
				dto.setNotice_no(rs.getInt("NOTICE_NO"));
				dto.setNotice_userId(rs.getString("NOTICE_USERID"));
				dto.setNotice_writer(rs.getString("NOTICE_WRITER"));
				dto.setNotice_title(rs.getString("NOTICE_TITLE"));
				dto.setNotice_content(rs.getString("NOTICE_CONTENT"));
				dto.setNotice_pwd(rs.getString("NOTICE_PWD"));
				dto.setNotice_hit(rs.getInt("NOTICE_HIT"));
				dto.setNotice_date(rs.getString("NOTICE_DATE"));
				dto.setNotice_update(rs.getString("NOTICE_UPDATE"));
				
				list.add(dto);
			}
			
			rs.close(); pstmt.close(); con.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	} // searchNotice() - End
}













