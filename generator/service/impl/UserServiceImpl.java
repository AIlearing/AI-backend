package com.zjy.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.project.common.ErrorCode;
import com.zjy.project.constant.CommonConstant;
import com.zjy.project.exception.ThrowUtils;
import com.zjy.project.mapper.UserMapper;
import com.zjy.project.model.dto.User.UserQueryRequest;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.entity.UserFavour;
import com.zjy.project.model.entity.UserThumb;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.UserVO;
import com.zjy.project.model.vo.UserVO;
import com.zjy.project.service.UserService;
import com.zjy.project.service.UserService;
import com.zjy.project.utils.SqlUtils;
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
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param User
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUser(User User, boolean add) {
        ThrowUtils.throwIf(User == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = User.getTitle();
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
     * @param UserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest UserQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (UserQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = UserQueryRequest.getId();
        Long notId = UserQueryRequest.getNotId();
        String title = UserQueryRequest.getTitle();
        String content = UserQueryRequest.getContent();
        String searchText = UserQueryRequest.getSearchText();
        String sortField = UserQueryRequest.getSortField();
        String sortOrder = UserQueryRequest.getSortOrder();
        List<String> tagList = UserQueryRequest.getTags();
        Long userId = UserQueryRequest.getUserId();
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
     * 获取用户封装
     *
     * @param User
     * @param request
     * @return
     */
    @Override
    public UserVO getUserVO(User User, HttpServletRequest request) {
        // 对象转封装类
        UserVO UserVO = UserVO.objToVo(User);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = User.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        UserVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long UserId = User.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<UserThumb> UserThumbQueryWrapper = new QueryWrapper<>();
            UserThumbQueryWrapper.in("UserId", UserId);
            UserThumbQueryWrapper.eq("userId", loginUser.getId());
            UserThumb UserThumb = UserThumbMapper.selectOne(UserThumbQueryWrapper);
            UserVO.setHasThumb(UserThumb != null);
            // 获取收藏
            QueryWrapper<UserFavour> UserFavourQueryWrapper = new QueryWrapper<>();
            UserFavourQueryWrapper.in("UserId", UserId);
            UserFavourQueryWrapper.eq("userId", loginUser.getId());
            UserFavour UserFavour = UserFavourMapper.selectOne(UserFavourQueryWrapper);
            UserVO.setHasFavour(UserFavour != null);
        }
        // endregion

        return UserVO;
    }

    /**
     * 分页获取用户封装
     *
     * @param UserPage
     * @param request
     * @return
     */
    @Override
    public Page<UserVO> getUserVOPage(Page<User> UserPage, HttpServletRequest request) {
        List<User> UserList = UserPage.getRecords();
        Page<UserVO> UserVOPage = new Page<>(UserPage.getCurrent(), UserPage.getSize(), UserPage.getTotal());
        if (CollUtil.isEmpty(UserList)) {
            return UserVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserVO> UserVOList = UserList.stream().map(User -> {
            return UserVO.objToVo(User);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = UserList.stream().map(User::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> UserIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> UserIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> UserIdSet = UserList.stream().map(User::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<UserThumb> UserThumbQueryWrapper = new QueryWrapper<>();
            UserThumbQueryWrapper.in("UserId", UserIdSet);
            UserThumbQueryWrapper.eq("userId", loginUser.getId());
            List<UserThumb> UserUserThumbList = UserThumbMapper.selectList(UserThumbQueryWrapper);
            UserUserThumbList.forEach(UserUserThumb -> UserIdHasThumbMap.put(UserUserThumb.getUserId(), true));
            // 获取收藏
            QueryWrapper<UserFavour> UserFavourQueryWrapper = new QueryWrapper<>();
            UserFavourQueryWrapper.in("UserId", UserIdSet);
            UserFavourQueryWrapper.eq("userId", loginUser.getId());
            List<UserFavour> UserFavourList = UserFavourMapper.selectList(UserFavourQueryWrapper);
            UserFavourList.forEach(UserFavour -> UserIdHasFavourMap.put(UserFavour.getUserId(), true));
        }
        // 填充信息
        UserVOList.forEach(UserVO -> {
            Long userId = UserVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            UserVO.setUser(userService.getUserVO(user));
            UserVO.setHasThumb(UserIdHasThumbMap.getOrDefault(UserVO.getId(), false));
            UserVO.setHasFavour(UserIdHasFavourMap.getOrDefault(UserVO.getId(), false));
        });
        // endregion

        UserVOPage.setRecords(UserVOList);
        return UserVOPage;
    }

}
