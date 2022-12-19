package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.bean.Actor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-04 12:00
 */
//注意这个接口上面不需要加 @Repository 注解（跟es一样，es也不需要加）
public interface ActorRepository extends MongoRepository<Actor, String> {

    //在持久化层自定义查询方法: 自定义查询方法必须以findBy开头，后面跟pojo类的属性名，把属性名的首字母大写
    public List<Actor> findByActorName(String name);

    public List<Actor> findByActorNameLike(String name);

    public List<Actor> findByActorNameLikeAndGender(String name, Boolean gender);
}
