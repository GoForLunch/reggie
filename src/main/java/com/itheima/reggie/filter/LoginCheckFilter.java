package com.itheima.reggie.filter;


import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获得本次请求uri
        String requestUri = request.getRequestURI();

        log.info("拦截到请求：{}",requestUri);

        // 定义不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 判断本次请求是否需要处理
        boolean check = check(urls,requestUri);
        // 不需要处理则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestUri);
            filterChain.doFilter(request,response);
            return;
        }

        // 判断登录状态，如果已登录则放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userid = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userid);

            filterChain.doFilter(request,response);
            return;
        }

        // 如果未登录则返回登录结果，通过输出流的方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


//        log.info("拦截到请求:{}",request.getRequestURI());
//        filterChain.doFilter(request,response);

    }


    /**
     * 路径匹配，判断是否需要放行
     * @param urls
     * @param requestUri
     * @return
     */
    public boolean check(String[] urls,String requestUri){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url,requestUri);
            if(match){
                return true;
            }
        }
        return false;
    }
}
