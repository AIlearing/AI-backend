package com.zjy.ScoringResult.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.ScoringResult.common.ErrorCode;
import com.zjy.ScoringResult.constant.CommonConstant;
import com.zjy.ScoringResult.exception.ThrowUtils;
import com.zjy.ScoringResult.mapper.ScoringResultMapper;
import com.zjy.ScoringResult.model.dto.ScoringResult.ScoringResultQueryRequest;
import com.zjy.ScoringResult.model.entity.ScoringResult;
import com.zjy.ScoringResult.model.entity.ScoringResultFavour;
import com.zjy.ScoringResult.model.entity.ScoringResultThumb;
import com.zjy.ScoringResult.model.entity.User;
import com.zjy.ScoringResult.model.vo.ScoringResultVO;
import com.zjy.ScoringResult.model.vo.UserVO;
import com.zjy.ScoringResult.service.ScoringResultService;
import com.zjy.ScoringResult.service.UserService;
import com.zjy.ScoringResult.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评分结果服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult> implements ScoringResultService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param ScoringResult
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validScoringResult(ScoringResult ScoringResult, boolean add) {
        ThrowUtils.throwIf(ScoringResult == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = ScoringResult.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param ScoringResultQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest ScoringResultQueryRequest) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (ScoringResultQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = ScoringResultQueryRequest.getId();
        Long notId = ScoringResultQueryRequest.getNotId();
        String title = ScoringResultQueryRequest.getTitle();
        String content = ScoringResultQueryRequest.getContent();
        String searchText = ScoringResultQueryRequest.getSearchText();
        String sortField = ScoringResultQueryRequest.getSortField();
        String sortOrder = ScoringResultQueryRequest.getSortOrder();
        List<String> tagList = ScoringResultQueryRequest.getTags();
        Long userId = ScoringResultQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取评分结果封装
     *
     * @param ScoringResult
     * @param request
     * @return
     */
    @Override
    public ScoringResultVO getScoringResultVO(ScoringResult ScoringResult, HttpServletRequest request) {
        // 对象转封装类
        ScoringResultVO ScoringResultVO = ScoringResultVO.objToVo(ScoringResult);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = ScoringResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        ScoringResultVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long ScoringResultId = ScoringResult.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<ScoringResultThumb> ScoringResultThumbQueryWrapper = new QueryWrapper<>();
            ScoringResultThumbQueryWrapper.in("ScoringResultId", ScoringResultId);
            ScoringResultThumbQueryWrapper.eq("userId", loginUser.getId());
            ScoringResultThumb ScoringResultThumb = ScoringResultThumbMapper.selectOne(ScoringResultThumbQueryWrapper);
            ScoringResultVO.setHasThumb(ScoringResultThumb != null);
            // 获取收藏
            QueryWrapper<ScoringResultFavour> ScoringResultFavourQueryWrapper = new QueryWrapper<>();
            ScoringResultFavourQueryWrapper.in("ScoringResultId", ScoringResultId);
            ScoringResultFavourQueryWrapper.eq("userId", loginUser.getId());
            ScoringResultFavour ScoringResultFavour = ScoringResultFavourMapper.selectOne(ScoringResultFavourQueryWrapper);
            ScoringResultVO.setHasFavour(ScoringResultFavour != null);
        }
        // endregion

        return ScoringResultVO;
    }

    /**
     * 分页获取评分结果封装
     *
     * @param ScoringResultPage
     * @param request
     * @return
     */
    @Override
    public Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> ScoringResultPage, HttpServletRequest request) {
        List<ScoringResult> ScoringResultList = ScoringResultPage.getRecords();
        Page<ScoringResultVO> ScoringResultVOPage = new Page<>(ScoringResultPage.getCurrent(), ScoringResultPage.getSize(), ScoringResultPage.getTotal());
        if (CollUtil.isEmpty(ScoringResultList)) {
            return ScoringResultVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ScoringResultVO> ScoringResultVOList = ScoringResultList.stream().map(ScoringResult -> {
            return ScoringResultVO.objToVo(ScoringResult);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = ScoringResultList.stream().map(ScoringResult::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> ScoringResultIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> ScoringResultIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> ScoringResultIdSet = ScoringResultList.stream().map(ScoringResult::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<ScoringResultThumb> ScoringResultThumbQueryWrapper = new QueryWrapper<>();
            ScoringResultThumbQueryWrapper.in("ScoringResultId", ScoringResultIdSet);
            ScoringResultThumbQueryWrapper.eq("userId", loginUser.getId());
            List<ScoringResultThumb> ScoringResultScoringResultThumbList = ScoringResultThumbMapper.selectList(ScoringResultThumbQueryWrapper);
            ScoringResultScoringResultThumbList.forEach(ScoringResultScoringResultThumb -> ScoringResultIdHasThumbMap.put(ScoringResultScoringResultThumb.getScoringResultId(), true));
            // 获取收藏
            QueryWrapper<ScoringResultFavour> ScoringResultFavourQueryWrapper = new QueryWrapper<>();
            ScoringResultFavourQueryWrapper.in("ScoringResultId", ScoringResultIdSet);
            ScoringResultFavourQueryWrapper.eq("userId", loginUser.getId());
            List<ScoringResultFavour> ScoringResultFavourList = ScoringResultFavourMapper.selectList(ScoringResultFavourQueryWrapper);
            ScoringResultFavourList.forEach(ScoringResultFavour -> ScoringResultIdHasFavourMap.put(ScoringResultFavour.getScoringResultId(), true));
        }
        // 填充信息
        ScoringResultVOList.forEach(ScoringResultVO -> {
            Long userId = ScoringResultVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            ScoringResultVO.setUser(userService.getUserVO(user));
            ScoringResultVO.setHasThumb(ScoringResultIdHasThumbMap.getOrDefault(ScoringResultVO.getId(), false));
            ScoringResultVO.setHasFavour(ScoringResultIdHasFavourMap.getOrDefault(ScoringResultVO.getId(), false));
        });
        // endregion

        ScoringResultVOPage.setRecords(ScoringResultVOList);
        return ScoringResultVOPage;
    }

}
