package com.zjy.project.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.project.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class UserVO implements Serializable {

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
     * @param UserVO
     * @return
     */
    public static User voToObj(UserVO UserVO) {
        if (UserVO == null) {
            return null;
        }
        User User = new User();
        BeanUtils.copyProperties(UserVO, User);
        List<String> tagList = UserVO.getTagList();
        User.setTags(JSONUtil.toJsonStr(tagList));
        return User;
    }

    /**
     * 对象转封装类
     *
     * @param User
     * @return
     */
    public static UserVO objToVo(User User) {
        if (User == null) {
            return null;
        }
        UserVO UserVO = new UserVO();
        BeanUtils.copyProperties(User, UserVO);
        UserVO.setTagList(JSONUtil.toList(User.getTags(), String.class));
        return UserVO;
    }
}
