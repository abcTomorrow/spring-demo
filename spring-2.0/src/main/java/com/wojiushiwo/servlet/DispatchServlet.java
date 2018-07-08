package com.wojiushiwo.servlet;

import com.wojiushiwo.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by meng on 2018/7/8.
 */
public class DispatchServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext context=new ApplicationContext(config.getInitParameter("configLocation"));
        context.refresh();
    }
}
