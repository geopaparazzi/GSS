package com.hydrologis.gss.server.utils;

public enum FormStatus {
    HIDDEN(0),
    VISIBLE(1);

    private int statusCode;

    private FormStatus( int statusCode ) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public FormStatus fromCode( int status ) {
        for( FormStatus fs : values() ) {
            if (fs.getStatusCode() == status) {
                return fs;
            }
        }
        throw new IllegalArgumentException("No status available for code: " + status);
    }

}
