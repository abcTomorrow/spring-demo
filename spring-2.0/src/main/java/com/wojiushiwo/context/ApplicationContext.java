package com.wojiushiwo.context;

import com.wojiushiwo.annotation.AutoWired;
import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.Service;
import com.wojiushiwo.bean.BeanDefinationReader;
import com.wojiushiwo.bean.BeanWrapper;
import com.wojiushiwo.controller.UserController;
import com.wojiushiwo.service.UserService;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by meng on 2018/7/8.
 */
public class ApplicationContext {
    private BeanDefinationReader reader;
    private String configLocation;
    private Map<String, Object> iocMap = new ConcurrentHashMap<>();
    //保证注册式单例
    private Map<String, Object> beanCacheMap = new ConcurrentHashMap<>();
    //保存代理实例
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

    public ApplicationContext(String configLocation) {
        this.configLocation = configLocation;
    }

    public void refresh() {
        //定位
        reader = new BeanDefinationReader(configLocation);
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //载入
        doRegistry(beanDefinitions);
        //注册
        doAutowired(iocMap);
//        userController.sayHello(null, "hello");
    }


    private void doRegistry(List<String> classList) {
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

    private void doAutowired(Map<String, Object> iocMap) {
        if (CollectionUtils.isEmpty(iocMap.entrySet())) return;
        try {
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                Object value = entry.getValue();
                Field[] fields = value.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(AutoWired.class)) {
                        Class<?> type = field.getType();
                        AutoWired annotation = field.getAnnotation(AutoWired.class);
                        String beanName = annotation.value().isEmpty() ? type.getSimpleName() : annotation.value();
                        field.setAccessible(true);
                        field.set(field.getName(), iocMap.get(lowerCase(beanName)));
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    // 对Bean进行填充
//    private void populateBean(String beanName, Object instance) {
//        try {
//            Class<?> clazz = instance.getClass();
//            if (!clazz.isAnnotationPresent(Controller.class) || !clazz.isAnnotationPresent(Service.class)) return;
//            Field[] fields = clazz.getDeclaredFields();
//            for (Field field : fields) {
//                if (!field.isAnnotationPresent(AutoWired.class)) return;
//                field.setAccessible(true);
//                beanName = field.getType().getName();
//                field.set(instance, beanWrapperMap.get(beanName).getWrapperInstance());
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }

    //如果不是延迟加载 则通过IOC容器获取Bean
    //如果该Bean的作用域是单例 则先查看单例Bean集合中是否已经实例化该bean
//    public Object getBean(String beanName) {
//        BeanDefination beanDefination = iocMap.get(beanName);
//        Object instance = doInit(beanDefination);
//        if (instance == null) return null;
//
//        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
//        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
//        //Bean包裹类 wrapperInstance 即产生的代理对象
//        BeanWrapper beanWrapper = new BeanWrapper(instance);
//        beanWrapperMap.put(beanName, beanWrapper);
//
//        beanPostProcessor.postProcessAfterInitialization(instance, beanName);
//        return beanWrapper.getWrapperInstance();
//    }
//
//    //为了确保初始化的bean是单例的，所以有一个集合存放单例bean。是不是单例 有没有初始化 去这个集合中查找即可
//    private Object doInit(BeanDefination beanDefination) {
//        try {
//            String beanName = beanDefination.getBeanName();
//            if (!beanCacheMap.containsKey(beanName)) {
//                Class<?> clazz = Class.forName(beanName);
//                beanCacheMap.put(beanName, clazz.newInstance());
//            }
//            return beanCacheMap.get(beanName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private String lowerCase(String beanName) {
        char[] chars = beanName.toCharArray();
        //大小写ASCII差32
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Map<String, Object> getAll() {
        return this.iocMap;
    }

    public Properties getConfig() {
        return reader.getProp();
    }
}
