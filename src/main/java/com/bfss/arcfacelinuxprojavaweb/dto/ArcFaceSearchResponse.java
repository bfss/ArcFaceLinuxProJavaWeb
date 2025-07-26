package com.bfss.arcfacelinuxprojavaweb.dto;

public class ArcFaceSearchResponse {
    // 搜索成功返回的结果类
    
    private boolean success;
    private String personName;
    private String imagePath;

    public ArcFaceSearchResponse(boolean success, String personName, String imagePath){
        this.personName = personName;
        this.success = success;
        this.imagePath = imagePath;
    }

    public void setSuccess(boolean success){
        this.success = success;
    }

    public boolean getSuccess(){
        return success;
    }

    public void setPersonName(String personName){
        this.personName = personName;
    }

    public String getPersonName(){
        return personName;
    }

    public void setImgePath(String imagePath){
        this.imagePath = imagePath;
    }

    public String getImagePath(){
        return imagePath;
    }

}
