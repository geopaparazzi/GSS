package com.hydrologis.gss.utils;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.hortonmachine.dbs.compat.objects.QueryResult;

public class DevicesTableContentProvider implements IStructuredContentProvider {

    private static DevicesTableContentProvider instance;

    public static DevicesTableContentProvider getInstance() {
        synchronized (DevicesTableContentProvider.class) {
            if (instance == null) {
                instance = new DevicesTableContentProvider();
            }
            return instance;
        }
    }
    public Object[] getElements( Object inputElement ) {
        if (inputElement instanceof Object[]) {
            return (Object[]) inputElement;
        }
        if (inputElement instanceof Collection) {
            return ((Collection) inputElement).toArray();
        }
        if (inputElement instanceof QueryResult) {
            return ((QueryResult) inputElement).data.toArray();
        }
        return new Object[0];
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        // do nothing.
    }

    public void dispose() {
        // do nothing.
    }
}