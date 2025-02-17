package com.zjy.projectAnswer.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.projectAnswer.model.entity.UserAnswer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户答题记录视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class UserAnswerVO implements Serializable {

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
     * @param UserAnswerVO
     * @return
     */
    public static UserAnswer voToObj(UserAnswerVO UserAnswerVO) {
        if (UserAnswerVO == null) {
            return null;
        }
        UserAnswer UserAnswer = new UserAnswer();
        BeanUtils.copyProperties(UserAnswerVO, UserAnswer);
        List<String> tagList = UserAnswerVO.getTagList();
        UserAnswer.setTags(JSONUtil.toJsonStr(tagList));
        return UserAnswer;
    }

    /**
     * 对象转封装类
     *
     * @param UserAnswer
     * @return
     */
    public static UserAnswerVO objToVo(UserAnswer UserAnswer) {
        if (UserAnswer == null) {
            return null;
        }
        UserAnswerVO UserAnswerVO = new UserAnswerVO();
        BeanUtils.copyProperties(UserAnswer, UserAnswerVO);
        UserAnswerVO.setTagList(JSONUtil.toList(UserAnswer.getTags(), String.class));
        return UserAnswerVO;
    }
}
