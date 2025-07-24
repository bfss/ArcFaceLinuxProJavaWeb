package com.bfss.arcfacelinuxprojavaweb.dto;

public class ArcFaceSearchResponse {
    private boolean success;
    private String imagePath;

    public ArcFaceSearchResponse(boolean success, String imagePath){
        this.success = success;
        this.imagePath = imagePath;
    }

    public void setSuccess(boolean success){
        this.success = success;
    }

    public boolean getSuccess(){
        return success;
    }

    public void setImgePath(String imagePath){
        this.imagePath = imagePath;
    }

    public String getImagePath(){
        return imagePath;
    }

}
