package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-05 14:08
 */
//@Repository //持久化层接口上面不需要加 @Repository注解，也不需要再主启动类上设置mapperscan，只要继承MongoRepository就好使
public interface DepartmentRespository extends MongoRepository<Department, String> {
    //注意：只要这个方法时按照规定的格式定义的：以findBy开头，后面跟属性名，属性名首字母大写，有多个条件进行and或者or进行连接就可以了
    //这个方法就生效了，可以直接使用
    Department findByHoscodeAndDepcode(String hoscode, String depcode);

}
