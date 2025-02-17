package com.zjy.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjy.project.annotation.AuthCheck;
import com.zjy.project.common.BaseResponse;
import com.zjy.project.common.DeleteRequest;
import com.zjy.project.common.ErrorCode;
import com.zjy.project.common.ResultUtils;
import com.zjy.project.constant.UserConstant;
import com.zjy.project.exception.BusinessException;
import com.zjy.project.exception.ThrowUtils;
import com.zjy.project.model.dto.Question.QuestionAddRequest;
import com.zjy.project.model.dto.Question.QuestionEditRequest;
import com.zjy.project.model.dto.Question.QuestionQueryRequest;
import com.zjy.project.model.dto.Question.QuestionUpdateRequest;
import com.zjy.project.model.entity.Question;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.QuestionVO;
import com.zjy.project.service.QuestionService;
import com.zjy.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/Question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService QuestionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param QuestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest QuestionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionAddRequest, Question);
        // 数据校验
        QuestionService.validQuestion(Question, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        Question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = QuestionService.save(Question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = Question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = QuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = QuestionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param QuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest QuestionUpdateRequest) {
        if (QuestionUpdateRequest == null || QuestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionUpdateRequest, Question);
        // 数据校验
        QuestionService.validQuestion(Question, false);
        // 判断是否存在
        long id = QuestionUpdateRequest.getId();
        Question oldQuestion = QuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = QuestionService.updateById(Question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question Question = QuestionService.getById(id);
        ThrowUtils.throwIf(Question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(QuestionService.getQuestionVO(Question, request));
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param QuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest) {
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> QuestionPage = QuestionService.page(new Page<>(current, size),
                QuestionService.getQueryWrapper(QuestionQueryRequest));
        return ResultUtils.success(QuestionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param QuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest,
                                                               HttpServletRequest request) {
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> QuestionPage = QuestionService.page(new Page<>(current, size),
                QuestionService.getQueryWrapper(QuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionService.getQuestionVOPage(QuestionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param QuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        QuestionQueryRequest.setUserId(loginUser.getId());
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> QuestionPage = QuestionService.page(new Page<>(current, size),
                QuestionService.getQueryWrapper(QuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionService.getQuestionVOPage(QuestionPage, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param QuestionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest QuestionEditRequest, HttpServletRequest request) {
        if (QuestionEditRequest == null || QuestionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionEditRequest, Question);
        // 数据校验
        QuestionService.validQuestion(Question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = QuestionEditRequest.getId();
        Question oldQuestion = QuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = QuestionService.updateById(Question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
