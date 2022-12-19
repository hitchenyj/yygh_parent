package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-05 8:29
 */
public interface HospRespository extends MongoRepository<Hospital, String> {
    Hospital findByHoscode(String hoscode);

    List<Hospital> findByHosnameLike(String name);
}
