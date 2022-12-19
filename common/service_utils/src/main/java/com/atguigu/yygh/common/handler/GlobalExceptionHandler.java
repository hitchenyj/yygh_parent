package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * @author chenyj
 * @create 2022-11-26 18:07
 */
//@ControllerAdvice //凡是由@ControllerAdvice注解标记的类都表示全局异常处理类
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
        @ExceptionHandler注解的value属性指定它可以处理哪种类型的异常，它是一个字节码数组类型的:
        Class<? extends Throwable>[] value() default {};
        是一个继承了Throwable这个异常类的字节码数组；
        数组在注解中是用大括号{}来表示的；
        假如想让它处理所有类型的异常，那就用Exception.class

        在handleException中可以使用参数接收controller层抛出的额异常;

        然后，为了让handleException函数返回的数据是Json数据，所以，可以在handleException上面加@ResponseBody注解;
        当然，@ResponseBody 和 @ControllerAdvice 注解，这两个可以合起来，合成 @RestControllerAdvice 注解
        @RestController表示当前类里的方法返回的数据都是json数据；
        所以，在类的方法上就不用再加@ResponseBody注解了

        然后，有异常的话，正常情况下，这个异常要记录到日志文件里面，后面再加

        在实际开发中，异常不会直接输出到控制台，一般是保存到日志文件里，方便排查错误

     */
//    @ResponseBody
    @ExceptionHandler(Exception.class)
    public R handleException(Exception ex) { //粒度有点大
        ex.printStackTrace(); //输出异常，日志文件
        log.error(ex.getMessage());
        return R.error().message(ex.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public R handleSqlException(SQLException ex) { //细粒度的异常处理
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error().message("SQL异常");
    }

    @ExceptionHandler(ArithmeticException.class)
    public R handleArithmeticException(ArithmeticException ex) { //细粒度的异常处理
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error().message("算数异常");
    }

    @ExceptionHandler(RuntimeException.class)
    public R handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error().message("运行时异常");
    }

    @ExceptionHandler(YyghException.class)
    public R handleYyghException(YyghException ex) {
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error().message(ex.getMessage()).code(ex.getCode());
    }
}
