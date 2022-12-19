package com.atguigu.yygh.hosp.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * @author chenyj
 * @create 2022-12-03 18:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//@Document(value = "Actor")//@Document注解，标注在pojo类上，让它和mongodb数据库中的某个集合对应起来
public class Actor {
    //往mongodb中国添加文档的时候，一般这个id用String类型，不用Integer;
    //假如这个字段不叫id，又想让它和mongodb中的_id对应起来，就需要在这个字段上加一个 @Id注解
//    @Id //这个注解表示：当前属性和mongdb集合中的主键_id是对应的
//    private String pid; //pojo类属性和mongodb中集合的字段有一个对应关系，MongoTemplate规定：id对应mongodb中集合的_id字段

    private String id;
//    @Field(value = "actor_name")
    private String actorName;
    private Boolean gender;
    private Date birth;
}
