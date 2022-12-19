package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-02 22:04
 */
public class DictEevoListener extends AnalysisEventListener<DictEeVo> {

    /*
     为了把解析处理的DictEeVo对象插入到数据表里，这里需要一个DictMapper，以前在Controller或者Service层需要的话直接注入即可。
     但是，在这里不能注入！因为这里的DictEevoListener不能交给Spring容器去管理，这个DictEevoListener上面不能加 @Component 注解，
     不加 @Component 注解，这里是没法注入 DictMapper 的，因为如果这个DictEevoListener类不在容器里，它就没办法知道它的DictMapper 属性去容器里找
     这里可以通过构造器的方式给它传进来，同时在 DictEevoListener 类里单独定义一个属性：dictMapper
     */
    private DictMapper dictMapper;
    public DictEevoListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        // 使用 BeanUtils.copyProperties 把 DictEeVo 对象转换成 Dict 对象
        BeanUtils.copyProperties(dictEeVo, dict);

        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", dictEeVo.getId());
        Integer count = dictMapper.selectCount(queryWrapper);
        if (count > 0) {
            //如果查询在表中已经有了，就做更新操作
            dictMapper.updateById(dict);
        } else {
            //没有，再做添加操作
            dictMapper.insert(dict);
        }
    }

    // 解析表头的，这里不需要
//    @Override
//    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
//        super.invokeHeadMap(headMap, context);
//    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
