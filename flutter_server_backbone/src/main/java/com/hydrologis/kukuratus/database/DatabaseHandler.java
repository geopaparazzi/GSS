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
package com.hydrologis.kukuratus.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.IDbVisitor;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.datatypes.EGeometryType;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.postgis.PostgisDb;
import org.hortonmachine.dbs.spatialite.hm.SpatialiteDb;

import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcSingleConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHandler implements AutoCloseable, IDbVisitor {

    public static final String TABLES_EPSG = "4326"; //$NON-NLS-1$
    protected ConnectionSource connectionSource;
    protected ASpatialDb db;
    protected HashMap<Class< ? >, Dao< ? , ? >> daosMap = new HashMap<>();

    private static DatabaseHandler dbHandler;

    public static DatabaseHandler init( ASpatialDb db ) throws Exception {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(db);
            db.accept(dbHandler);
        }
        return dbHandler;
    }

    public static DatabaseHandler instance() throws Exception {
        if (dbHandler == null) {
            throw new IllegalArgumentException("The DatabaseHandler was not initialized, did you call init()?");
        }
        return dbHandler;
    }

    protected DatabaseHandler( ASpatialDb db ) throws SQLException {
        this.db = db;
    }

    public ASpatialDb getDb() {
        return db;
    }

    @Override
    public void visit( DataSource dataSource ) throws Exception {
        if (dataSource != null) {
            String url = db.getJdbcUrlPre() + db.getDatabasePath();
            connectionSource = new DataSourceConnectionSource(dataSource, url);
        }
    }

    @Override
    public void visit( Connection singleConnection ) throws Exception {
        if (singleConnection != null) {
            String url = db.getJdbcUrlPre() + db.getDatabasePath();
            connectionSource = new JdbcSingleConnectionSource(url, singleConnection);
            ((JdbcSingleConnectionSource) connectionSource).initialize();
        }
    }

    public void populateWithDefaults() throws SQLException {
    }

    public void populateForTests() throws SQLException {
    }

    public void populateForDemo() throws SQLException {
    }

    public <T> void callInTransaction( Callable<T> callable ) throws SQLException {
        TransactionManager.callInTransaction(connectionSource, callable);
    }

    public <T> int createTableIfNotExists( Class<T> tClass ) throws Exception {
        String tableName = DatabaseHandler.getTableName(tClass);
        if (db.hasTable(tableName)) {
            KukuratusLogger.logDebug(this, "Table exists already: " + tableName); //$NON-NLS-1$
            return -1;
        }
        int createTableIfNotExists = TableUtils.createTableIfNotExists(connectionSource, tClass);
        if (ISpatialTable.class.isAssignableFrom(tClass)) {
            String columnName = null;
            EGeometryType gt = null;
            Field[] fields = tClass.getFields();
            for( Field field : fields ) {
                Class< ? > type = field.getType();
                gt = EGeometryType.forClass(type);
                if (gt != EGeometryType.UNKNOWN) {
                    DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
                    columnName = databaseField.columnName();
                    break;
                }
            }
            if (columnName != null) {
                if (db instanceof H2GisDb) {
                    H2GisDb h2gisDb = (H2GisDb) db;
                    h2gisDb.addSrid(tableName, DatabaseHandler.TABLES_EPSG, ISpatialTable.GEOM_FIELD_NAME);
                    h2gisDb.createSpatialIndex(tableName, ISpatialTable.GEOM_FIELD_NAME);
                } else if (db instanceof PostgisDb) {
                    PostgisDb postgisDb = (PostgisDb) db;
                    // remove column
                    String drop = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName + ";"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    db.executeInsertUpdateDeleteSql(drop);
                    // add spatial
                    postgisDb.addGeometryXYColumnAndIndex(tableName, columnName, gt.name(), DatabaseHandler.TABLES_EPSG, false);
                } else if (db instanceof SpatialiteDb) {
                    // SpatialiteDb spatialiteDb = (SpatialiteDb) db;
                    String geometryType = DatabaseHandler.getGeometryType(tClass);
                    db.execOnConnection(conn -> {
                        // SELECT RecoverGeometryColumn('pipespieces', 'the_geom', 4326,
                        // 'LINESTRING', 'XY')
                        String sql = "SELECT RecoverGeometryColumn('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "', " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + DatabaseHandler.TABLES_EPSG + ", '" + geometryType + "', 'XY')"; //$NON-NLS-1$ //$NON-NLS-2$
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }
                        // SELECT CreateSpatialIndex('pipespieces','the_geom');
                        sql = "SELECT CreateSpatialIndex('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "')"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }

                        return null;
                    });
                }
            }
        }

        return createTableIfNotExists;
    }

    /**
     * Get the {@link Dao} for a given class.
     * 
     * @param type the class to use.
     * @return the dao.
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <T> Dao<T, ? > getDao( Class<T> type ) throws SQLException {
        Dao<T, ? > dao = (Dao<T, ? >) daosMap.get(type);
        if (dao == null) {
            dao = DaoManager.createDao(connectionSource, type);
        }
        return dao;
    }

    /**
     * Get the querybuilder for a given class.
     * 
     * @param type the class to use.
     * @return the querybuilder.
     * @throws SQLException
     */
    public <T> QueryBuilder<T, ? > getQueryBuilder( Class<T> type ) throws SQLException {
        Dao<T, ? > dao = getDao(type);
        QueryBuilder<T, ? > queryBuilder = dao.queryBuilder();
        return queryBuilder;
    }

    /**
     * Get the updateBuilder for a given class.
     * 
     * @param type the class to use.
     * @return the updateBuilder.
     * @throws SQLException
     */
    public <T> UpdateBuilder<T, ? > getUpdateBuilder( Class<T> type ) throws SQLException {
        Dao<T, ? > dao = getDao(type);
        UpdateBuilder<T, ? > updateBuilder = dao.updateBuilder();
        return updateBuilder;
    }

    public <T> int deleteTableContent( Class<T> type ) throws SQLException {
        Dao<T, ? > dao = getDao(type);
        int deletedRows = dao.deleteBuilder().delete();
        return deletedRows;
    }

    @Override
    public void close() throws Exception {
        daosMap.clear();
        connectionSource.close();
        db.close();
    }

    public static String getTableName( Class< ? > ormliteClass ) {
        DatabaseTable annotation = ormliteClass.getAnnotation(DatabaseTable.class);
        String tableName = annotation.tableName();
        return tableName;
    }

    public static String getGeometryType( Class< ? > ormliteClass ) throws Exception {
        Field field = ormliteClass.getField(ISpatialTable.GEOM_FIELD_NAME);
        Class< ? > declaringClass = field.getType();
        String simpleName = declaringClass.getSimpleName();
        return simpleName.toUpperCase();
    }

}
