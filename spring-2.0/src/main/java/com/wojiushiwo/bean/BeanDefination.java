package com.wojiushiwo.bean;

/**
 * Created by meng on 2018/7/8.
 */
public class BeanDefination {
    //扫描包所得beanName
    private String beanName;
    //bean在ioc容器中的名字 即注解value的值
    //This the name of the bean to call the specified factory method on.
    private String beanFactoryName;
    //是否懒加载
    private boolean lazyInit = true;


    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanFactoryName() {
        return beanFactoryName;
    }

    public void setBeanFactoryName(String beanFactoryName) {
        this.beanFactoryName = beanFactoryName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
}
