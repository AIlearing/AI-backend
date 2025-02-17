package com.zjy.projectAnswer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerQueryRequest;
import com.zjy.projectAnswer.model.entity.UserAnswer;
import com.zjy.projectAnswer.model.vo.UserAnswerVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户答题记录服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface UserAnswerService extends IService<UserAnswer> {

    /**
     * 校验数据
     *
     * @param UserAnswer
     * @param add 对创建的数据进行校验
     */
    void validUserAnswer(UserAnswer UserAnswer, boolean add);

    /**
     * 获取查询条件
     *
     * @param UserAnswerQueryRequest
     * @return
     */
    QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest UserAnswerQueryRequest);
    
    /**
     * 获取用户答题记录封装
     *
     * @param UserAnswer
     * @param request
     * @return
     */
    UserAnswerVO getUserAnswerVO(UserAnswer UserAnswer, HttpServletRequest request);

    /**
     * 分页获取用户答题记录封装
     *
     * @param UserAnswerPage
     * @param request
     * @return
     */
    Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> UserAnswerPage, HttpServletRequest request);
}
