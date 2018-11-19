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
package com.hydrologis.gss.server.utils;

import java.io.File;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.gears.utils.files.FileUtilities;

import com.hydrologis.gss.server.database.objects.Forms;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.j256.ormlite.dao.Dao;

public class FormDatabaseUtilis {
    public static void main( String[] args ) throws Exception {
//        insertForm("/home/hydrologis/TMP/TESTGSS/DATA/gss_database.mv.db",
//                "/home/hydrologis/TMP/TESTGSS/FW_TreeVisualAssessment.json");
        insertForm("/home/hydrologis/TMP/TESTGSS/DATA/gss_database.mv.db", "/home/hydrologis/TMP/TESTGSS/tags.json"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static void insertForm( String dbPath, String formPath ) throws Exception {
        EDb edb = EDb.fromFileDesktop(new File(dbPath));
        try (ASpatialDb spatialDb = edb.getSpatialDb()) {
            spatialDb.open(dbPath);

            DatabaseHandler databaseHandler = DatabaseHandler.instance(spatialDb);

            if (!spatialDb.hasTable(DatabaseHandler.getTableName(Forms.class))) {
                databaseHandler.createTableIfNotExists(Forms.class);
            }
            Dao<Forms, ? > formsDao = databaseHandler.getDao(Forms.class);

            String formString = FileUtilities.readFile(formPath);
            String formName = FileUtilities.getNameWithoutExtention(new File(formPath));

            Forms form = new Forms(formName, formString, "god", FormStatus.VISIBLE.getStatusCode()); //$NON-NLS-1$
            formsDao.create(form);

        }
    }

}
