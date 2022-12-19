package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.ServiceHospMainStarter;
import com.atguigu.yygh.hosp.bean.Actor;
import com.mongodb.client.result.DeleteResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author chenyj
 * @create 2022-12-03 18:05
 */
//创建测试类时：要求 "测试类“ 跟 ”主启动类“ 在同一个包下，否则，主启动类不会加载它；在同一个包下的话就不用加(classes = ServiceHospMainStarter.class)
@SpringBootTest//(classes = ServiceHospMainStarter.class) //加载主启动类
public class AppTest {

    /*
        使用MongoTemplate的三步：
        1. 引入starter依赖
        2. 配置mongodb的连接信息
        3. 在使用的地方直接注入MongoTemplate对象
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    /*  新增：可以使用insert、save方法
            insert只能做添加，不能做修改;insert可以批量添加数据
            save既可以做添加，也可以做修改;如果要使用save修改集合中某个字段的值，其它字段保持原来的值，必须先查询出来，然后修改，最后写回去
     */
    @Test
    public void testBatchInsert() {
        List<Actor> actors = new ArrayList<>();
        actors.add(new Actor("11", "Jerry", false, new Date()));
        actors.add(new Actor("15", "Tom", false, new Date()));
        actors.add(new Actor("13", "Andy", false, new Date()));
        actors.add(new Actor("14", "jack", false, new Date()));
        mongoTemplate.insert(actors, Actor.class);
    }

    @Test
    public void testModify() {
        Actor actor = mongoTemplate.findById("1", Actor.class);
        actor.setActorName("朱丽倩");
        mongoTemplate.save(actor);
    }


    @Test //注意：test有junit4,也有junit5，如果用junit4的话，默认不会加spring的容器，所以，要用junit5
    public void testInsert() {
//        mongoTemplate.insert(new Actor("1", "刘德华", true, new Date()));

        Actor actor = new Actor();
        actor.setId("2");
        actor.setActorName("郭富城");

        mongoTemplate.save(actor);
    }

    //删除
    @Test
    public void testDelete() {
//        Query query = new Query(Criteria.where("gender").is(false).and("actorName").is("张敏")); //与的关系

        //or 或的关系
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("_id").is("1"), Criteria.where("actorName").is("关之琳"));
        Query query = new Query(criteria);
        DeleteResult result = mongoTemplate.remove(query, Actor.class);
        System.out.println(result.getDeletedCount());
    }

    //修改：upsert:
//         updateFirst: 只修改符合条件的第一个文档
//         updateMulti: 修改符合条件的所有文档

    @Test
    public void testUpdate() {
//        Query query = new Query(Criteria.where("actorName").is("刘德华"));
//        Update update = new Update();
//        update.set("gender", true);
//        update.set("birth", new Date());
//        mongoTemplate.upsert(query, update, Actor.class);

//        Query query = new Query(Criteria.where("gender").is(true));
//        Update update = new Update();
//        update.set("age", 18);
//        mongoTemplate.updateFirst(query, update, Actor.class);

        Query query = new Query(Criteria.where("gender").is(true));
        Update update = new Update();
        update.set("age", 19);
        mongoTemplate.updateMulti(query, update, Actor.class);
    }

    @Test
    public void testQuery() {
        //精确查询
//        Query query = new Query(Criteria.where("age").is(19));

//        Query query = new Query(Criteria.where("actorName").regex(".*y.*")); // .*就相当于Mysql中模糊查询的 %

//        Pattern pattern = Pattern.compile(".*y.*");
        String regex = String.format("%s%s%s", ".*", "j", ".*");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Query query = new Query(Criteria.where("actorName").regex(pattern)); // .*就相当于Mysql中模糊查询的 %
        List<Actor> actors = mongoTemplate.find(query, Actor.class);
        for (Actor actor : actors) {
            System.out.println("actor = " + actor);
        }

        //        List<Actor> all = mongoTemplate.findAll(Actor.class);
//        for (Actor actor : all) {
//            System.out.println("actor = " + actor);
//        }

    }

    /*
        测试分页查询QueryPage
        注意：
            MongoTemplate没有提供分页查询的接口！
            要做分页查询，只需要提供两个数据：只要把总记录数 和 当前页对应的列表数据返回给前端，前端就可以使用element-ui的分页插件做分页显示
                1. total: 总记录数
                2. rows:  当前页对应的列表数据；
            只需要在后端把这两个数据给前端准备好，封装在一个Map<String, Object>里返回给前端即可。
     */
    @Test
    public void testQueryPage() {

        //1. 计算总记录数
        Query query = new Query(Criteria.where("gender").is(false));
        long total = mongoTemplate.count(query, Actor.class);

        //2. 计算当前页列表数据
        int pageNum = 1; //当前查第1页
        int size = 3; //每页显示3条
        List<Actor> actors = mongoTemplate.find(query.skip((pageNum-1)*size).limit(size), Actor.class);
        for (Actor actor : actors) {
            System.out.println("actor = " + actor);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        System.out.println("total = " + total);
        map.put("rows", actors);

        System.out.println(map);
    }

}
