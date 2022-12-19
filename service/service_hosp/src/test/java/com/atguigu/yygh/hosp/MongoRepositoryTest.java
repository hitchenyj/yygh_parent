package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.bean.Actor;
import com.atguigu.yygh.hosp.repository.ActorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author chenyj
 * @create 2022-12-04 12:10
 */
@SpringBootTest
public class MongoRepositoryTest {

    @Autowired
    private ActorRepository actorRepository;

    /*
        测试添加Insert
     */
    @Test
    public void testInsert() {
//        actorRepository.insert(new Actor("16", "小振中", true, new Date()));
//        actorRepository.save(new Actor("17", "小文博", true, new Date()));

        List<Actor> actorList = new ArrayList<>();
        actorList.add(new Actor("30", "周润发", true, new Date()));
        actorList.add(new Actor("31", "周星驰", true, new Date()));
        actorList.add(new Actor("32", "李连杰", true, new Date()));

//        actorRepository.insert(actorList);
        actorRepository.saveAll(actorList);
    }

    /*
        测试Delete
     */
    @Test
    public void testDelete() {
        actorRepository.deleteById("32");
    }

    /*
        测试Query
     */
    @Test
    public void testQuery() {
        //根据id查
//        Actor actor = actorRepository.findById("30").get();
//        System.out.println("actor = " + actor);

        Actor actor = new Actor();
//        actor.setActorName("成龙");
        actor.setGender(true);
        //import org.springframework.data.domain.Example;
        Example<Actor> example = Example.of(actor);
        List<Actor> actors = actorRepository.findAll(example);
        for (Actor actor1 : actors) {
            System.out.println(actor1);
        }
    }

    /*
        测试模糊Query1
     */
    @Test
    public void testQuery1() {

        Actor actor = new Actor();
        actor.setActorName("j");
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
//                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withMatcher("actorName", ExampleMatcher.GenericPropertyMatchers.startsWith())  //name字段模糊匹配
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        Example<Actor> example = Example.of(actor, matcher);
        List<Actor> actors = actorRepository.findAll(example);
        for (Actor actor1 : actors) {
            System.out.println(actor1);
        }
    }

    /*
        测试Page
     */
    @Test
    public void testPage() {

        int pageNum = 1;
        int pageSize = 3;

        Actor actor = new Actor();
        actor.setGender(true);
        Example<Actor> example = Example.of(actor);

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("id").descending());
        Page<Actor> page = actorRepository.findAll(example, pageable);
        System.out.println("总记录数: " + page.getTotalElements());
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("当前页列表数据: ");
        for (Actor actor1 : page.getContent()) {
            System.out.println(actor1);
        }
    }
    
    /*
        测试SelfDefinitionMethod
     */
    @Test
    public void testSelfDefinitionMethod() {
//        List<Actor> actors = actorRepository.findByActorName("龙");
//        List<Actor> actors = actorRepository.findByActorNameLike("龙");
        List<Actor> actors = actorRepository.findByActorNameLikeAndGender("龙", true);
        for (Actor actor : actors) {
            System.out.println(actor);
        }
    }
}
