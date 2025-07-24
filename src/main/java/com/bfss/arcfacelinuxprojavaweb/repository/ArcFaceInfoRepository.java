package com.bfss.arcfacelinuxprojavaweb.repository;

import com.bfss.arcfacelinuxprojavaweb.entity.ArcFaceInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArcFaceInfoRepository extends JpaRepository<ArcFaceInfoEntity, Long> {
    
}
