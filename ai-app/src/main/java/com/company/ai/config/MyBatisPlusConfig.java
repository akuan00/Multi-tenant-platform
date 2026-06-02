package com.company.ai.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.company.ai.**.mapper")
public class MyBatisPlusConfig {
}
