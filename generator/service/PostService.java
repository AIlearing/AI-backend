package com.zjy.Post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.Post.model.dto.Post.PostQueryRequest;
import com.zjy.Post.model.entity.Post;
import com.zjy.Post.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface PostService extends IService<Post> {

    /**
     * 校验数据
     *
     * @param Post
     * @param add 对创建的数据进行校验
     */
    void validPost(Post Post, boolean add);

    /**
     * 获取查询条件
     *
     * @param PostQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest PostQueryRequest);
    
    /**
     * 获取帖子封装
     *
     * @param Post
     * @param request
     * @return
     */
    PostVO getPostVO(Post Post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param PostPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> PostPage, HttpServletRequest request);
}
