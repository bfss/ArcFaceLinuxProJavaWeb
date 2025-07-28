package com.bfss.arcfacelinuxprojavaweb.engine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.*;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;

@Component
public class ArcFaceEngineManager {

    private static final Logger logger = LoggerFactory.getLogger(ArcFaceEngineManager.class);

    @Value("${arcsoft.appId}")
    private String appId;
    @Value("${arcsoft.sdkKey}")
    private String sdkKey;
    @Value("${arcsoft.activeKey}")
    private String activeKey;

    @Value("${arcsoft.libPath}")
    private String libPath;

    // 引擎池和线程池的大小
    @Value("${app.face.poolSize:4}")
    private int poolSize;

    // 引擎实例池：使用BlockingQueue管理FaceEngine实例
    private  BlockingQueue<ArcFaceEngine> enginePool = null;

    // 线程池
    private ExecutorService executorService = null;

    public void initPool() {
        // 初始化
        logger.info("开始初始化人脸识别引擎实例池和线程池...");

        // 初始化FaceEngine实例池
        enginePool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            ArcFaceEngine arcFaceEngine = new ArcFaceEngine();
            // 激活并初始化SDK引擎
            if (!arcFaceEngine.activateAndInitSdk(appId, sdkKey, activeKey, libPath)) {
                logger.error("人脸识别SDK初始化和激活失败，应用程序将无法正常运行。");
                return;
            }
            // 将初始化好的引擎放入队列
            try{
                enginePool.put(arcFaceEngine); 
                logger.info("人脸识别引擎实例 {} 初始化成功并加入池中.", i);
            }catch(InterruptedException e){
                logger.error("人脸识别引擎实例 {} 加入池失败.", i);
                return;
            }
        }

        // 初始化线程池，大小与引擎池一致，以便每个线程可以独占一个引擎
        executorService = Executors.newFixedThreadPool(poolSize);
        logger.info("人脸识别线程池初始化成功，大小为: {}", poolSize);
    }

    public void destroyPool() {
        // 销毁所有FaceEngine实例并关闭线程池
        logger.info("开始关闭人脸识别引擎实例池和线程池...");

        // 关闭线程池, 不再接受新任务，等待已提交任务完成
        executorService.shutdown(); 
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { 
                // 等待最多60秒
                // 强制关闭所有正在执行的任务
                executorService.shutdownNow(); 
                logger.warn("线程池未能正常关闭，强制关闭。");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            // 重新中断当前线程
            Thread.currentThread().interrupt(); 
            logger.warn("线程池关闭被中断，强制关闭。");
        }
        logger.info("线程池已关闭.");

        // 销毁FaceEngine实例池中的所有实例
        while (!enginePool.isEmpty()) {
            ArcFaceEngine engine = enginePool.poll(); // 取出并移除
            if (engine != null) {
                engine.unInitSdk();
            }
        }
        logger.info("人脸识别引擎实例池已清空并销毁.");
    }

    public Future<FaceFeature> submitFeatureExtraction(File imageFile) {
        // 提交人脸特征提取任务到线程池
        // 任务会从引擎池中借用一个FaceEngine实例，使用完毕后归还
        Callable<FaceFeature> task = () -> {
            ArcFaceEngine currentEngine = null;
            try {
                // 从引擎池中获取一个引擎实例
                // 如果池中没有可用引擎，会阻塞直到有引擎可用
                currentEngine = enginePool.take();
                logger.debug("线程 {} 借用了一个FaceEngine实例。", Thread.currentThread().getName());

                // 人脸特征提取
                FaceFeature faceFeature = currentEngine.extractFaceFeatureFromImage(imageFile, ExtractType.REGISTER);

                return faceFeature;

            } catch (Exception e) {
                logger.error("人脸特征提取任务执行失败: {}", e.getMessage(), e);
                // 重新抛出异常，让Future能够捕获
                throw e; 
            } finally {
                if (currentEngine != null) {
                    // 确保引擎实例归还到池中
                    enginePool.put(currentEngine);
                    logger.debug("线程 {} 归还了FaceEngine实例。", Thread.currentThread().getName());
                }
            }
        };
        // 提交任务
        return executorService.submit(task); 
    }
}
