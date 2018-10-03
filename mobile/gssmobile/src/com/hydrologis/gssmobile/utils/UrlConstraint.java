/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.utils;

import com.codename1.ui.validation.Constraint;

/**
 *
 * @author hydrologis
 */
public class UrlConstraint implements Constraint {

    @Override
    public boolean isValid(Object value) {
        String url = value.toString();
        if (!url.startsWith("http")) {
            return false;
        }
        return true;
    }

    @Override
    public String getDefaultFailMessage() {
        return "Format has to be: http://host";
    }

}
