package com.zjy.project.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjy.project.model.entity.App;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 应用视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class AppVO implements Serializable {

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
     * @param AppVO
     * @return
     */
    public static App voToObj(AppVO AppVO) {
        if (AppVO == null) {
            return null;
        }
        App App = new App();
        BeanUtils.copyProperties(AppVO, App);
        List<String> tagList = AppVO.getTagList();
        App.setTags(JSONUtil.toJsonStr(tagList));
        return App;
    }

    /**
     * 对象转封装类
     *
     * @param App
     * @return
     */
    public static AppVO objToVo(App App) {
        if (App == null) {
            return null;
        }
        AppVO AppVO = new AppVO();
        BeanUtils.copyProperties(App, AppVO);
        AppVO.setTagList(JSONUtil.toList(App.getTags(), String.class));
        return AppVO;
    }
}
