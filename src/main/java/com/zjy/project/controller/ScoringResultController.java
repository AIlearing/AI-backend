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
import com.zjy.project.model.dto.scoringResult.ScoringResultAddRequest;
import com.zjy.project.model.dto.scoringResult.ScoringResultEditRequest;
import com.zjy.project.model.dto.scoringResult.ScoringResultQueryRequest;
import com.zjy.project.model.dto.scoringResult.ScoringResultUpdateRequest;
import com.zjy.project.model.entity.ScoringResult;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.ScoringResultVO;
import com.zjy.project.service.ScoringResultService;
import com.zjy.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评分结果接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/ScoringResult")
@Slf4j
public class ScoringResultController {

    @Resource
    private ScoringResultService ScoringResultService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建评分结果
     *
     * @param ScoringResultAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addScoringResult(@RequestBody ScoringResultAddRequest ScoringResultAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ScoringResultAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        ScoringResult ScoringResult = new ScoringResult();
        BeanUtils.copyProperties(ScoringResultAddRequest, ScoringResult);
        // 数据校验
        ScoringResultService.validScoringResult(ScoringResult, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        ScoringResult.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = ScoringResultService.save(ScoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newScoringResultId = ScoringResult.getId();
        return ResultUtils.success(newScoringResultId);
    }

    /**
     * 删除评分结果
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteScoringResult(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ScoringResult oldScoringResult = ScoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldScoringResult.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = ScoringResultService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新评分结果（仅管理员可用）
     *
     * @param ScoringResultUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateScoringResult(@RequestBody ScoringResultUpdateRequest ScoringResultUpdateRequest) {
        if (ScoringResultUpdateRequest == null || ScoringResultUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        ScoringResult ScoringResult = new ScoringResult();
        BeanUtils.copyProperties(ScoringResultUpdateRequest, ScoringResult);
        // 数据校验
        ScoringResultService.validScoringResult(ScoringResult, false);
        // 判断是否存在
        long id = ScoringResultUpdateRequest.getId();
        ScoringResult oldScoringResult = ScoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = ScoringResultService.updateById(ScoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取评分结果（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ScoringResultVO> getScoringResultVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        ScoringResult ScoringResult = ScoringResultService.getById(id);
        ThrowUtils.throwIf(ScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(ScoringResultService.getScoringResultVO(ScoringResult, request));
    }

    /**
     * 分页获取评分结果列表（仅管理员可用）
     *
     * @param ScoringResultQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ScoringResult>> listScoringResultByPage(@RequestBody ScoringResultQueryRequest ScoringResultQueryRequest) {
        long current = ScoringResultQueryRequest.getCurrent();
        long size = ScoringResultQueryRequest.getPageSize();
        // 查询数据库
        Page<ScoringResult> ScoringResultPage = ScoringResultService.page(new Page<>(current, size),
                ScoringResultService.getQueryWrapper(ScoringResultQueryRequest));
        return ResultUtils.success(ScoringResultPage);
    }

    /**
     * 分页获取评分结果列表（封装类）
     *
     * @param ScoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listScoringResultVOByPage(@RequestBody ScoringResultQueryRequest ScoringResultQueryRequest,
                                                               HttpServletRequest request) {
        long current = ScoringResultQueryRequest.getCurrent();
        long size = ScoringResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<ScoringResult> ScoringResultPage = ScoringResultService.page(new Page<>(current, size),
                ScoringResultService.getQueryWrapper(ScoringResultQueryRequest));
        // 获取封装类
        return ResultUtils.success(ScoringResultService.getScoringResultVOPage(ScoringResultPage, request));
    }

    /**
     * 分页获取当前登录用户创建的评分结果列表
     *
     * @param ScoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listMyScoringResultVOByPage(@RequestBody ScoringResultQueryRequest ScoringResultQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(ScoringResultQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        ScoringResultQueryRequest.setUserId(loginUser.getId());
        long current = ScoringResultQueryRequest.getCurrent();
        long size = ScoringResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<ScoringResult> ScoringResultPage = ScoringResultService.page(new Page<>(current, size),
                ScoringResultService.getQueryWrapper(ScoringResultQueryRequest));
        // 获取封装类
        return ResultUtils.success(ScoringResultService.getScoringResultVOPage(ScoringResultPage, request));
    }

    /**
     * 编辑评分结果（给用户使用）
     *
     * @param ScoringResultEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editScoringResult(@RequestBody ScoringResultEditRequest ScoringResultEditRequest, HttpServletRequest request) {
        if (ScoringResultEditRequest == null || ScoringResultEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        ScoringResult ScoringResult = new ScoringResult();
        BeanUtils.copyProperties(ScoringResultEditRequest, ScoringResult);
        // 数据校验
        ScoringResultService.validScoringResult(ScoringResult, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = ScoringResultEditRequest.getId();
        ScoringResult oldScoringResult = ScoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldScoringResult.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = ScoringResultService.updateById(ScoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
