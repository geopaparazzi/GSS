package com.hydrologis.gss.server.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.IDbVisitor;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.spatialite.hm.SpatialiteDb;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsData;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcSingleConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHandler implements AutoCloseable, IDbVisitor {

    private static final String TABLES_EPSG = "4326";
    private ConnectionSource connectionSource;
    private ASpatialDb db;
    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            GpsLogsProperties.class);
    private HashMap<Class< ? >, Dao< ? , ? >> daosMap = new HashMap<>();

    public static DatabaseHandler instance( ASpatialDb db ) throws Exception {
        DatabaseHandler dbHandler = new DatabaseHandler(db);
        db.accept(dbHandler);
        return dbHandler;
    }

    public List<Class< ? >> getTableClasses() {
        return tableClasses;
    }

    private DatabaseHandler( ASpatialDb db ) throws SQLException {
        this.db = db;
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

    public void createTables() throws Exception {
        for( Class< ? > tClass : tableClasses ) {
            System.out.println("Create if not exists: " + getTableName(tClass));
            TableUtils.createTableIfNotExists(connectionSource, tClass);
            if (ISpatialTable.class.isAssignableFrom(tClass)) {
                if (db instanceof H2GisDb) {
                    H2GisDb h2gisDb = (H2GisDb) db;
                    String tableName = getTableName(tClass);
                    h2gisDb.addSrid(tableName, TABLES_EPSG, ISpatialTable.GEOM_FIELD_NAME);
                    h2gisDb.createSpatialIndex(tableName, ISpatialTable.GEOM_FIELD_NAME);
                } else if (db instanceof SpatialiteDb) {
                    // SpatialiteDb spatialiteDb = (SpatialiteDb) db;
                    String tableName = getTableName(tClass);
                    String geometryType = getGeometryType(tClass);
                    db.execOnConnection(conn -> {
                        // SELECT RecoverGeometryColumn('pipespieces', 'the_geom', 4326,
                        // 'LINESTRING', 'XY')
                        String sql = "SELECT RecoverGeometryColumn('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "', "
                                + TABLES_EPSG + ", '" + geometryType + "', 'XY')";
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }
                        // SELECT CreateSpatialIndex('pipespieces','the_geom');
                        sql = "SELECT CreateSpatialIndex('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "')";
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }

                        return null;
                    });
                }
            }
        }
    }

    public void populateWithDefaults() throws SQLException {
    }

    public void populateForTests() throws SQLException {
    }

    public void populateForDemo() throws SQLException {
    }
    
    public <T> void callInTransaction(Callable<T> callable) throws SQLException {
        TransactionManager.callInTransaction(connectionSource, callable);
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
