/*
 Copyright  2002-2006 MySQL AB, 2008 Sun Microsystems
 All rights reserved. Use is subject to license terms.

  The MySQL Connector/J is licensed under the terms of the GPL,
  like most MySQL Connectors. There are special exceptions to the
  terms and conditions of the GPL as it is applied to this software,
  see the FLOSS License Exception available on mysql.com.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; version 2 of the
  License.

  This program is distributed in the hope that it will be useful,  
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA
 */

package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for result set data backed by a cursor. Only works for forward-only
 * result sets (but still works with updatable concurrency).
 * 
 * @version $Id: CursorRowProvider.java,v 1.1.2.1 2005/05/19 18:31:49 mmatthews
 *          Exp $
 */
public class RowDataCursor implements RowData {
	private static my.Debug DEBUG=new my.Debug(my.Debug.RowData);//我加上的

	private final static int BEFORE_START_OF_ROWS = -1;

	/**
	 * The cache of rows we have retrieved from the server.
	 */
	//只保持每次fetch回来的记录，上一次的记录在MysqlIO.fetchRowsViaCursor中清除
	private List fetchedRows;

	/**
	 * Where we are positionaly in the entire result set, used mostly to
	 * facilitate easy 'isBeforeFirst()' and 'isFirst()' methods.
	 */
	//这个是用来记录总的next次数
	private int currentPositionInEntireResult = BEFORE_START_OF_ROWS;

	/**
	 * Position in cache of rows, used to determine if we need to fetch more
	 * rows from the server to satisfy a request for the next row.
	 */
	//这个是用来记录每次fetch回来的记录的next次数，下一次fetch时自动从0开始
	private int currentPositionInFetchedRows = BEFORE_START_OF_ROWS;

	/**
	 * The result set that we 'belong' to.
	 */
	private ResultSetImpl owner;

	/**
	 * Have we been told from the server that we have seen the last row?
	 */
	private boolean lastRowFetched = false; //由SERVER_STATUS_LAST_ROW_SENT决定

	/**
	 * Field-level metadata from the server. We need this, because it is not
	 * sent for each batch of rows, but we need the metadata to unpack the
	 * results for each field.
	 */
	private Field[] metadata;

	/**
	 * Communications channel to the server
	 */
	private MysqlIO mysql;

	/**
	 * Identifier for the statement that created this cursor.
	 */
	private long statementIdOnServer; //发送COM_FETCH指令时要用到

	/**
	 * The prepared statement that created this cursor.
	 */
	private ServerPreparedStatement prepStmt;

	/**
	 * The server status for 'last-row-sent'...This might belong in mysqldefs,
	 * but it it only ever referenced from here.
	 */
	//这个值128必须跟mysql_com.h中定义的一样
	private static final int SERVER_STATUS_LAST_ROW_SENT = 128;

	/**
	 * Have we attempted to fetch any rows yet?
	 */
	private boolean firstFetchCompleted = false; //第一次fetch完之后就变为true

	private boolean wasEmpty = false; //第一次fetch之后如果没有记录就是true

	private boolean useBufferRowExplicit = false; //见MysqlIO.readSingleRowSet中的注释
	
	/**
	 * Creates a new cursor-backed row provider.
	 * 
	 * @param ioChannel
	 *            connection to the server.
	 * @param creatingStatement
	 *            statement that opened the cursor.
	 * @param metadata
	 *            field-level metadata for the results that this cursor covers.
	 */
	public RowDataCursor(MysqlIO ioChannel,
			ServerPreparedStatement creatingStatement, Field[] metadata) {
		try {//我加上的
		DEBUG.P(this,"RowDataCursor(3)");
		DEBUG.PA("metadata",metadata);

		this.currentPositionInEntireResult = BEFORE_START_OF_ROWS;
		this.metadata = metadata;
		this.mysql = ioChannel;
		this.statementIdOnServer = creatingStatement.getServerStatementId();
		this.prepStmt = creatingStatement;
		this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);

		DEBUG.P("statementIdOnServer="+statementIdOnServer);
		DEBUG.P("useBufferRowExplicit="+useBufferRowExplicit);

		}finally{//我加上的
		DEBUG.P(0,this,"RowDataCursor(3)");
		}
		
	}

	/**
	 * Returns true if we got the last element.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isAfterLast() {
		//必须在所有的行都取回来后(些时lastRowFetched为true)
		//才能判断是否在最后一行之后(currentPositionInFetchedRows > fetchedRows.size())
		//第一行是从1开始的，所以用的是>而不是>=
		return lastRowFetched
				&& this.currentPositionInFetchedRows > this.fetchedRows.size();
	}

	/**
	 * Only works on non dynamic result sets.
	 * 
	 * @param index
	 *            row number to get at
	 * @return row data at index
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ResultSetRow getAt(int ind) throws SQLException {
		notSupported();

		return null;
	}

	/**
	 * Returns if iteration has not occured yet.
	 * 
	 * @return true if before first row
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean isBeforeFirst() throws SQLException {
		return this.currentPositionInEntireResult < 0;
	}

	/**
	 * Moves the current position in the result set to the given row number.
	 * 
	 * @param rowNumber
	 *            row to move to
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void setCurrentRow(int rowNumber) throws SQLException {
		notSupported();
	}

	/**
	 * Returns the current position in the result set as a row number.
	 * 
	 * @return the current row number
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public int getCurrentRowNumber() throws SQLException {
		return this.currentPositionInEntireResult + 1;
	}

	/**
	 * Returns true if the result set is dynamic.
	 * 
	 * This means that move back and move forward won't work because we do not
	 * hold on to the records.
	 * 
	 * @return true if this result set is streaming from the server
	 */
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Has no records.
	 * 
	 * @return true if no records
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean isEmpty() throws SQLException {
		return this.isBeforeFirst() && this.isAfterLast();
	}

	/**
	 * Are we on the first row of the result set?
	 * 
	 * @return true if on first row
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean isFirst() throws SQLException {
		return this.currentPositionInEntireResult == 0;
	}

	/**
	 * Are we on the last row of the result set?
	 * 
	 * @return true if on last row
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean isLast() throws SQLException {
		return this.lastRowFetched
				&& this.currentPositionInFetchedRows == (this.fetchedRows
						.size() - 1);
	}

	/**
	 * Adds a row to this row data.
	 * 
	 * @param row
	 *            the row to add
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void addRow(ResultSetRow row) throws SQLException {
		notSupported();
	}

	/**
	 * Moves to after last.
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void afterLast() throws SQLException {
		notSupported();
	}

	/**
	 * Moves to before first.
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void beforeFirst() throws SQLException {
		notSupported();
	}

	/**
	 * Moves to before last so next el is the last el.
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void beforeLast() throws SQLException {
		notSupported();
	}

	/**
	 * We're done.
	 * 
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void close() throws SQLException {

		this.metadata = null;
		this.owner = null;
	}

	/**
	 * Returns true if another row exists.
	 * 
	 * @return true if more rows
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public boolean hasNext() throws SQLException {

		if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
			return false;
		}

		if (this.owner != null && this.owner.owningStatement != null) {
			int maxRows = this.owner.owningStatement.maxRows;
			
			if (maxRows != -1 && this.currentPositionInEntireResult + 1 > maxRows) {
				return false;
			}	
		}
		
		if (this.currentPositionInEntireResult != BEFORE_START_OF_ROWS) {
			// Case, we've fetched some rows, but are not at end of fetched
			// block
			if (this.currentPositionInFetchedRows < (this.fetchedRows.size() - 1)) {
				return true;
			} else if (this.currentPositionInFetchedRows == this.fetchedRows
					.size()
					&& this.lastRowFetched) {
				return false;
			} else {
				// need to fetch to determine
				fetchMoreRows();

				return (this.fetchedRows.size() > 0);
			}
		}

		// Okay, no rows _yet_, so fetch 'em

		fetchMoreRows();

		return this.fetchedRows.size() > 0;
	}

	/**
	 * Moves the current position relative 'rows' from the current position.
	 * 
	 * @param rows
	 *            the relative number of rows to move
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void moveRowRelative(int rows) throws SQLException {
		notSupported();
	}

	/**
	 * Returns the next row.
	 * 
	 * @return the next row value
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public ResultSetRow next() throws SQLException {
		try {//我加上的
		DEBUG.P(this,"next()");
		DEBUG.P("fetchedRows="+fetchedRows);
		DEBUG.P("currentPositionInEntireResult="+currentPositionInEntireResult);

		if (this.fetchedRows == null && this.currentPositionInEntireResult != BEFORE_START_OF_ROWS) {
			throw SQLError.createSQLException(
					Messages
							.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), //$NON-NLS-1$
					SQLError.SQL_STATE_GENERAL_ERROR, mysql.getExceptionInterceptor());
		}
		
		if (!hasNext()) {
			return null;
		}
		
		//这两个字段的最初值都是-1
		this.currentPositionInEntireResult++;
		this.currentPositionInFetchedRows++;

		// Catch the forced scroll-passed-end
		if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
			return null;
		}

		if (this.currentPositionInFetchedRows > (this.fetchedRows.size() - 1)) {
			fetchMoreRows();
			this.currentPositionInFetchedRows = 0;
		}

		ResultSetRow row = (ResultSetRow) this.fetchedRows
				.get(this.currentPositionInFetchedRows);

		row.setMetadata(this.metadata);
		
		return row;

		}finally{//我加上的
		DEBUG.P(0,this,"next()");
		}
	}

	/**
	 * 
	 */
	private void fetchMoreRows() throws SQLException {
		try {//我加上的
		DEBUG.P(this,"fetchMoreRows()");
		DEBUG.P("lastRowFetched="+lastRowFetched);
		DEBUG.P("firstFetchCompleted="+firstFetchCompleted);

		if (this.lastRowFetched) {
			this.fetchedRows = new ArrayList(0);
			return;
		}

		synchronized (this.owner.connection.getMutex()) {
			boolean oldFirstFetchCompleted = this.firstFetchCompleted;
			
			if (!this.firstFetchCompleted) {
				this.firstFetchCompleted = true;
			}

			//优先使用ResultSet设置过的FetchSize
			//(在MysqlIO===>getResultSet(...)中默认是取prepStmt.getFetchSize()的值)
			int numRowsToFetch = this.owner.getFetchSize();

			if (numRowsToFetch == 0) {
				numRowsToFetch = this.prepStmt.getFetchSize();
			}
			
			if (numRowsToFetch == Integer.MIN_VALUE) {
				// Handle the case where the user used 'old'
				// streaming result sets

				numRowsToFetch = 1;
			}

			this.fetchedRows = this.mysql.fetchRowsViaCursor(this.fetchedRows,
					this.statementIdOnServer, this.metadata, numRowsToFetch, 
					this.useBufferRowExplicit);
			this.currentPositionInFetchedRows = BEFORE_START_OF_ROWS;

			if ((this.mysql.getServerStatus() & SERVER_STATUS_LAST_ROW_SENT) != 0) {
				this.lastRowFetched = true;
				
				//第一次fetch时就没有得到记录，些时就认为是空的(wasEmpty=true)
				if (!oldFirstFetchCompleted && this.fetchedRows.size() == 0) {
					this.wasEmpty  = true;
				}
			}
		}

		}finally{//我加上的
		DEBUG.P(0,this,"fetchMoreRows()");
		}
	}

	/**
	 * Removes the row at the given index.
	 * 
	 * @param index
	 *            the row to move to
	 * @throws SQLException
	 *             if a database error occurs
	 */
	public void removeRow(int ind) throws SQLException {
		notSupported();
	}

	/**
	 * Only works on non dynamic result sets.
	 * 
	 * @return the size of this row data
	 */
	public int size() {
		return RESULT_SET_SIZE_UNKNOWN;
	}

	private void nextRecord() throws SQLException {

	}

	private void notSupported() throws SQLException {
		throw new OperationNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mysql.jdbc.RowProvider#setOwner(com.mysql.jdbc.ResultSet)
	 */
	public void setOwner(ResultSetImpl rs) {
		this.owner = rs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mysql.jdbc.RowProvider#getOwner()
	 */
	public ResultSetInternalMethods getOwner() {
		return this.owner;
	}

	public boolean wasEmpty() {
		return this.wasEmpty;
	}
	
	public void setMetadata(Field[] metadata) {
		this.metadata = metadata;
	}

}
