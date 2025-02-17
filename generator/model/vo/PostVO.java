package com.zjy.Post.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.Post.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class PostVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param PostVO
     * @return
     */
    public static Post voToObj(PostVO PostVO) {
        if (PostVO == null) {
            return null;
        }
        Post Post = new Post();
        BeanUtils.copyProperties(PostVO, Post);
        List<String> tagList = PostVO.getTagList();
        Post.setTags(JSONUtil.toJsonStr(tagList));
        return Post;
    }

    /**
     * 对象转封装类
     *
     * @param Post
     * @return
     */
    public static PostVO objToVo(Post Post) {
        if (Post == null) {
            return null;
        }
        PostVO PostVO = new PostVO();
        BeanUtils.copyProperties(Post, PostVO);
        PostVO.setTagList(JSONUtil.toList(Post.getTags(), String.class));
        return PostVO;
    }
}
