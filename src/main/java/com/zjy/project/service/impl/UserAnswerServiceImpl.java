package com.zjy.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.project.model.entity.UserAnswer;
import com.zjy.project.service.UserAnswerService;
import com.zjy.project.mapper.UserAnswerMapper;
import org.springframework.stereotype.Service;

/**
* @author 31962
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service实现
* @createDate 2025-02-16 16:37:17
*/
@Service
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer>
    implements UserAnswerService{

}




