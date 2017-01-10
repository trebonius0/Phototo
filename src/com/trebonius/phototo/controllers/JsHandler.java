package com.trebonius.phototo.controllers;

import java.nio.file.Path;

public class JsHandler extends FileHandler {

    public JsHandler(String prefix, Path folderRoot) {
        super(prefix, folderRoot);
    }

    @Override
    protected String getContentType(String extension) {
        return "application/javascript";
    }

}
