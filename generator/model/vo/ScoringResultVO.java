package com.zjy.ScoringResult.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.ScoringResult.model.entity.ScoringResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评分结果视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class ScoringResultVO implements Serializable {

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
     * @param ScoringResultVO
     * @return
     */
    public static ScoringResult voToObj(ScoringResultVO ScoringResultVO) {
        if (ScoringResultVO == null) {
            return null;
        }
        ScoringResult ScoringResult = new ScoringResult();
        BeanUtils.copyProperties(ScoringResultVO, ScoringResult);
        List<String> tagList = ScoringResultVO.getTagList();
        ScoringResult.setTags(JSONUtil.toJsonStr(tagList));
        return ScoringResult;
    }

    /**
     * 对象转封装类
     *
     * @param ScoringResult
     * @return
     */
    public static ScoringResultVO objToVo(ScoringResult ScoringResult) {
        if (ScoringResult == null) {
            return null;
        }
        ScoringResultVO ScoringResultVO = new ScoringResultVO();
        BeanUtils.copyProperties(ScoringResult, ScoringResultVO);
        ScoringResultVO.setTagList(JSONUtil.toList(ScoringResult.getTags(), String.class));
        return ScoringResultVO;
    }
}
