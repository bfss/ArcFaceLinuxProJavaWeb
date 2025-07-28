package com.bfss.arcfacelinuxprojavaweb.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.bfss.arcfacelinuxprojavaweb.engine.ArcFaceEngineManager;
import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import com.bfss.arcfacelinuxprojavaweb.repository.ArcFaceInfoRepository;

import jakarta.transaction.Transactional;

@Service
public class ArcFaceService {
    // 人脸注册服务
    // 此处以注册举例
    private static final Logger logger = LoggerFactory.getLogger(ArcFaceService.class);

    private final ArcFaceEngineManager arcFaceEngineManager;
    private final ArcFaceInfoRepository arcFaceInfoRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ArcFaceService(ArcFaceEngineManager arcFaceEngineManager, ArcFaceInfoRepository arcFaceInfoRepository){
        this.arcFaceEngineManager = arcFaceEngineManager;
        this.arcFaceInfoRepository = arcFaceInfoRepository;
    }

    @Transactional
    public ArcFaceInfoEntity registerFaceFromImage(MultipartFile file){
        // 人脸注册
        try{
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(file.getOriginalFilename());
            // 此处为方便演示，使用文件覆盖选项
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            File storedFile = targetPath.toFile();

            // 将特征提取任务提交到线程池
            Future<FaceFeature> future = arcFaceEngineManager.submitFeatureExtraction(storedFile);
            // 获取异步结果 (可以设置超时)
            // 实际应用中，这里不应该直接阻塞等待，而是立即返回，让客户端异步查询结果，或者使用WebSocket推送
            // 但为简化演示，我们这里同步等待
            // 最多等待30秒
            FaceFeature faceFeature = future.get(30, TimeUnit.SECONDS); 

            // 这里使用文件名作为personName，在实际项目中可能需要从前端获取
            ArcFaceInfoEntity arcFaceInfo = new ArcFaceInfoEntity(file.getOriginalFilename(), faceFeature.getFeatureData(), targetPath.toString());
            arcFaceInfo = arcFaceInfoRepository.save(arcFaceInfo);

            return arcFaceInfo;
        } catch (Exception e){
            logger.error("注册函数发生异常", e);
            return null;
        }
    }
}
