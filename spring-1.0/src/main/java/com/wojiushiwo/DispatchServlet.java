package com.wojiushiwo;

import com.wojiushiwo.annotation.AutoWired;
import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.Service;
import com.wojiushiwo.service.UserService;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * Created by meng on 2018/7/8.
 */
public class DispatchServlet extends HttpServlet {
    private Properties prop = new Properties();
    private List<String> classList = new ArrayList<>();
    private Map<String, Object> iocMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //定位
        doLocation(config.getInitParameter("configLocation"));
        //加载
        String basePackage = prop.getProperty("basePackage");
        doScan(basePackage);
        //注册 注入到IOC容器
        doRegister(classList);
        //依赖注入
        doAutoWired(iocMap);
    }

    //定位
    private void doLocation(String configLocations) {
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(configLocations.replace("classpath:", ""));
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doScan(String basePackage) {
        try {
            //犯错点 误使用replace(不对正则生效 只适用字符 字符串)
            //生成的resource是基于basePackage的class文件路径
            URL resource = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
            File directory = new File(resource.getPath());
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    //犯错点 这里是basePackage
                    doScan(basePackage + "." + file.getName());
                } else {
                    String fileName = basePackage + "." + file.getName();
                    classList.add(fileName.replace(".class", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRegister(List<String> classList) {
        try {
            if (CollectionUtils.isEmpty(classList)) return;
            for (String className : classList) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller annotation = clazz.getAnnotation(Controller.class);
                    String beanName = annotation.value();
                    //如果注解内容为空 则将类型作为beanName
                    if (beanName == null || "".equals(beanName)) {
                        beanName = lowerCase(clazz.getSimpleName());
                    }
                    iocMap.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service annotation = clazz.getAnnotation(Service.class);
                    String beanName = annotation.value();
                    //如果注解内容为空 则将类型作为beanName
                    if (beanName == null || "".equals(beanName)) {
                        beanName = lowerCase(clazz.getSimpleName());
                    }
                    iocMap.put(beanName, clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutoWired(Map<String, Object> iocMap) {
        if (CollectionUtils.isEmpty(iocMap.entrySet())) return;
        try {
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                Object value = entry.getValue();
                Field[] fields = value.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(AutoWired.class)) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        AutoWired annotation = field.getAnnotation(AutoWired.class);
                        String beanName = annotation.value().isEmpty() ? type.getSimpleName() : annotation.value();
                        field.set(field.getName(), iocMap.get(lowerCase(beanName)));
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private String lowerCase(String beanName) {
        char[] chars = beanName.toCharArray();
        //大小写ASCII差32
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
