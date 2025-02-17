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
import com.zjy.project.model.dto.user.UserAddRequest;
import com.zjy.project.model.dto.user.UserQueryRequest;
import com.zjy.project.model.dto.user.UserUpdateRequest;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.UserVO;
import com.zjy.project.service.UserService;
import com.zjy.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/User")
@Slf4j
public class UserController {

    @Resource
    private UserService UserService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户
     *
     * @param UserAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest UserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(UserAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        User User = new User();
        BeanUtils.copyProperties(UserAddRequest, User);
        // 数据校验
        UserService.validUser(User, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        User.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = UserService.save(User);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserId = User.getId();
        return ResultUtils.success(newUserId);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        User oldUser = UserService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUser.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = UserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户（仅管理员可用）
     *
     * @param UserUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest UserUpdateRequest) {
        if (UserUpdateRequest == null || UserUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        User User = new User();
        BeanUtils.copyProperties(UserUpdateRequest, User);
        // 数据校验
        UserService.validUser(User, false);
        // 判断是否存在
        long id = UserUpdateRequest.getId();
        User oldUser = UserService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = UserService.updateById(User);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        User User = UserService.getById(id);
        ThrowUtils.throwIf(User == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(UserService.getUserVO(User, request));
    }

    /**
     * 分页获取用户列表（仅管理员可用）
     *
     * @param UserQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest UserQueryRequest) {
        long current = UserQueryRequest.getCurrent();
        long size = UserQueryRequest.getPageSize();
        // 查询数据库
        Page<User> UserPage = UserService.page(new Page<>(current, size),
                UserService.getQueryWrapper(UserQueryRequest));
        return ResultUtils.success(UserPage);
    }

    /**
     * 分页获取用户列表（封装类）
     *
     * @param UserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest UserQueryRequest,
                                                               HttpServletRequest request) {
        long current = UserQueryRequest.getCurrent();
        long size = UserQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<User> UserPage = UserService.page(new Page<>(current, size),
                UserService.getQueryWrapper(UserQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserService.getUserVOPage(UserPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户列表
     *
     * @param UserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserVO>> listMyUserVOByPage(@RequestBody UserQueryRequest UserQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(UserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        UserQueryRequest.setUserId(loginUser.getId());
        long current = UserQueryRequest.getCurrent();
        long size = UserQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<User> UserPage = UserService.page(new Page<>(current, size),
                UserService.getQueryWrapper(UserQueryRequest));
        // 获取封装类
        return ResultUtils.success(UserService.getUserVOPage(UserPage, request));
    }

    /**
     * 编辑用户（给用户使用）
     *
     * @param UserEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUser(@RequestBody UserEditRequest UserEditRequest, HttpServletRequest request) {
        if (UserEditRequest == null || UserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        User User = new User();
        BeanUtils.copyProperties(UserEditRequest, User);
        // 数据校验
        UserService.validUser(User, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = UserEditRequest.getId();
        User oldUser = UserService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUser.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = UserService.updateById(User);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
