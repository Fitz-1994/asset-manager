package com.assetmanager.mapper;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@MappedTypes(Instant.class)
public class InstantTypeHandler extends BaseTypeHandler<Instant> {

    private static final DateTimeFormatter MYSQL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType) throws SQLException {
        // MySQL datetime format: YYYY-MM-DD HH:MM:SS
        ps.setString(i, parameter.atOffset(ZoneOffset.UTC).toLocalDateTime().toString());
    }

    @Override
    public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return parseInstant(s);
    }

    @Override
    public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return parseInstant(s);
    }

    @Override
    public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return parseInstant(s);
    }

    private Instant parseInstant(String s) {
        if (s == null) return null;
        try {
            // Try parsing as Instant first (ISO format with timezone)
            return Instant.parse(s);
        } catch (Exception e) {
            // Fall back to MySQL datetime format (no timezone)
            LocalDateTime ldt = LocalDateTime.parse(s, MYSQL_FORMAT);
            return ldt.atOffset(ZoneOffset.UTC).toInstant();
        }
    }
}
