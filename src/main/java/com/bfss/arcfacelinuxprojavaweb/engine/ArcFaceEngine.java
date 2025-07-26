package com.bfss.arcfacelinuxprojavaweb.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.*;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;

@Component
public class ArcFaceEngine {
    // 将SDK功能封装为Component，以实现依赖注入(DI)与控制反转(IOC)

    private static final Logger logger = LoggerFactory.getLogger(ArcFaceEngine.class);

    @Value("${arcsoft.appId}")
    private String appId;
    @Value("${arcsoft.sdkKey}")
    private String sdkKey;
    @Value("${arcsoft.activeKey}")
    private String activeKey;

    @Value("${arcsoft.libPath}")
    private String libPath;

    private FaceEngine faceEngineInstance = null;

    public boolean activateAndInitSdk() {
        // 激活引擎
        logger.info("--- 正在初始化虹软人脸识别SDK ---");
        try {
            faceEngineInstance = new FaceEngine(libPath);
            logger.info("FaceEngine 实例创建成功，SDK库路径: {}", libPath);
        } catch (Throwable e) {
            logger.error("FaceEngine 实例创建失败: {}", e.getMessage());
            faceEngineInstance = null;
            return false;
        }

        int errorCode = faceEngineInstance.activeOnline(appId, sdkKey, activeKey);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            logger.error("虹软SDK在线激活失败，错误码: {}. ", errorCode);
            faceEngineInstance.unInit();
            faceEngineInstance = null;
            return false;
        }
        logger.info("虹软SDK在线激活成功。");
        
        // 引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_0_ONLY);
        engineConfiguration.setDetectFaceMaxNum(1);
        // 这里使用了Large模型
        engineConfiguration.setFaceModel(FaceModel.ASF_REC_LARGE);

        // 功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);

        // 初始化
        errorCode = faceEngineInstance.init(engineConfiguration);
        if (errorCode != ErrorInfo.MOK.getValue()) {
            logger.error("虹软人脸识别引擎初始化失败，错误码: {}", errorCode);
            faceEngineInstance = null;
            return false;
        }
        logger.info("虹软人脸识别引擎初始化成功。");
        return true;
    }

    public boolean registerFace(Long id, byte[] featureData) {
        // 注册人脸信息
        FaceFeatureInfo faceFeatureInfo = new FaceFeatureInfo();
        faceFeatureInfo.setSearchId(id.intValue());
        faceFeatureInfo.setFeatureData(featureData);

        int errorCode = faceEngineInstance.registerFaceFeature(faceFeatureInfo);
        if (errorCode != ErrorInfo.MOK.getValue()) {
            logger.error("注册人脸特征失败，错误码: {}", errorCode);
            return false;
        }
        logger.debug("成功注册人脸特征 (ID: {}).", id);
        return true;
    }

    public int getRegisteredFaceCount() {
        // 获取已注册的人脸数
        FaceSearchCount faceSearchCount = new FaceSearchCount();
        int errorCode = faceEngineInstance.getFaceCount(faceSearchCount);
        if (errorCode != ErrorInfo.MOK.getValue()) {
            logger.error("获取注册人脸个数失败: {}", errorCode);
            return -1;
        }
        return faceSearchCount.getCount();
    }

    public SearchResult searchFace(byte[] targetFeatureData) {
        // 搜索人脸

        FaceFeature faceFeature = new FaceFeature();
        faceFeature.setFeatureData(targetFeatureData);

        SearchResult searchResult = new SearchResult();
        // 这里默认采用了生活照对比方式
        int errorCode = faceEngineInstance.searchFaceFeature(faceFeature, CompareModel.LIFE_PHOTO, searchResult);

        if (errorCode != ErrorInfo.MOK.getValue()) {
            logger.error("人脸搜索失败，错误码: {}.", errorCode);
            return null;
        }

        if (searchResult != null && searchResult.getFaceFeatureInfo() != null) {
            logger.info("搜索结果 - 匹配ID: {}",
                        searchResult.getFaceFeatureInfo().getSearchId());
            return searchResult;
        } else {
            logger.info("未找到匹配的人脸特征");
            return null;
        }
    }

    public FaceFeature extractFaceFeatureFromImage(File imageFile, ExtractType extractType) {
        // 提取特征
        ImageInfo imageInfo = null;
        try {
            imageInfo = ImageFactory.getRGBData(imageFile);
            if (imageInfo == null) {
                logger.error("无法读取图片数据或图片格式不支持: {}", imageFile.getAbsolutePath());
                return null;
            }

            List<FaceInfo> faceInfoList = new ArrayList<>();
            int errorCode = faceEngineInstance.detectFaces(imageInfo, faceInfoList);
            if (errorCode != ErrorInfo.MOK.getValue()) {
                logger.error("人脸检测失败: {}. ", errorCode);
                return null;
            }
            if (faceInfoList.isEmpty()) {
                logger.warn("图片 {} 中未检测到人脸。", imageFile.getName());
                return null;
            }

            // 在引擎配置处设置了最大检测人脸数为1,所以此处直接提取检测到的第一个人脸
            // 如果需要同时识别多张人脸，更改engineConfiguration.setDetectFaceMaxNum(1)
            // 并根据实际场景选择需要的人脸，如根据人脸框（faceInfo的rect属性）大小选择
            FaceInfo detectedFace = faceInfoList.get(0);

            FaceFeature faceFeature = new FaceFeature();
            errorCode = faceEngineInstance.extractFaceFeature(imageInfo, detectedFace, extractType, 0, faceFeature);
            if (errorCode != ErrorInfo.MOK.getValue()) {
                logger.error("特征提取失败: {}. ", errorCode);
                return null;
            }
            return faceFeature;

        } catch (Exception e) {
            logger.error("处理图片和提取特征时发生异常: {}", e.getMessage());
            return null;
        }
    }

    public void unInitSdk() {
        // 反初始化，销毁SDK
        logger.info("--- 正在销毁虹软人脸识别SDK引擎 ---");
        int errorCode = faceEngineInstance.unInit();
        if (errorCode != ErrorInfo.MOK.getValue()) {
            logger.error("虹软SDK引擎销毁失败: {}", errorCode);
        } else {
            faceEngineInstance = null;
            logger.info("虹软SDK引擎销毁完成。");
        }
    }
}
