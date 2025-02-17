package com.zjy.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.project.model.dto.User.UserQueryRequest;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface UserService extends IService<User> {

    /**
     * 校验数据
     *
     * @param User
     * @param add 对创建的数据进行校验
     */
    void validUser(User User, boolean add);

    /**
     * 获取查询条件
     *
     * @param UserQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest UserQueryRequest);
    
    /**
     * 获取用户封装
     *
     * @param User
     * @param request
     * @return
     */
    UserVO getUserVO(User User, HttpServletRequest request);

    /**
     * 分页获取用户封装
     *
     * @param UserPage
     * @param request
     * @return
     */
    Page<UserVO> getUserVOPage(Page<User> UserPage, HttpServletRequest request);
}
