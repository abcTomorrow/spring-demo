package com.wojiushiwo.bean;

/**
 * Created by meng on 2018/7/8.
 */
public class BeanWrapper {
    private Object originalInstance;
    private Object wrapperInstance;
    private BeanPostProcessor beanProcessor;

    public BeanWrapper(Object instance) {
        this.originalInstance = instance;
        this.wrapperInstance = instance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    public void setOriginalInstance(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public BeanPostProcessor getBeanProcessor() {
        return beanProcessor;
    }

    public void setBeanProcessor(BeanPostProcessor beanProcessor) {
        this.beanProcessor = beanProcessor;
    }
}
