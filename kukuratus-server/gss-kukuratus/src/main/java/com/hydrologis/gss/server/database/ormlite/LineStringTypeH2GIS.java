/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database.ormlite;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Ormlite type for H2GIS linear spatial objects.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LineStringTypeH2GIS extends BaseDataType {
    private static final LineStringTypeH2GIS singleTon = new LineStringTypeH2GIS();

    public static LineStringTypeH2GIS getSingleton() {
        return singleTon;
    }

    private LineStringTypeH2GIS() {
        super(SqlType.OTHER);
    }

    public String getSqlOtherType() {
        return "LINESTRING";
    }

    /**
     * Here for others to subclass.
     */
    protected LineStringTypeH2GIS( SqlType sqlType, Class< ? >[] classes ) {
        super(sqlType, classes);
    }

    @Override
    public Object parseDefaultString( FieldType fieldType, String defaultStr ) throws SQLException {
        throw new SQLException("Default values for point types are not supported");
    }

    @Override
    public Object resultToSqlArg( FieldType fieldType, DatabaseResults results, int columnPos ) throws SQLException {
        Object object = results.getObject(columnPos);
        return object;
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
