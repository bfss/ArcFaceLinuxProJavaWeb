package com.bfss.arcfacelinuxprojavaweb.repository;

import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArcFaceInfoRepository extends JpaRepository<ArcFaceInfoEntity, Long> {
    // JPA仓库接口，由于使用JPA内置方法，此处留空即可
    
}
