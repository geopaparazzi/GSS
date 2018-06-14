package com.hydrologis.gss.utils;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.hortonmachine.dbs.compat.objects.QueryResult;

public class QueryResultContentProvider implements IStructuredContentProvider {

	private static QueryResultContentProvider instance;

	/**
	 * Returns an instance of QueryResultContentProvider. Since instances of this
	 * class do not maintain any state, they can be shared between multiple
	 * clients.
	 * 
	 * @return an instance of QueryResultContentProvider
	 * 
	 * @since 1.3
	 */
	public static QueryResultContentProvider getInstance() {
		synchronized(QueryResultContentProvider.class) {
			if (instance == null) {
				instance = new QueryResultContentProvider();
			}
			return instance;
		}
	}
    /**
     * Returns the elements in the input, which must be either an array or a
     * <code>Collection</code>. 
     */
    public Object[] getElements(Object inputElement) {
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

    /**
     * This implementation does nothing.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing.
    }

    /**
     * This implementation does nothing.
     */
    public void dispose() {
        // do nothing.
    }
}