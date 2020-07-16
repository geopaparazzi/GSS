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
package com.hydrologis.kukuratus.utils.log;

public class LogMessages {
    private long id;
    private String ts;
    private String type;
    private String tag;
    private String msg;
    public long getId() {
        return id;
    }
    public void setId( long id ) {
        this.id = id;
    }
    public String getTs() {
        return ts;
    }
    public void setTs( String ts ) {
        this.ts = ts;
    }
    public String getType() {
        return type;
    }
    public void setType( String type ) {
        this.type = type;
    }
    public String getTag() {
        return tag;
    }
    public void setTag( String tag ) {
        this.tag = tag;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg( String msg ) {
        this.msg = msg;
    }
    @Override
    public String toString() {
        return "LogMessages [id=" + id + ", ts=" + ts + ", type=" + type + ", tag=" + tag + ", msg=" + msg + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }
    
    

}
