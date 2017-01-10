package com.trebonius.phototo.controllers;

import java.nio.file.Path;

public class JsHandler extends FileHandler {

    public JsHandler(Path folderRoot, String prefix) {
        super(folderRoot, prefix, "js");
    }

    @Override
    protected String getContentType(String extension) {
        return "application/javascript";
    }

}
