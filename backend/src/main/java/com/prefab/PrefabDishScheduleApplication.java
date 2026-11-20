package com.prefab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 生鲜即时配送调度系统启动类
 *
 * @author prefab
 * @since 2024
 */
@SpringBootApplication
@MapperScan("com.prefab.mapper")
public class PrefabDishScheduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrefabDishScheduleApplication.class, args);
        System.out.println("============================================");
        System.out.println("   生鲜即时配送调度系统启动成功！");
        System.out.println("   接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("============================================");
    }
}
