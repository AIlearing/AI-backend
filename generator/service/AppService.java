package com.zjy.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.project.model.dto.App.AppQueryRequest;
import com.zjy.project.model.entity.App;
import com.zjy.project.model.vo.AppVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 应用服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface AppService extends IService<App> {

    /**
     * 校验数据
     *
     * @param App
     * @param add 对创建的数据进行校验
     */
    void validApp(App App, boolean add);

    /**
     * 获取查询条件
     *
     * @param AppQueryRequest
     * @return
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest AppQueryRequest);
    
    /**
     * 获取应用封装
     *
     * @param App
     * @param request
     * @return
     */
    AppVO getAppVO(App App, HttpServletRequest request);

    /**
     * 分页获取应用封装
     *
     * @param AppPage
     * @param request
     * @return
     */
    Page<AppVO> getAppVOPage(Page<App> AppPage, HttpServletRequest request);
}
