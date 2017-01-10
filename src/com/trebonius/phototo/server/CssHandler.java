package com.trebonius.phototo.server;

import java.nio.file.Path;

public class CssHandler extends FileHandler {

    public CssHandler(String prefix, Path folderRoot) {
        super(prefix, folderRoot);
    }

    @Override
    protected String getContentType(String extension) {
        return "text/css;charset=UTF-8";
    }

}
