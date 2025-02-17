package com.zjy.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.App;
import com.zjy.project.service.AppService;
import com.zjy.project.mapper.AppMapper;
import org.springframework.stereotype.Service;

/**
* @author 31962
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-02-16 16:37:17
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

}




