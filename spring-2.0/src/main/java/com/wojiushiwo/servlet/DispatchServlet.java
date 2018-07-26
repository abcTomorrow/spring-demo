package com.wojiushiwo.servlet;

import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.RequestMapping;
import com.wojiushiwo.annotation.RequestParam;
import com.wojiushiwo.context.ApplicationContext;
import com.wojiushiwo.support.HandlerAdapter;
import com.wojiushiwo.support.HandlerMapping;
import com.wojiushiwo.support.ModelAndView;
import com.wojiushiwo.support.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by meng on 2018/7/8.
 */
public class DispatchServlet extends HttpServlet {
    List<HandlerMapping> handlerMappings = new ArrayList<>();
    Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<>();
    List<ViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext context = new ApplicationContext(config.getInitParameter("configLocation"));
        context.refresh();

        //加载9大组件
        initStrategies(context);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {

        try {
            //找到HandlerMapping
            HandlerMapping handlerMapping = getHandlerMapping(request);
            //找到HandlerAdapter
            HandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);
            ModelAndView view = handlerAdapter.handle(request, response, handlerMapping);
            applyDefaultViewName(response, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private HandlerMapping getHandlerMapping(HttpServletRequest request) {
        if (handlerMappings.isEmpty()) return null;
        //根据url从handlerMappings中获取HandlerMapping 即根据url确定处理请求的controller
        String url = request.getRequestURI();//包含context 全路径了
        String contextPath = request.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (HandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if (matcher.matches()) {
                return handlerMapping;
            }
        }
        return null;
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {
        if (handlerAdapters.isEmpty()) return null;
        return handlerAdapters.get(handlerMapping);
    }

    private void initStrategies(ApplicationContext context) {
        //请求解析
        initMultipartResolver(context);
        //本地化 国际化
        initLocaleResolver(context);
        //主题 view层
        initThemeResolver(context);
        //解析url和method的关系
        initHandlerMappings(context);
        //适配器匹配的过程
        initHandlerAdapters(context);
        //异常解析
        initHandlerExceptionResolvers(context);
        //请求转发
        initRequestToViewNameTranslator(context);
        //视图解析
        initViewResolvers(context);
        initFlashMapManager(context);
    }

    private void initMultipartResolver(ApplicationContext context) {
    }

    private void initLocaleResolver(ApplicationContext context) {
    }

    private void initThemeResolver(ApplicationContext context) {
    }

    //获取url和method的关系 如url和method的对应
    private void initHandlerMappings(ApplicationContext context) {
        Map<String, Object> iocMap = context.getAll();
        if (iocMap.isEmpty()) return;
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) continue;
            String url = "";
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                url = requestMapping.value();
            }
            //扫描Controller 下面的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) continue;
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                //如果url中有多个/ 如//get 等改为/get
                String regex = (url + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(entry.getValue(), method, pattern));
            }
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        if (handlerMappings.isEmpty()) return;
        //key作为参数的参数类型 value作为参数的索引号
        Map<String, Integer> paramMapping = new HashMap<>();
        for (HandlerMapping handlerMapping : handlerMappings) {
            Method method = handlerMapping.getMethod();
            //把这个方法的所有参数获取到
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                //匹配Request response类型的参数
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }
            //匹配自定义参数
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int j = 0; j < annotations.length; j++) {
                for (Annotation annotation : annotations[j]) {
                    if (annotation instanceof RequestParam) {
                        String paramValue = ((RequestParam) annotation).value();
                        if (!"".equals(paramValue)) {
                            paramMapping.put(paramValue, j);
                        }
                    }
                }
            }
            handlerAdapters.put(handlerMapping, new HandlerAdapter(paramMapping));
        }
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
    }

    private void initViewResolvers(ApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String rootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File file = new File(rootPath);
        for (File template : file.listFiles()) {
            viewResolvers.add(new ViewResolver(template.getName(), template));
        }
    }

    private void initFlashMapManager(ApplicationContext context) {
    }

    public void applyDefaultViewName(HttpServletResponse resp, ModelAndView mv) throws Exception {
        if (null == mv) {
            return;
        }
        if (viewResolvers.isEmpty()) {
            return;
        }

        for (ViewResolver resolver : viewResolvers) {
            if (!mv.getView().equals(resolver.getFileName())) {
                continue;
            }

            String r = resolver.parse(mv);

            if (r != null) {
                resp.getWriter().write(r);
                break;
            }
        }
    }
}
