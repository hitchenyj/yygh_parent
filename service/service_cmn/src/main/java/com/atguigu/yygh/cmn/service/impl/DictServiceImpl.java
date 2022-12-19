package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictEevoListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-11-30
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /*
        Springcache:底层最终封装的还是Redis，也可以用其它的比如：memcache
        使用Springcache的四步如下：
        1. 导入依赖：导入对应的starter依赖
        2. 以前不使用springcache时，要配置applicatio.yml文件，配置：redis连接信息
        3. 以前不使用Springcache的话，除了以上两步；还要自己在service层注入RedisTemplate，然后使用编码的方式对redis进行操作；
           现在有了Springcache，第3步，就要求在配置类中提供一个cacheManager对象，不用记，直接把配置类那个拿过来就行了，
                                    同时，要求在这个配置类上标记一个：@EnableCaching注解，开启缓存支持
        4. 为了使用redis缓存，使用这几个注解： 在查询方法上@Cacheable、@CachePut、@CacheEvict
                            注意：这三个注解在使用时必须指定value属性值，代表redis中的key，值就是这个方法的返回值
                            如果方法有参数的话，它也会在key的后面用::拼上参数作为redis中的key：key::param
     */

    //这里调持久化层：DictService调DictMapper，在这里就直接使用BaseMapper就可以了，
    // BaseMapper也不用注入了，它在ServiceImpl已经注入过了，可以直接使用
    @Override
    @Cacheable(value = "abc", key = "'selectIndexList'+#pid")
    public List<Dict> getChildListByPid(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);
        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict : dicts) {
            dict.setHasChildren(isHasChildren(dict.getId()));
        }

        return dicts;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException {
        List<Dict> list = baseMapper.selectList(null);
        List<DictEeVo> dictEeVoList = new ArrayList<>(list.size());
        for (Dict dict : list) {
            DictEeVo dictEeVo = new DictEeVo();
            //这里使用Spring里的BeanUtils工具类，它里面有一个copyProperties方法，它可以把源对象的属性值复制到目标对象的属性上
            BeanUtils.copyProperties(dict, dictEeVo);//copyProperties要求源对象dict和目标对象dictEevo，对应的属性名必须相同
            dictEeVoList.add(dictEeVo);
        }

        //这里不能使用绝对路径，如果使用这样的绝对路径，excel文件就下载到了服务器上了；
        //EasyExcel.write("C:\\Users\\chenyj\\Desktop\\hello.xlsx", DictEeVo.class).sheet("字典数据").doWrite(dictEeVoList);

        //另外，在做下载时还应该设置一些响应头信息，直接复制官网的即可
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//设置下载的文件类型：excel文件
        response.setCharacterEncoding("utf-8"); //设置响应编码，这个编码其实没啥用
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("字典文件", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        /*
        这里应该使用基于流的方式，而且写出excel的时候，用的是字节流，不是字符流，字符流是写纯文本数据的
        借助于字节流: response.getOutputStream()，有异常就往外抛，抛到Controller层后可以try-catch，或者继续往上抛，因为已经做过全局异常处理
        这里借助 response.getOutputStream()响应流就传给浏览器了，它通过响应报文把这个字节流传输给浏览器端了，在浏览器端让它下载下来。
         */
        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("学生列表1").doWrite(dictEeVoList);
    }

    @Override
    @CacheEvict(value = "abc", allEntries = true)
    public void upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictEevoListener(baseMapper)).sheet(0).doRead();
    }

    @Override
    public String getNameByValue(Long value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("value", value);
        Dict dict = baseMapper.selectOne(queryWrapper);
        if (dict != null) {
            return dict.getName();
        }
        return null;
    }

    @Override
    public String getNameByDictCodeAndValue(String dictCode, Long value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);

        QueryWrapper<Dict> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("parent_id", dict.getId());
        queryWrapper2.eq("value", value);
        Dict dict2 = baseMapper.selectOne(queryWrapper2);
        if (dict2 != null) {
            return dict2.getName();
        }
        return null;
    }

    private Boolean isHasChildren(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }
}
