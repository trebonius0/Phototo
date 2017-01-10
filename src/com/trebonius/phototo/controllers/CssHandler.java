package com.trebonius.phototo.controllers;

import java.nio.file.Path;

public class CssHandler extends FileHandler {

    public CssHandler(Path folderRoot, String prefix) {
        super(folderRoot, prefix, "css");
    }

    @Override
    protected String getContentType(String extension) {
        return "text/css;charset=UTF-8";
    }

}
