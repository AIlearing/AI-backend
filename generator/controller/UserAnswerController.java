package com.zjy.projectAnswer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjy.projectAnswer.annotation.AuthCheck;
import com.zjy.projectAnswer.common.BaseResponse;
import com.zjy.projectAnswer.common.DeleteRequest;
import com.zjy.projectAnswer.common.ErrorCode;
import com.zjy.projectAnswer.common.ResultUtils;
import com.zjy.projectAnswer.constant.UserConstant;
import com.zjy.projectAnswer.exception.BusinessException;
import com.zjy.projectAnswer.exception.ThrowUtils;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerAddRequest;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerEditRequest;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerQueryRequest;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerUpdateRequest;
import com.zjy.projectAnswer.model.entity.UserAnswer;
import com.zjy.projectAnswer.model.entity.User;
import com.zjy.projectAnswer.model.vo.UserAnswerVO;
import com.zjy.projectAnswer.service.UserAnswerService;
import com.zjy.projectAnswer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户答题记录接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/UserAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService UserAnswerService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户答题记录
     *
     * @param UserAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest UserAnswerAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(UserAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer UserAnswer = new UserAnswer();
        BeanUtils.copyProperties(UserAnswerAddRequest, UserAnswer);
        // 数据校验
        UserAnswerService.validUserAnswer(UserAnswer, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        UserAnswer.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = UserAnswerService.save(UserAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserAnswerId = UserAnswer.getId();
        return ResultUtils.success(newUserAnswerId);
    }

    /**
     * 删除用户答题记录
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = UserAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = UserAnswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答题记录（仅管理员可用）
     *
     * @param UserAnswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest UserAnswerUpdateRequest) {
        if (UserAnswerUpdateRequest == null || UserAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer UserAnswer = new UserAnswer();
        BeanUtils.copyProperties(UserAnswerUpdateRequest, UserAnswer);
        // 数据校验
        UserAnswerService.validUserAnswer(UserAnswer, false);
        // 判断是否存在
        long id = UserAnswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = UserAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = UserAnswerService.updateById(UserAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答题记录（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer UserAnswer = UserAnswerService.getById(id);
        ThrowUtils.throwIf(UserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(UserAnswerService.getUserAnswerVO(UserAnswer, request));
    }

    /**
     * 分页获取用户答题记录列表（仅管理员可用）
     *
     * @param UserAnswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest) {
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = UserAnswerService.page(new Page<>(current, size),
                UserAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        return ResultUtils.success(UserAnswerPage);
    }

    /**
     * 分页获取用户答题记录列表（封装类）
     *
     * @param UserAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest,
                                                               HttpServletRequest request) {
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = UserAnswerService.page(new Page<>(current, size),
                UserAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserAnswerService.getUserAnswerVOPage(UserAnswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答题记录列表
     *
     * @param UserAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(UserAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        UserAnswerQueryRequest.setUserId(loginUser.getId());
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = UserAnswerService.page(new Page<>(current, size),
                UserAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserAnswerService.getUserAnswerVOPage(UserAnswerPage, request));
    }

    /**
     * 编辑用户答题记录（给用户使用）
     *
     * @param UserAnswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest UserAnswerEditRequest, HttpServletRequest request) {
        if (UserAnswerEditRequest == null || UserAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer UserAnswer = new UserAnswer();
        BeanUtils.copyProperties(UserAnswerEditRequest, UserAnswer);
        // 数据校验
        UserAnswerService.validUserAnswer(UserAnswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = UserAnswerEditRequest.getId();
        UserAnswer oldUserAnswer = UserAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = UserAnswerService.updateById(UserAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
