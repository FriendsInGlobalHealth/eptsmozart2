package org.openmrs.module.eptsmozart2.etl;

import org.openmrs.module.eptsmozart2.Mozart2Properties;
import org.openmrs.module.eptsmozart2.ConnectionPool;
import org.openmrs.module.eptsmozart2.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @uthor Willa Mhawila<a.mhawila@gmail.com> on 6/15/22.
 */
public class IdentifierTableGenerator extends AbstractGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTableGenerator.class);
	
	private static final String CREATE_TABLE_FILE_NAME = "identifier.sql";
	
	@Override
	protected PreparedStatement prepareInsertStatement(ResultSet resultSet) throws SQLException {
		return prepareInsertStatement(resultSet, null);
	}
	
	@Override
	protected PreparedStatement prepareInsertStatement(ResultSet results, Integer batchSize) throws SQLException {
		if (batchSize == null)
			batchSize = Integer.MAX_VALUE;
		String insertSql = new StringBuilder("INSERT INTO ")
		        .append(Mozart2Properties.getInstance().getNewDatabaseName())
		        .append(
		            ".identifier (patient_id, patient_uuid, identifier_type, identifier_type_name, identifier_value, `primary`, ")
		        .append("identifier_uuid, source_database) VALUES (?, ?, ?, ?, ?, ?, ?, ?)").toString();
		try {
			if (insertStatement == null) {
				insertStatement = ConnectionPool.getConnection().prepareStatement(insertSql);
			} else {
				insertStatement.clearParameters();
			}
			int count = 0;
			while (results.next() && count < batchSize) {
				insertStatement.setInt(1, results.getInt("patient_id"));
				insertStatement.setString(2, results.getString("patient_uuid"));
				insertStatement.setInt(3, results.getInt("identifier_type"));
				insertStatement.setString(4, results.getString("name"));
				insertStatement.setString(5, results.getString("identifier"));
				insertStatement.setBoolean(6, results.getBoolean("primary"));
				insertStatement.setString(7, results.getString("identifier_uuid"));
				insertStatement.setString(8, Mozart2Properties.getInstance().getDatabaseName());
				
				insertStatement.addBatch();
				++count;
			}
			return insertStatement;
		}
		catch (SQLException e) {
			LOGGER.error("Error preparing insert statement for table {}", getTable());
			throw e;
		}
	}
	
	@Override
	public String getTable() {
		return "identifier";
	}
	
	@Override
	protected String getCreateTableSql() throws IOException {
		return Utils.readFileToString(CREATE_TABLE_FILE_NAME);
	}
	
	@Override
	protected String countQuery() {
		return new StringBuilder("SELECT COUNT(*) FROM ")
		        .append(Mozart2Properties.getInstance().getDatabaseName())
		        .append(".patient_identifier pi JOIN ")
		        .append(Mozart2Properties.getInstance().getNewDatabaseName())
		        .append(".patient p ON pi.patient_id = p.patient_id JOIN ")
		        .append(Mozart2Properties.getInstance().getDatabaseName())
		        .append(
		            ".patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id AND !pit.retired ")
		        .append("WHERE !pi.voided").toString();
	}
	
	@Override
	protected String fetchQuery(Integer start, Integer batchSize) {
		StringBuilder sb = new StringBuilder(
		        "SELECT pi.patient_id, p.patient_uuid, pi.identifier_type, pit.name, pi.identifier, ")
		        .append("pi.preferred as 'primary', pi.uuid as identifier_uuid FROM ")
		        .append(Mozart2Properties.getInstance().getDatabaseName())
		        .append(".patient_identifier pi JOIN ")
		        .append(Mozart2Properties.getInstance().getNewDatabaseName())
		        .append(".patient p ON pi.patient_id = p.patient_id JOIN ")
		        .append(Mozart2Properties.getInstance().getDatabaseName())
		        .append(
		            ".patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id AND !pit.retired ")
		        .append("WHERE !pi.voided ORDER BY pi.patient_identifier_id");
		
		if (start != null) {
			sb.append(" limit ?");
		}
		
		if (batchSize != null) {
			sb.append(", ?");
		}
		
		return sb.toString();
	}
}
