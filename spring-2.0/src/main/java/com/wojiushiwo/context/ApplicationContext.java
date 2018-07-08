package com.wojiushiwo.context;

import com.wojiushiwo.annotation.AutoWired;
import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.Service;
import com.wojiushiwo.bean.BeanDefination;
import com.wojiushiwo.bean.BeanDefinationReader;
import com.wojiushiwo.bean.BeanPostProcessor;
import com.wojiushiwo.bean.BeanWrapper;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by meng on 2018/7/8.
 */
public class ApplicationContext {
    private BeanDefinationReader reader;
    private String configLocation;
    private Map<String, BeanDefination> iocMap = new ConcurrentHashMap<>();
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
        doAutowired();
    }


    private void doRegistry(List<String> beanDefinitions) {
        if (CollectionUtils.isEmpty(beanDefinitions)) return;
        try {
            for (String className : beanDefinitions) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isInterface()) continue;
                BeanDefination beanDefination = reader.registerBeanDefinition(className);
                //对应于注解放在接口上，会以其实现类来注册
                if (beanDefination != null) {
                    String factoryBeanName = beanDefination.getBeanFactoryName();
                    iocMap.put(factoryBeanName, beanDefination);
                }
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class interfaceClazz : interfaces) {
                    iocMap.put(interfaceClazz.getName(), beanDefination);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if (iocMap.isEmpty()) return;
        for (Map.Entry<String, BeanDefination> entry : iocMap.entrySet()) {
            if (entry.getValue().isLazyInit()) {
                getBean(entry.getKey());
            }

            for(Map.Entry<String,BeanWrapper> mapEntry : beanWrapperMap.entrySet()){
                populateBean(mapEntry.getKey(),mapEntry.getValue().getWrapperInstance());
            }
        }
    }

    private void populateBean(String beanName, Object instance) {
        try {
            Class<?> clazz = instance.getClass();
            if(!clazz.isAnnotationPresent(Controller.class)||!clazz.isAnnotationPresent(Service.class)) return;
            Field[] fields = clazz.getDeclaredFields();
            for(Field field:fields){
                if(!field.isAnnotationPresent(AutoWired.class))return;
                field.setAccessible(true);
                beanName=field.getType().getName();
                field.set(instance,beanWrapperMap.get(beanName).getWrapperInstance());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object getBean(String beanName) {
        BeanDefination beanDefination = iocMap.get(beanName);
        Object instance = doInit(beanDefination);
        if (instance == null) return null;

        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

        BeanWrapper beanWrapper = new BeanWrapper(instance);
        beanWrapperMap.put(beanName, beanWrapper);

        beanPostProcessor.postProcessAfterInitialization(instance, beanName);
        return beanWrapper.getWrapperInstance();
    }

    //为了确保初始化的bean是单例的，所以有一个集合存放单例bean。是不是单例 有没有初始化 去这个集合中查找即可
    private Object doInit(BeanDefination beanDefination) {
        try {
            String beanName = beanDefination.getBeanName();
            if (!beanCacheMap.containsKey(beanName)) {
                Class<?> clazz = Class.forName(beanName);
                beanCacheMap.put(beanName, clazz.newInstance());
            }
            return beanCacheMap.get(beanName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
