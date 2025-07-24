package com.bfss.arcfacelinuxprojavaweb.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bfss.arcfacelinuxprojavaweb.engine.ArcFaceEngine;
import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import com.bfss.arcfacelinuxprojavaweb.repository.ArcFaceInfoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class ArcFaceInitService {
    private static final Logger logger = LoggerFactory.getLogger(ArcFaceInitService.class);

    private final ArcFaceEngine arcFaceEngine;
    // 从数据库加载特征
    private final ArcFaceInfoRepository faceInfoRepository; 

    public ArcFaceInitService(ArcFaceEngine arcFaceEngine, ArcFaceInfoRepository faceInfoRepository) {
        this.arcFaceEngine = arcFaceEngine;
        this.faceInfoRepository = faceInfoRepository;
    }

    @PostConstruct
    public void initArcFace() {
        
        // 激活并初始化SDK引擎
        if (!arcFaceEngine.activateAndInitSdk()) {
            logger.error("人脸识别SDK初始化和激活失败，应用程序将无法正常运行。");
            return;
        }

        // 从数据库读取所有特征并注册到SDK
        logger.info("从数据库加载人脸特征并注册到SDK...");
        List<ArcFaceInfoEntity> allFaceFeatures = faceInfoRepository.findAll();

        if (allFaceFeatures.isEmpty()) {
            logger.warn("数据库中没有人脸特征数据");
        } else {
            int registeredCount = 0;
            for (ArcFaceInfoEntity faceInfo : allFaceFeatures) {
                // 使用SDK的registerFace函数，将数据库ID和特征值一起注册
                if (arcFaceEngine.registerFace(faceInfo.getId(), faceInfo.getFaceFeature())) {
                    registeredCount++;
                }
            }
            logger.info("成功注册 {} / {} 个人脸特征到SDK引擎。", registeredCount, allFaceFeatures.size());
            logger.info("当前SDK已注册人脸总数: {}", arcFaceEngine.getRegisteredFaceCount());
        }

        logger.info("--- 人脸识别模块初始化完成 ---");
    }

    @PreDestroy
    public void cleanup() {
        arcFaceEngine.unInitSdk();
    }
}
