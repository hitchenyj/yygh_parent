package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-02 14:23
 */
//官网不建议把这个Listener放入到Spring的容器中，所以，这里不要加@Component注解
public class StudentReadListener extends AnalysisEventListener<Student> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    /**
     * 缓存的数据
     */
    private List<Student> cachedDataList = new ArrayList<>();
    /**
     * 假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
     */

    //每解析excel文件中的一行数据，都会调用一次invoke方法，它会把excel中每一行的数据都封装到参数Student对象中
    @Override
    public void invoke(Student student, AnalysisContext analysisContext) {

        //批量操作
//        cachedDataList.add(student);
//        if (cachedDataList.size() >= 5) {
//            //baseMapper.batchInsert(cachedDataList);
//            // 存储完成清理 list
//            cachedDataList.clear();
//        }
        System.out.println(student);
    }

    //当解析excel文件某个sheet的标题的时候
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("标题为：" + headMap);
    }

    //当excel文件被解析完毕之后，走这个方法，所以这个方法可以做一些收尾工作，比如：关闭连接
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //在收尾之前，把不够一次批处理的数据，再插入数据库中
        //baseMapper.batchInsert(cachedDataList);
    }
}
