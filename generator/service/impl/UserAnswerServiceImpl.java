package com.zjy.projectAnswer.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.projectAnswer.common.ErrorCode;
import com.zjy.projectAnswer.constant.CommonConstant;
import com.zjy.projectAnswer.exception.ThrowUtils;
import com.zjy.projectAnswer.mapper.UserAnswerMapper;
import com.zjy.projectAnswer.model.dto.UserAnswer.UserAnswerQueryRequest;
import com.zjy.projectAnswer.model.entity.UserAnswer;
import com.zjy.projectAnswer.model.entity.UserAnswerFavour;
import com.zjy.projectAnswer.model.entity.UserAnswerThumb;
import com.zjy.projectAnswer.model.entity.User;
import com.zjy.projectAnswer.model.vo.UserAnswerVO;
import com.zjy.projectAnswer.model.vo.UserVO;
import com.zjy.projectAnswer.service.UserAnswerService;
import com.zjy.projectAnswer.service.UserService;
import com.zjy.projectAnswer.utils.SqlUtils;
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
 * 用户答题记录服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param UserAnswer
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer UserAnswer, boolean add) {
        ThrowUtils.throwIf(UserAnswer == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = UserAnswer.getTitle();
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
     * @param UserAnswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest UserAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (UserAnswerQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = UserAnswerQueryRequest.getId();
        Long notId = UserAnswerQueryRequest.getNotId();
        String title = UserAnswerQueryRequest.getTitle();
        String content = UserAnswerQueryRequest.getContent();
        String searchText = UserAnswerQueryRequest.getSearchText();
        String sortField = UserAnswerQueryRequest.getSortField();
        String sortOrder = UserAnswerQueryRequest.getSortOrder();
        List<String> tagList = UserAnswerQueryRequest.getTags();
        Long userId = UserAnswerQueryRequest.getUserId();
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
     * 获取用户答题记录封装
     *
     * @param UserAnswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer UserAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO UserAnswerVO = UserAnswerVO.objToVo(UserAnswer);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = UserAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        UserAnswerVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long UserAnswerId = UserAnswer.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<UserAnswerThumb> UserAnswerThumbQueryWrapper = new QueryWrapper<>();
            UserAnswerThumbQueryWrapper.in("UserAnswerId", UserAnswerId);
            UserAnswerThumbQueryWrapper.eq("userId", loginUser.getId());
            UserAnswerThumb UserAnswerThumb = UserAnswerThumbMapper.selectOne(UserAnswerThumbQueryWrapper);
            UserAnswerVO.setHasThumb(UserAnswerThumb != null);
            // 获取收藏
            QueryWrapper<UserAnswerFavour> UserAnswerFavourQueryWrapper = new QueryWrapper<>();
            UserAnswerFavourQueryWrapper.in("UserAnswerId", UserAnswerId);
            UserAnswerFavourQueryWrapper.eq("userId", loginUser.getId());
            UserAnswerFavour UserAnswerFavour = UserAnswerFavourMapper.selectOne(UserAnswerFavourQueryWrapper);
            UserAnswerVO.setHasFavour(UserAnswerFavour != null);
        }
        // endregion

        return UserAnswerVO;
    }

    /**
     * 分页获取用户答题记录封装
     *
     * @param UserAnswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> UserAnswerPage, HttpServletRequest request) {
        List<UserAnswer> UserAnswerList = UserAnswerPage.getRecords();
        Page<UserAnswerVO> UserAnswerVOPage = new Page<>(UserAnswerPage.getCurrent(), UserAnswerPage.getSize(), UserAnswerPage.getTotal());
        if (CollUtil.isEmpty(UserAnswerList)) {
            return UserAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> UserAnswerVOList = UserAnswerList.stream().map(UserAnswer -> {
            return UserAnswerVO.objToVo(UserAnswer);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = UserAnswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> UserAnswerIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> UserAnswerIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> UserAnswerIdSet = UserAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<UserAnswerThumb> UserAnswerThumbQueryWrapper = new QueryWrapper<>();
            UserAnswerThumbQueryWrapper.in("UserAnswerId", UserAnswerIdSet);
            UserAnswerThumbQueryWrapper.eq("userId", loginUser.getId());
            List<UserAnswerThumb> UserAnswerUserAnswerThumbList = UserAnswerThumbMapper.selectList(UserAnswerThumbQueryWrapper);
            UserAnswerUserAnswerThumbList.forEach(UserAnswerUserAnswerThumb -> UserAnswerIdHasThumbMap.put(UserAnswerUserAnswerThumb.getUserAnswerId(), true));
            // 获取收藏
            QueryWrapper<UserAnswerFavour> UserAnswerFavourQueryWrapper = new QueryWrapper<>();
            UserAnswerFavourQueryWrapper.in("UserAnswerId", UserAnswerIdSet);
            UserAnswerFavourQueryWrapper.eq("userId", loginUser.getId());
            List<UserAnswerFavour> UserAnswerFavourList = UserAnswerFavourMapper.selectList(UserAnswerFavourQueryWrapper);
            UserAnswerFavourList.forEach(UserAnswerFavour -> UserAnswerIdHasFavourMap.put(UserAnswerFavour.getUserAnswerId(), true));
        }
        // 填充信息
        UserAnswerVOList.forEach(UserAnswerVO -> {
            Long userId = UserAnswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            UserAnswerVO.setUser(userService.getUserVO(user));
            UserAnswerVO.setHasThumb(UserAnswerIdHasThumbMap.getOrDefault(UserAnswerVO.getId(), false));
            UserAnswerVO.setHasFavour(UserAnswerIdHasFavourMap.getOrDefault(UserAnswerVO.getId(), false));
        });
        // endregion

        UserAnswerVOPage.setRecords(UserAnswerVOList);
        return UserAnswerVOPage;
    }

}
