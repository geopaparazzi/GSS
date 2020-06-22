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
package com.hydrologis.kukuratus.registry;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.hortonmachine.dbs.compat.ADb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IDbVisitor;
import org.mindrot.jbcrypt.BCrypt;

import com.hydrologis.kukuratus.tiles.EOnlineTileSources;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcSingleConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

/**
 * Handler for the registry database.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public enum RegistryHandler implements IDbVisitor {
    INSTANCE;

    public static final String MAPSFORGE = "Mapsforge"; //$NON-NLS-1$
    public static String SETTINGS_KEY_MAPS = "KUKURATUS_SETTINGS_KEY_MAPS"; //$NON-NLS-1$

    private ConnectionSource connectionSource;
    private ADb db;

    private Dao<Authorization, Integer> authorizationDao;
    private Dao<Group, Integer> groupDao;
    private Dao<User, Integer> userDao;
    private Dao<Settings, Integer> settingsDao;

    private RegistryHandler() {
    }

    /**
     * Initialize the registry with an existing db.
     * 
     * <p>
     * This needs to be done before any other action else automatic db creationg
     * will kick in.
     * 
     * @param existingDb the db to use for the registry.
     */
    public void initWithDb(ADb existingDb) {
        if (db != null) {
            throw new IllegalArgumentException("The db for the registry has already been set. Check your settings.");
        }
        try {
            db = existingDb;
            db.accept(this);

            initTablesIfNeeded();
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void checkInit() {
        if (db == null) {
            try {
                File registryDatabase = KukuratusWorkspace.getInstance().getRegistryDatabase();
                db = EDb.SQLITE.getDb();
                db.open(registryDatabase.getAbsolutePath());
                db.accept(this);

                initTablesIfNeeded();

            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        }
    }

    private void initTablesIfNeeded() throws SQLException, Exception {
        if (authorizationDao != null) {
            KukuratusLogger.logError(this, "Init called with already existing DAOs. Should not happen.",
                    new RuntimeException());
            return;
        }

        authorizationDao = DaoManager.createDao(connectionSource, Authorization.class);
        groupDao = DaoManager.createDao(connectionSource, Group.class);
        userDao = DaoManager.createDao(connectionSource, User.class);
        settingsDao = DaoManager.createDao(connectionSource, Settings.class);

        if (!db.hasTable(getTableName(Authorization.class))) {
            TableUtils.createTableIfNotExists(connectionSource, Authorization.class);

            // and create some default data
            Authorization adminAuth = new Authorization(IRegistryVars.adminAuthorization);
            Authorization userAuth = new Authorization(IRegistryVars.userAuthorization);
            authorizationDao.createIfNotExists(adminAuth);
            authorizationDao.createIfNotExists(userAuth);

            TableUtils.createTableIfNotExists(connectionSource, Group.class);
            TableUtils.createTableIfNotExists(connectionSource, User.class);

            Group adminsGroup = new Group(IRegistryVars.adminGroup, adminAuth);
            Group usersGroup = new Group(IRegistryVars.userGroup, userAuth);
            groupDao.createIfNotExists(adminsGroup);
            groupDao.createIfNotExists(usersGroup);

            User adminUser = new User(IRegistryVars.FIRST_ADMIN_USERNAME, IRegistryVars.FIRST_ADMIN_UNIQUE_USER,
                    IRegistryVars.FIRST_ADMIN_EMAIL, IRegistryVars.FIRST_ADMIN_PWD, adminsGroup);
            userDao.createIfNotExists(adminUser);
            User normalUser = new User(IRegistryVars.FIRST_USER_USERNAME, IRegistryVars.FIRST_USER_UNIQUE_USER,
                    IRegistryVars.FIRST_USER_EMAIL, IRegistryVars.FIRST_USER_PWD, usersGroup);
            userDao.createIfNotExists(normalUser);

            TableUtils.createTableIfNotExists(connectionSource, Settings.class);
        }
    }

    /**
     * Checks the login and returns the logged user if ok.
     * 
     * @param uniqueUserName the username to test.
     * @param pwd            the pwd of the user.
     * @return the user if login was ok, else <code>null</code>.
     * @throws Exception
     */
    public User isLoginOk(String uniqueUserName, String pwd) throws Exception {
        User user = getUserByUniqueName(uniqueUserName);
        if (user != null && BCrypt.checkpw(pwd, user.getPwd())) {
            return user;
        }
        return null;
    }

    /**
     * Get a USer by its unique name.
     * 
     * @param uniqueUserName the unique name.
     * @return the user or <code>null</code>, if none exists for the given username.
     * @throws Exception
     */
    public User getUserByUniqueName(String uniqueUserName) throws Exception {
        checkInit();
        if (uniqueUserName.indexOf(' ') != -1) {
            // user name can't have spaces
            return null;
        }
        QueryBuilder<User, Integer> queryBuilder = userDao.queryBuilder();
        queryBuilder.where().eq(User.UNIQUENAME_FIELD_NAME, uniqueUserName);
        User user = userDao.queryForFirst(queryBuilder.prepare());
        return user;
    }

    /**
     * Get a User by its id.
     * 
     * @param id the id.
     * @return the user or <code>null</code>, if none exists for the given id.
     * @throws Exception
     */
    public User getUserById(long id) throws Exception {
        checkInit();

        QueryBuilder<User, Integer> queryBuilder = userDao.queryBuilder();
        queryBuilder.where().eq(User.ID_FIELD_NAME, id);
        User user = userDao.queryForFirst(queryBuilder.prepare());
        return user;
    }

    public List<User> getUsersList() {
        List<User> all = new ArrayList<>();
        try {
            List<Group> groupsWithAuthorizations = getGroupsWithAuthorizations();
            for (Group group : groupsWithAuthorizations) {
                all.addAll(getUsersOfGroup(group));
            }
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
        }
        return all;
    }

    /**
     * Check if the user is Admin.
     * 
     * @param user the user to check.
     * @return <code>true</code> if the user is admin.
     */
    public boolean isAdmin(User user) {
        checkInit();
        try {
            if (user == null) {
                return false;
            }
            Group group = user.getGroup();
            group = groupDao.queryForId(group.getId());
            Authorization authorization = group.getAuthorization();
            authorization = authorizationDao.queryForId(authorization.getId());
            if (authorization.getName().equals(IRegistryVars.adminAuthorization)) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
            return false;
        }
    }

    /**
     * Check if the user is normal user.
     * 
     * @param user the user to check.
     * @return <code>true</code> if the user is normal.
     */
    public boolean isUser(User user) {
        checkInit();
        try {
            if (user == null) {
                return false;
            }
            Group group = user.getGroup();
            group = groupDao.queryForId(group.getId());
            Authorization authorization = group.getAuthorization();
            authorization = authorizationDao.queryForId(authorization.getId());
            if (authorization.getName().equals(IRegistryVars.userAuthorization)) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
            return false;
        }
    }

    /**
     * Get the user groups with the authorizations set.
     * 
     * @return the groups.
     * @throws SQLException
     */
    public List<Group> getGroupsWithAuthorizations() throws SQLException {
        checkInit();
        List<Group> allGroups = groupDao.queryForAll();

        List<Authorization> authorizations = authorizationDao.queryForAll();
        HashMap<Integer, Authorization> id2Auth = new HashMap<>();
        for (Authorization authorization : authorizations) {
            id2Auth.put(authorization.getId(), authorization);
        }

        for (Group group : allGroups) {
            int id = group.getAuthorization().getId();
            group.setAuthorization(id2Auth.get(id));
        }

        return allGroups;
    }

    public void updateGroup(Group group) {
        try {
            groupDao.update(group);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteGroup(Group group) {
        try {
            groupDao.delete(group);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            userDao.update(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(User user) {
        try {
            userDao.delete(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all users of a group.
     * 
     * @param group the group to query for.
     * @return the list of users.
     * @throws SQLException
     */
    public List<User> getUsersOfGroup(Group group) throws SQLException {
        checkInit();
        QueryBuilder<User, Integer> queryBuilder = userDao.queryBuilder();
        queryBuilder.where().eq(User.GROUP_FIELD_NAME, group.getId());
        List<User> users = queryBuilder.query();
        return users;
    }

    /**
     * @return the list of authorizations.
     * @throws SQLException
     */
    public List<Authorization> getAuthorizations() throws SQLException {
        checkInit();
        return authorizationDao.queryForAll();
    }

    /**
     * Get a group by its name.
     * 
     * @param groupName the name.
     * @return the group.
     * @throws SQLException
     */
    public Group getGroupByName(String groupName) throws SQLException {
        checkInit();
        QueryBuilder<Group, Integer> queryBuilder = groupDao.queryBuilder();
        queryBuilder.where().eq(Group.DESCR_FIELD_NAME, groupName);
        List<Group> groups = queryBuilder.query();
        if (groups.size() == 0) {
            return null;
        }
        return groups.get(0);
    }

    /**
     * Get the authorization by its name.
     * 
     * @param authName the name.
     * @return the authorization.
     * @throws SQLException
     */
    public Authorization getAuthorizationByName(String authName) throws SQLException {
        checkInit();
        QueryBuilder<Authorization, Integer> queryBuilder = authorizationDao.queryBuilder();
        queryBuilder.where().eq(Authorization.NAME_FIELD_NAME, authName);
        List<Authorization> auth = queryBuilder.query();
        if (auth.size() == 0) {
            return null;
        }
        return auth.get(0);
    }

    /**
     * Add a new group.
     * 
     * @param group
     * @throws SQLException
     */
    public void addGroup(Group group) throws SQLException {
        checkInit();
        groupDao.create(group);
    }

    /**
     * Delete a group.
     * 
     * @param group the group to delete.
     * @throws SQLException
     */
    public void removeGroup(Group group) throws SQLException {
        checkInit();
        QueryBuilder<User, Integer> userQB = userDao.queryBuilder();
        List<User> userToRemove = userQB.where().eq(User.GROUP_FIELD_NAME, group).query();
        userDao.delete(userToRemove);
        groupDao.delete(group);
    }

    /**
     * Add a new user.
     * 
     * @param user the user to add.
     * @throws SQLException
     */
    public void addUser(User user) throws SQLException {
        checkInit();
        userDao.create(user);
    }

    /**
     * Delete a user.
     * 
     * @param user the user to delete.
     * @throws SQLException
     */
    public void removeUser(User user) throws SQLException {
        checkInit();
        userDao.delete(user);
    }

    /**
     * Get a user setting from the db.
     * 
     * @param key          the settings key.
     * @param defaultValue the value in case the setting doesn't exist.
     * @param userName     the userName the setting belongs to.
     * @return the setting string value.
     * @throws SQLException
     */
    public String getSettingByKey(String key, String defaultValue, String userName) {
        checkInit();
        try {
            Settings settings = settingsDao.queryBuilder().where().eq(Settings.KEY_FIELD_NAME, key).and()
                    .eq(Settings.USER_FIELD_NAME, userName).queryForFirst();
            if (settings == null || settings.value == null) {
                return defaultValue;
            }
            return settings.value;
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
        }
        return null;
    }

    /**
     * Get a global setting from the db.
     * 
     * @param key          the settings key.
     * @param defaultValue the value in case the setting doesn't exist.
     * @return the setting string value.
     * @throws SQLException
     */
    public String getGlobalSettingByKey(String key, String defaultValue) {
        checkInit();
        try {
            Settings settings = settingsDao.queryBuilder().where().eq(Settings.KEY_FIELD_NAME, key).and()
                    .eq(Settings.USER_FIELD_NAME, Settings.GLOBALUSER).queryForFirst();
            if (settings == null || settings.value == null) {
                return defaultValue;
            }
            return settings.value;
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
        }
        return defaultValue;
    }

    /**
     * Insert or update a setting.
     * 
     * @param setting the setting to add or update.
     * @throws SQLException
     */
    public void insertOrUpdateSetting(Settings setting) throws SQLException {
        checkInit();
        settingsDao.createOrUpdate(setting);
    }

    /**
     * Insert or update a global setting.
     * 
     * @param setting the setting to add or update.
     * @throws SQLException
     */
    public void insertOrUpdateGlobalSetting(Settings setting) throws SQLException {
        checkInit();
        setting.userName = Settings.GLOBALUSER;
        settingsDao.createOrUpdate(setting);
    }

    /**
     * @return the list of available tilesources names.
     */
    public static List<String> getAllTileSourcesNames() {
        List<String> tileSourcesNames = new ArrayList<>();
        tileSourcesNames.add(MAPSFORGE);
        for (EOnlineTileSources source : EOnlineTileSources.values()) {
            tileSourcesNames.add(source.getName());
        }
        return tileSourcesNames;
    }

    /**
     * Get selected tilesources names from the settings.
     * 
     * @param userName the user the setting belongs to.
     * @return the list of names.
     */
    public static List<String> getSelectedTileSourcesNames(String userName) {
        String colonSepMaps = RegistryHandler.INSTANCE.getSettingByKey(SETTINGS_KEY_MAPS,
                EOnlineTileSources.Open_Street_Map_Standard.getName(), userName);
        String[] split = colonSepMaps.split(";"); //$NON-NLS-1$
        return new ArrayList<>(Arrays.asList(split));
    }

    /**
     * Put selected tilesources names in the settings.
     * 
     * @param names    the names of teh tilesources to insert.
     * @param userName the name of the user the setting belongs to.
     */
    public void putSelectedTileSourcesNames(List<String> names, String userName) {
        String maps = names.stream().collect(Collectors.joining(";")); //$NON-NLS-1$
        Settings newSetting = new Settings(SETTINGS_KEY_MAPS, maps, userName);
        try {
            RegistryHandler.INSTANCE.insertOrUpdateSetting(newSetting);
        } catch (SQLException e) {
            KukuratusLogger.logError(this, e);
        }
    }

    @Override
    public void visit(DataSource dataSource) throws Exception {
        if (dataSource != null) {
            String url = db.getJdbcUrlPre() + db.getDatabasePath();
            connectionSource = new DataSourceConnectionSource(dataSource, url);
        }
    }

    @Override
    public void visit(Connection singleConnection) throws Exception {
        if (singleConnection != null) {
            String url = db.getJdbcUrlPre() + db.getDatabasePath();
            connectionSource = new JdbcSingleConnectionSource(url, singleConnection);
            ((JdbcSingleConnectionSource) connectionSource).initialize();
        }
    }

    /**
     * Get the name of the table the class describes.
     * 
     * @param ormliteClass the ormlite annotated class.
     * @return the name of the table.
     */
    public static String getTableName(Class<?> ormliteClass) {
        DatabaseTable annotation = ormliteClass.getAnnotation(DatabaseTable.class);
        String tableName = annotation.tableName();
        return tableName;
    }

    public static String hashPwd(String pwd) {
        String hashed = BCrypt.hashpw(pwd, BCrypt.gensalt(12));
        return hashed;
    }

}
