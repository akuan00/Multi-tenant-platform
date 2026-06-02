package com.company.ai.platform.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultTools {

    @Tool("获取当前的日期和时间")
    public String getCurrentDateTime() {
        return java.time.LocalDateTime.now().toString();
    }

    @Tool("执行简单的数学计算，输入一个数学表达式并返回结果")
    public String calculate(String expression) {
        try {
            var engine = new javax.script.ScriptEngineManager()
                    .getEngineByName("js");
            if (engine == null) {
                return "计算引擎不可用";
            }
            Object result = engine.eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            log.warn("Calculation failed for expression: {}", expression, e);
            return "计算失败: " + e.getMessage();
        }
    }

    @Tool("生成一个随机的UUID")
    public String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
