package com.hydrologis.gss.server.database.ormlite;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Ormlite type for H2GIS point spatial objects.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PointTypeH2GIS extends BaseDataType {
    private static final PointTypeH2GIS singleTon = new PointTypeH2GIS();

    public static PointTypeH2GIS getSingleton() {
        return singleTon;
    }

    private PointTypeH2GIS() {
        super(SqlType.OTHER);
    }

    public String getSqlOtherType() {
        return "POINT";
    }

    /**
     * Here for others to subclass.
     */
    protected PointTypeH2GIS( SqlType sqlType, Class< ? >[] classes ) {
        super(sqlType, classes);
    }

    @Override
    public Object parseDefaultString( FieldType fieldType, String defaultStr ) throws SQLException {
        throw new SQLException("Default values for point types are not supported");
    }

    @Override
    public Object resultToSqlArg( FieldType fieldType, DatabaseResults results, int columnPos ) throws SQLException {
        return results.getObject(columnPos);
    }

    @Override
    public Object javaToSqlArg( FieldType fieldType, Object obj ) throws SQLException {
        return obj;
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isAppropriateId() {
        return false;
    }
    
    @Override
    public boolean isValidForField( Field field ) {
        return true;
    }
}