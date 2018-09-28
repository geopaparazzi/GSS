package com.hydrologis.gss.server.utils;

public abstract class TextRunnable implements Runnable {
    protected String text;

    public abstract void runOnText( String text );
    
    public void setText( String text ) {
        this.text = text;
    }

    @Override
    public void run() {
        runOnText(text);
    }

}
