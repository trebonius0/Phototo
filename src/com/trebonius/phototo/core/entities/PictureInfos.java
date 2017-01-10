package com.trebonius.phototo.core.entities;

import com.google.gson.annotations.Expose;

public class PictureInfos {

    @Expose
    public final String url;
    
    @Expose
    public final int width;
    
    @Expose
    public final int height;

    public PictureInfos(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

}
