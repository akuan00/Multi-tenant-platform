package com.company.ai.platform.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.ai.platform.prompt.model.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
