package com.zjy.project.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.project.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionVO implements Serializable {

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
     * @param QuestionVO
     * @return
     */
    public static Question voToObj(QuestionVO QuestionVO) {
        if (QuestionVO == null) {
            return null;
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionVO, Question);
        List<String> tagList = QuestionVO.getTagList();
        Question.setTags(JSONUtil.toJsonStr(tagList));
        return Question;
    }

    /**
     * 对象转封装类
     *
     * @param Question
     * @return
     */
    public static QuestionVO objToVo(Question Question) {
        if (Question == null) {
            return null;
        }
        QuestionVO QuestionVO = new QuestionVO();
        BeanUtils.copyProperties(Question, QuestionVO);
        QuestionVO.setTagList(JSONUtil.toList(Question.getTags(), String.class));
        return QuestionVO;
    }
}
