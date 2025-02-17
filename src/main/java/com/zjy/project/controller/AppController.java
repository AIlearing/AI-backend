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
import com.zjy.project.model.dto.app.AppAddRequest;
import com.zjy.project.model.dto.app.AppEditRequest;
import com.zjy.project.model.dto.app.AppQueryRequest;
import com.zjy.project.model.dto.app.AppUpdateRequest;
import com.zjy.project.model.entity.App;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.AppVO;
import com.zjy.project.service.AppService;
import com.zjy.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 应用接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/App")
@Slf4j
public class AppController {

    @Resource
    private AppService AppService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建应用
     *
     * @param AppAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest AppAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(AppAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        App App = new App();
        BeanUtils.copyProperties(AppAddRequest, App);
        // 数据校验
        AppService.validApp(App, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        App.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = AppService.save(App);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newAppId = App.getId();
        return ResultUtils.success(newAppId);
    }

    /**
     * 删除应用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = AppService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = AppService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新应用（仅管理员可用）
     *
     * @param AppUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest AppUpdateRequest) {
        if (AppUpdateRequest == null || AppUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        App App = new App();
        BeanUtils.copyProperties(AppUpdateRequest, App);
        // 数据校验
        AppService.validApp(App, false);
        // 判断是否存在
        long id = AppUpdateRequest.getId();
        App oldApp = AppService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = AppService.updateById(App);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取应用（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App App = AppService.getById(id);
        ThrowUtils.throwIf(App == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(AppService.getAppVO(App, request));
    }

    /**
     * 分页获取应用列表（仅管理员可用）
     *
     * @param AppQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest AppQueryRequest) {
        long current = AppQueryRequest.getCurrent();
        long size = AppQueryRequest.getPageSize();
        // 查询数据库
        Page<App> AppPage = AppService.page(new Page<>(current, size),
                AppService.getQueryWrapper(AppQueryRequest));
        return ResultUtils.success(AppPage);
    }

    /**
     * 分页获取应用列表（封装类）
     *
     * @param AppQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppVO>> listAppVOByPage(@RequestBody AppQueryRequest AppQueryRequest,
                                                               HttpServletRequest request) {
        long current = AppQueryRequest.getCurrent();
        long size = AppQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<App> AppPage = AppService.page(new Page<>(current, size),
                AppService.getQueryWrapper(AppQueryRequest));
        // 获取封装类
        return ResultUtils.success(AppService.getAppVOPage(AppPage, request));
    }

    /**
     * 分页获取当前登录用户创建的应用列表
     *
     * @param AppQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest AppQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(AppQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        AppQueryRequest.setUserId(loginUser.getId());
        long current = AppQueryRequest.getCurrent();
        long size = AppQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<App> AppPage = AppService.page(new Page<>(current, size),
                AppService.getQueryWrapper(AppQueryRequest));
        // 获取封装类
        return ResultUtils.success(AppService.getAppVOPage(AppPage, request));
    }

    /**
     * 编辑应用（给用户使用）
     *
     * @param AppEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editApp(@RequestBody AppEditRequest AppEditRequest, HttpServletRequest request) {
        if (AppEditRequest == null || AppEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        App App = new App();
        BeanUtils.copyProperties(AppEditRequest, App);
        // 数据校验
        AppService.validApp(App, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = AppEditRequest.getId();
        App oldApp = AppService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldApp.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = AppService.updateById(App);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
