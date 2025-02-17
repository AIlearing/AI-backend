package com.zjy.Post.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.Post.common.ErrorCode;
import com.zjy.Post.constant.CommonConstant;
import com.zjy.Post.exception.ThrowUtils;
import com.zjy.Post.mapper.PostMapper;
import com.zjy.Post.model.dto.Post.PostQueryRequest;
import com.zjy.Post.model.entity.Post;
import com.zjy.Post.model.entity.PostFavour;
import com.zjy.Post.model.entity.PostThumb;
import com.zjy.Post.model.entity.User;
import com.zjy.Post.model.vo.PostVO;
import com.zjy.Post.model.vo.UserVO;
import com.zjy.Post.service.PostService;
import com.zjy.Post.service.UserService;
import com.zjy.Post.utils.SqlUtils;
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
 * 帖子服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param Post
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validPost(Post Post, boolean add) {
        ThrowUtils.throwIf(Post == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = Post.getTitle();
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
     * @param PostQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest PostQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (PostQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = PostQueryRequest.getId();
        Long notId = PostQueryRequest.getNotId();
        String title = PostQueryRequest.getTitle();
        String content = PostQueryRequest.getContent();
        String searchText = PostQueryRequest.getSearchText();
        String sortField = PostQueryRequest.getSortField();
        String sortOrder = PostQueryRequest.getSortOrder();
        List<String> tagList = PostQueryRequest.getTags();
        Long userId = PostQueryRequest.getUserId();
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
     * 获取帖子封装
     *
     * @param Post
     * @param request
     * @return
     */
    @Override
    public PostVO getPostVO(Post Post, HttpServletRequest request) {
        // 对象转封装类
        PostVO PostVO = PostVO.objToVo(Post);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = Post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        PostVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long PostId = Post.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> PostThumbQueryWrapper = new QueryWrapper<>();
            PostThumbQueryWrapper.in("PostId", PostId);
            PostThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb PostThumb = PostThumbMapper.selectOne(PostThumbQueryWrapper);
            PostVO.setHasThumb(PostThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> PostFavourQueryWrapper = new QueryWrapper<>();
            PostFavourQueryWrapper.in("PostId", PostId);
            PostFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour PostFavour = PostFavourMapper.selectOne(PostFavourQueryWrapper);
            PostVO.setHasFavour(PostFavour != null);
        }
        // endregion

        return PostVO;
    }

    /**
     * 分页获取帖子封装
     *
     * @param PostPage
     * @param request
     * @return
     */
    @Override
    public Page<PostVO> getPostVOPage(Page<Post> PostPage, HttpServletRequest request) {
        List<Post> PostList = PostPage.getRecords();
        Page<PostVO> PostVOPage = new Page<>(PostPage.getCurrent(), PostPage.getSize(), PostPage.getTotal());
        if (CollUtil.isEmpty(PostList)) {
            return PostVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PostVO> PostVOList = PostList.stream().map(Post -> {
            return PostVO.objToVo(Post);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = PostList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> PostIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> PostIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> PostIdSet = PostList.stream().map(Post::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> PostThumbQueryWrapper = new QueryWrapper<>();
            PostThumbQueryWrapper.in("PostId", PostIdSet);
            PostThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> PostPostThumbList = PostThumbMapper.selectList(PostThumbQueryWrapper);
            PostPostThumbList.forEach(PostPostThumb -> PostIdHasThumbMap.put(PostPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> PostFavourQueryWrapper = new QueryWrapper<>();
            PostFavourQueryWrapper.in("PostId", PostIdSet);
            PostFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> PostFavourList = PostFavourMapper.selectList(PostFavourQueryWrapper);
            PostFavourList.forEach(PostFavour -> PostIdHasFavourMap.put(PostFavour.getPostId(), true));
        }
        // 填充信息
        PostVOList.forEach(PostVO -> {
            Long userId = PostVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            PostVO.setUser(userService.getUserVO(user));
            PostVO.setHasThumb(PostIdHasThumbMap.getOrDefault(PostVO.getId(), false));
            PostVO.setHasFavour(PostIdHasFavourMap.getOrDefault(PostVO.getId(), false));
        });
        // endregion

        PostVOPage.setRecords(PostVOList);
        return PostVOPage;
    }

}
