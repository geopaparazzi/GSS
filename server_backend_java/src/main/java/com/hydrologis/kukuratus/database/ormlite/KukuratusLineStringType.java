/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.database.ormlite;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.postgis.PostgisGeometryParser;
import org.postgis.LineString;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;
import org.locationtech.jts.geom.Geometry;

/**
 * Ormlite type for linear spatial objects.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class KukuratusLineStringType extends BaseDataType {
    private static final KukuratusLineStringType singleTon = new KukuratusLineStringType();
    private static EDb dbType;
    private static PostgisGeometryParser pg = new PostgisGeometryParser();

    public static KukuratusLineStringType getSingleton() {
        return singleTon;
    }

    private KukuratusLineStringType() {
        super(SqlType.OTHER, getGeomClass());
    }

    private static Class< ? >[] getGeomClass() {
        try {
            dbType = DatabaseHandler.instance().getDb().getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch( dbType ) {
        case POSTGIS:
            return new Class< ? >[]{LineString.class};
        case H2GIS:
        default:
            return new Class< ? >[]{org.locationtech.jts.geom.LineString.class};
        }
    }

    public String getSqlOtherType() {
        switch( dbType ) {
        case POSTGIS:
            return "lseg"; //$NON-NLS-1$
        case H2GIS:
        default:
            return "LINESTRING"; //$NON-NLS-1$
        }
    }

    @Override
    public Object parseDefaultString( FieldType fieldType, String defaultStr ) throws SQLException {
        throw new SQLException("Default values for line types are not supported"); //$NON-NLS-1$
    }

    @Override
    public Object resultToSqlArg( FieldType fieldType, DatabaseResults results, int columnPos ) throws SQLException {
        switch( dbType ) {
        case POSTGIS:
            Object object = results.getObject(columnPos);
            return object;
        case H2GIS:
        default:
            Object object1 = results.getObject(columnPos);
            return object1;
        }
    }

    @Override
    public Object javaToSqlArg( FieldType fieldType, Object obj ) throws SQLException {
        switch( dbType ) {
        case POSTGIS:
            try {
                if (obj instanceof Geometry) {
                    Object geom = pg.toSqlObject((Geometry) obj);
                    return geom;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        case H2GIS:
        default:
            return obj;
        }
    }

    @Override
    public Object sqlArgToJava( FieldType fieldType, Object sqlArg, int columnPos ) throws SQLException {
        switch( dbType ) {
        case POSTGIS:
            try {
                Geometry geom = pg.fromSqlObject(sqlArg);
                return geom;
            } catch (Exception e) {
                e.printStackTrace();
            }
        case H2GIS:
        default:
            if (sqlArg instanceof Geometry) {
                return sqlArg;
            }
        }
        return null;
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
