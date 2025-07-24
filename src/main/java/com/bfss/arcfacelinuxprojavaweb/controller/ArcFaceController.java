package com.bfss.arcfacelinuxprojavaweb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bfss.arcfacelinuxprojavaweb.service.ArcFaceService;

@RestController
@RequestMapping("api/face")
public class ArcFaceController {

    private ArcFaceService arcFaceService;

    public ArcFaceController(ArcFaceService arcFaceService){
        this.arcFaceService = arcFaceService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
        @RequestParam("image") MultipartFile image
    ){
        arcFaceService.registerFaceFromImage(image);
        return ResponseEntity.status(HttpStatus.CREATED).body("成功");
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchFace(
        @RequestParam("image") MultipartFile image
    ){
        return ResponseEntity.ok(arcFaceService.searchFaceByImage(image));
    }
}
