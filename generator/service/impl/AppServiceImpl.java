package com.zjy.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.project.common.ErrorCode;
import com.zjy.project.constant.CommonConstant;
import com.zjy.project.exception.ThrowUtils;
import com.zjy.project.mapper.AppMapper;
import com.zjy.project.model.dto.App.AppQueryRequest;
import com.zjy.project.model.entity.App;
import com.zjy.project.model.entity.AppFavour;
import com.zjy.project.model.entity.AppThumb;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.AppVO;
import com.zjy.project.model.vo.UserVO;
import com.zjy.project.service.AppService;
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
 * 应用服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param App
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App App, boolean add) {
        ThrowUtils.throwIf(App == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = App.getTitle();
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
     * @param AppQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest AppQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (AppQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = AppQueryRequest.getId();
        Long notId = AppQueryRequest.getNotId();
        String title = AppQueryRequest.getTitle();
        String content = AppQueryRequest.getContent();
        String searchText = AppQueryRequest.getSearchText();
        String sortField = AppQueryRequest.getSortField();
        String sortOrder = AppQueryRequest.getSortOrder();
        List<String> tagList = AppQueryRequest.getTags();
        Long userId = AppQueryRequest.getUserId();
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
     * 获取应用封装
     *
     * @param App
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App App, HttpServletRequest request) {
        // 对象转封装类
        AppVO AppVO = AppVO.objToVo(App);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = App.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        AppVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long AppId = App.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<AppThumb> AppThumbQueryWrapper = new QueryWrapper<>();
            AppThumbQueryWrapper.in("AppId", AppId);
            AppThumbQueryWrapper.eq("userId", loginUser.getId());
            AppThumb AppThumb = AppThumbMapper.selectOne(AppThumbQueryWrapper);
            AppVO.setHasThumb(AppThumb != null);
            // 获取收藏
            QueryWrapper<AppFavour> AppFavourQueryWrapper = new QueryWrapper<>();
            AppFavourQueryWrapper.in("AppId", AppId);
            AppFavourQueryWrapper.eq("userId", loginUser.getId());
            AppFavour AppFavour = AppFavourMapper.selectOne(AppFavourQueryWrapper);
            AppVO.setHasFavour(AppFavour != null);
        }
        // endregion

        return AppVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param AppPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> AppPage, HttpServletRequest request) {
        List<App> AppList = AppPage.getRecords();
        Page<AppVO> AppVOPage = new Page<>(AppPage.getCurrent(), AppPage.getSize(), AppPage.getTotal());
        if (CollUtil.isEmpty(AppList)) {
            return AppVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> AppVOList = AppList.stream().map(App -> {
            return AppVO.objToVo(App);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = AppList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> AppIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> AppIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> AppIdSet = AppList.stream().map(App::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<AppThumb> AppThumbQueryWrapper = new QueryWrapper<>();
            AppThumbQueryWrapper.in("AppId", AppIdSet);
            AppThumbQueryWrapper.eq("userId", loginUser.getId());
            List<AppThumb> AppAppThumbList = AppThumbMapper.selectList(AppThumbQueryWrapper);
            AppAppThumbList.forEach(AppAppThumb -> AppIdHasThumbMap.put(AppAppThumb.getAppId(), true));
            // 获取收藏
            QueryWrapper<AppFavour> AppFavourQueryWrapper = new QueryWrapper<>();
            AppFavourQueryWrapper.in("AppId", AppIdSet);
            AppFavourQueryWrapper.eq("userId", loginUser.getId());
            List<AppFavour> AppFavourList = AppFavourMapper.selectList(AppFavourQueryWrapper);
            AppFavourList.forEach(AppFavour -> AppIdHasFavourMap.put(AppFavour.getAppId(), true));
        }
        // 填充信息
        AppVOList.forEach(AppVO -> {
            Long userId = AppVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            AppVO.setUser(userService.getUserVO(user));
            AppVO.setHasThumb(AppIdHasThumbMap.getOrDefault(AppVO.getId(), false));
            AppVO.setHasFavour(AppIdHasFavourMap.getOrDefault(AppVO.getId(), false));
        });
        // endregion

        AppVOPage.setRecords(AppVOList);
        return AppVOPage;
    }

}
