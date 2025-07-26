package com.bfss.arcfacelinuxprojavaweb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bfss.arcfacelinuxprojavaweb.dto.ArcFaceSearchResponse;
import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import com.bfss.arcfacelinuxprojavaweb.service.ArcFaceService;

@RestController
@RequestMapping("api/face")
public class ArcFaceController {
    // API接口

    private ArcFaceService arcFaceService;

    public ArcFaceController(ArcFaceService arcFaceService){
        this.arcFaceService = arcFaceService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
        @RequestParam("image") MultipartFile image
    ){
        // 人脸注册接口
        ArcFaceInfoEntity arcFaceInfo = arcFaceService.registerFaceFromImage(image);
        if (arcFaceInfo  == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("失败");
        }else{
            return ResponseEntity.status(HttpStatus.CREATED).body("成功");
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchFace(
        @RequestParam("image") MultipartFile image
    ){
        // 人脸搜索接口
        ArcFaceSearchResponse arcFaceSearchResponse = arcFaceService.searchFaceByImage(image);
        if (arcFaceSearchResponse == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("失败");
        }else{
            return ResponseEntity.ok(arcFaceSearchResponse);
        }  
    }
}
