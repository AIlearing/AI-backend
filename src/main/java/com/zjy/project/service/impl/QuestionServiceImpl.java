package com.zjy.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Question;
import com.zjy.project.service.QuestionService;
import com.zjy.project.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author 31962
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-02-16 16:37:17
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




