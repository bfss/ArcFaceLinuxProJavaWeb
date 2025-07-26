package com.bfss.arcfacelinuxprojavaweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "face_info")
public class ArcFaceInfoEntity {
    // JPA实体类，由JPA自动在数据库中创建表
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_name")
    private String personName;

    @Column(name = "face_feature", nullable = false)
    private byte[] faceFeature;

    @Column(name = "image_path")
    private String imagePath;

    public ArcFaceInfoEntity() {}

    public ArcFaceInfoEntity(String personName, byte[] faceFeature, String imagePath){
        this.personName = personName;
        this.faceFeature = faceFeature;
        this.imagePath = imagePath;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getPersonName(){
        return personName;
    }

    public void setPersonName(String personName){
        this.personName = personName;
    }

    public byte[] getFaceFeature(){
        return faceFeature;
    }

    public void setFaceFeature(byte[] faceFeature){
        this.faceFeature = faceFeature;
    }

    public String getImagePath(){
        return imagePath;
    }

    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }
}
