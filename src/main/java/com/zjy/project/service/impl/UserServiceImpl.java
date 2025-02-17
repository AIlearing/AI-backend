package com.zjy.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.project.model.entity.User;
import com.zjy.project.service.UserService;
import com.zjy.project.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 31962
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-16 16:37:17
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




