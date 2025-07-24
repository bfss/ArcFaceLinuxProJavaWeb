package com.bfss.arcfacelinuxprojavaweb.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.SearchResult;
import com.arcsoft.face.enums.ExtractType;
import com.bfss.arcfacelinuxprojavaweb.dto.ArcFaceSearchResponse;
import com.bfss.arcfacelinuxprojavaweb.engine.ArcFaceEngine;
import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import com.bfss.arcfacelinuxprojavaweb.repository.ArcFaceInfoRepository;

import jakarta.transaction.Transactional;

@Service
public class ArcFaceService {
    private static final Logger logger = LoggerFactory.getLogger(ArcFaceService.class);

    private final ArcFaceEngine arcFaceEngine;
    private final ArcFaceInfoRepository arcFaceInfoRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ArcFaceService(ArcFaceEngine arcFaceEngine, ArcFaceInfoRepository arcFaceInfoRepository){
        this.arcFaceEngine = arcFaceEngine;
        this.arcFaceInfoRepository = arcFaceInfoRepository;
    }

    @Transactional
    public ArcFaceInfoEntity registerFaceFromImage(MultipartFile file){
        try{
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            File storedFile = targetPath.toFile();

            FaceFeature faceFeature = arcFaceEngine.extractFaceFeatureFromImage(storedFile, ExtractType.REGISTER);

            ArcFaceInfoEntity arcFaceInfo = new ArcFaceInfoEntity(faceFeature.getFeatureData(), targetPath.toString());
            arcFaceInfo = arcFaceInfoRepository.save(arcFaceInfo);

            arcFaceEngine.registerFace(arcFaceInfo.getId(), arcFaceInfo.getFaceFeature());
            return arcFaceInfo;
        } catch (IOException e){
            logger.error("注册函数发生异常", e);
            return null;
        }
    }

    public ArcFaceSearchResponse searchFaceByImage(MultipartFile file){
        try{
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempPath);
            File tempFile = tempPath.toFile();
            
            FaceFeature faceFeature = arcFaceEngine.extractFaceFeatureFromImage(tempFile, ExtractType.RECOGNIZE);
            SearchResult searchResult = arcFaceEngine.searchFace(faceFeature.getFeatureData());

            Files.delete(tempPath);

            ArcFaceSearchResponse arcFaceSearchResponse;
            if(searchResult.getMaxSimilar() > 0.8){
                int faceId = searchResult.getFaceFeatureInfo().getSearchId();
                Optional<ArcFaceInfoEntity> faceInfo = arcFaceInfoRepository.findById((long)faceId);
                arcFaceSearchResponse = new ArcFaceSearchResponse(true, faceInfo.get().getImagePath());
            }else{
                arcFaceSearchResponse = new ArcFaceSearchResponse(false, "");
            }
            return arcFaceSearchResponse;
            
        }catch(IOException e){
            logger.error("搜索函数发生异常", e);
            return null;
        }
        
    }
}
