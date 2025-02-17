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
import com.zjy.project.model.dto.post.PostAddRequest;
import com.zjy.project.model.dto.post.PostEditRequest;
import com.zjy.project.model.dto.post.PostQueryRequest;
import com.zjy.project.model.dto.post.PostUpdateRequest;
import com.zjy.project.model.entity.Post;
import com.zjy.project.model.entity.User;
import com.zjy.project.model.vo.PostVO;
import com.zjy.project.service.PostService;
import com.zjy.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/Post")
@Slf4j
public class PostController {

    @Resource
    private PostService PostService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建帖子
     *
     * @param PostAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest PostAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(PostAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Post Post = new Post();
        BeanUtils.copyProperties(PostAddRequest, Post);
        // 数据校验
        PostService.validPost(Post, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        Post.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = PostService.save(Post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newPostId = Post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除帖子
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Post oldPost = PostService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = PostService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子（仅管理员可用）
     *
     * @param PostUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest PostUpdateRequest) {
        if (PostUpdateRequest == null || PostUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Post Post = new Post();
        BeanUtils.copyProperties(PostUpdateRequest, Post);
        // 数据校验
        PostService.validPost(Post, false);
        // 判断是否存在
        long id = PostUpdateRequest.getId();
        Post oldPost = PostService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = PostService.updateById(Post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取帖子（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Post Post = PostService.getById(id);
        ThrowUtils.throwIf(Post == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(PostService.getPostVO(Post, request));
    }

    /**
     * 分页获取帖子列表（仅管理员可用）
     *
     * @param PostQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest PostQueryRequest) {
        long current = PostQueryRequest.getCurrent();
        long size = PostQueryRequest.getPageSize();
        // 查询数据库
        Page<Post> PostPage = PostService.page(new Page<>(current, size),
                PostService.getQueryWrapper(PostQueryRequest));
        return ResultUtils.success(PostPage);
    }

    /**
     * 分页获取帖子列表（封装类）
     *
     * @param PostQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest PostQueryRequest,
                                                               HttpServletRequest request) {
        long current = PostQueryRequest.getCurrent();
        long size = PostQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Post> PostPage = PostService.page(new Page<>(current, size),
                PostService.getQueryWrapper(PostQueryRequest));
        // 获取封装类
        return ResultUtils.success(PostService.getPostVOPage(PostPage, request));
    }

    /**
     * 分页获取当前登录用户创建的帖子列表
     *
     * @param PostQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest PostQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(PostQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        PostQueryRequest.setUserId(loginUser.getId());
        long current = PostQueryRequest.getCurrent();
        long size = PostQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Post> PostPage = PostService.page(new Page<>(current, size),
                PostService.getQueryWrapper(PostQueryRequest));
        // 获取封装类
        return ResultUtils.success(PostService.getPostVOPage(PostPage, request));
    }

    /**
     * 编辑帖子（给用户使用）
     *
     * @param PostEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest PostEditRequest, HttpServletRequest request) {
        if (PostEditRequest == null || PostEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Post Post = new Post();
        BeanUtils.copyProperties(PostEditRequest, Post);
        // 数据校验
        PostService.validPost(Post, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = PostEditRequest.getId();
        Post oldPost = PostService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = PostService.updateById(Post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
