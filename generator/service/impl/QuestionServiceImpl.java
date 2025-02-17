package com.zjy.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.project.common.ErrorCode;
import com.zjy.project.constant.CommonConstant;
import com.zjy.project.exception.ThrowUtils;
import com.zjy.project.mapper.QuestionMapper;
import com.zjy.project.model.dto.Question.QuestionQueryRequest;
import com.zjy.project.model.entity.Question;
import com.zjy.project.model.entity.QuestionFavour;
import com.zjy.project.model.entity.QuestionThumb;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.QuestionVO;
import com.zjy.project.model.vo.UserVO;
import com.zjy.project.service.QuestionService;
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
 * 题目服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param Question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question Question, boolean add) {
        ThrowUtils.throwIf(Question == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = Question.getTitle();
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
     * @param QuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest QuestionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (QuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = QuestionQueryRequest.getId();
        Long notId = QuestionQueryRequest.getNotId();
        String title = QuestionQueryRequest.getTitle();
        String content = QuestionQueryRequest.getContent();
        String searchText = QuestionQueryRequest.getSearchText();
        String sortField = QuestionQueryRequest.getSortField();
        String sortOrder = QuestionQueryRequest.getSortOrder();
        List<String> tagList = QuestionQueryRequest.getTags();
        Long userId = QuestionQueryRequest.getUserId();
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
     * 获取题目封装
     *
     * @param Question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question Question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO QuestionVO = QuestionVO.objToVo(Question);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = Question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        QuestionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long QuestionId = Question.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<QuestionThumb> QuestionThumbQueryWrapper = new QueryWrapper<>();
            QuestionThumbQueryWrapper.in("QuestionId", QuestionId);
            QuestionThumbQueryWrapper.eq("userId", loginUser.getId());
            QuestionThumb QuestionThumb = QuestionThumbMapper.selectOne(QuestionThumbQueryWrapper);
            QuestionVO.setHasThumb(QuestionThumb != null);
            // 获取收藏
            QueryWrapper<QuestionFavour> QuestionFavourQueryWrapper = new QueryWrapper<>();
            QuestionFavourQueryWrapper.in("QuestionId", QuestionId);
            QuestionFavourQueryWrapper.eq("userId", loginUser.getId());
            QuestionFavour QuestionFavour = QuestionFavourMapper.selectOne(QuestionFavourQueryWrapper);
            QuestionVO.setHasFavour(QuestionFavour != null);
        }
        // endregion

        return QuestionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param QuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> QuestionPage, HttpServletRequest request) {
        List<Question> QuestionList = QuestionPage.getRecords();
        Page<QuestionVO> QuestionVOPage = new Page<>(QuestionPage.getCurrent(), QuestionPage.getSize(), QuestionPage.getTotal());
        if (CollUtil.isEmpty(QuestionList)) {
            return QuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> QuestionVOList = QuestionList.stream().map(Question -> {
            return QuestionVO.objToVo(Question);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = QuestionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> QuestionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> QuestionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> QuestionIdSet = QuestionList.stream().map(Question::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<QuestionThumb> QuestionThumbQueryWrapper = new QueryWrapper<>();
            QuestionThumbQueryWrapper.in("QuestionId", QuestionIdSet);
            QuestionThumbQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionThumb> QuestionQuestionThumbList = QuestionThumbMapper.selectList(QuestionThumbQueryWrapper);
            QuestionQuestionThumbList.forEach(QuestionQuestionThumb -> QuestionIdHasThumbMap.put(QuestionQuestionThumb.getQuestionId(), true));
            // 获取收藏
            QueryWrapper<QuestionFavour> QuestionFavourQueryWrapper = new QueryWrapper<>();
            QuestionFavourQueryWrapper.in("QuestionId", QuestionIdSet);
            QuestionFavourQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionFavour> QuestionFavourList = QuestionFavourMapper.selectList(QuestionFavourQueryWrapper);
            QuestionFavourList.forEach(QuestionFavour -> QuestionIdHasFavourMap.put(QuestionFavour.getQuestionId(), true));
        }
        // 填充信息
        QuestionVOList.forEach(QuestionVO -> {
            Long userId = QuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            QuestionVO.setUser(userService.getUserVO(user));
            QuestionVO.setHasThumb(QuestionIdHasThumbMap.getOrDefault(QuestionVO.getId(), false));
            QuestionVO.setHasFavour(QuestionIdHasFavourMap.getOrDefault(QuestionVO.getId(), false));
        });
        // endregion

        QuestionVOPage.setRecords(QuestionVOList);
        return QuestionVOPage;
    }

}
