package com.bfss.arcfacelinuxprojavaweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bfss.arcfacelinuxprojavaweb.engine.ArcFaceEngineManager;
import com.bfss.arcfacelinuxprojavaweb.repository.ArcFaceInfoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class ArcFaceInitService {
    // 引擎初始化服务，用于在APP启动前自动初始化引擎
    // 由于使用多线程，多引擎特征不共享，故此处省略向引擎中注册数据库已有特征
    // 因为在配置文件中配置了spring.jpa.hibernate.ddl-auto=create
    // 所以每次启动前数据库都会被清空，如果需要保留，可以修改create为其他值
    private static final Logger logger = LoggerFactory.getLogger(ArcFaceInitService.class);

    private final ArcFaceEngineManager arcFaceEngineManager;
    // 没有用到faceInfoRepository
    private final ArcFaceInfoRepository faceInfoRepository; 

    public ArcFaceInitService(ArcFaceEngineManager arcFaceEngineManager, ArcFaceInfoRepository faceInfoRepository) {
        this.arcFaceEngineManager = arcFaceEngineManager;
        this.faceInfoRepository = faceInfoRepository;
    }

    @PostConstruct
    public void init() {
        // 在Spring应用启动时初始化FaceEngine实例池和线程池
        logger.info("开始初始化人脸识别引擎实例池和线程池...");
        arcFaceEngineManager.initPool();
    }

    @PreDestroy
    public void destroy() {
        // 在Spring应用关闭时销毁所有FaceEngine实例并关闭线程池
        logger.info("开始关闭人脸识别引擎实例池和线程池...");
        arcFaceEngineManager.destroyPool();
    }
}
