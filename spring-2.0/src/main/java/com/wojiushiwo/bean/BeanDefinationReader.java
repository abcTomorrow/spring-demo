package com.wojiushiwo.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by meng on 2018/7/8.
 */
public class BeanDefinationReader {
    private Properties prop = new Properties();
    private List<String> beanClassNames = new ArrayList<>();
    private static final String BASE_PACKAGE = "basePackage";

    public List<String> loadBeanDefinitions() {
        return beanClassNames;
    }

    public BeanDefinationReader(String basePackage) {
        doLoadConfig(basePackage);
        doScan(prop.getProperty(BASE_PACKAGE));
    }

    public void doLoadConfig(String basePackage) {
        try {
            String targetPackage = basePackage.replace("classpath:", "");
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(targetPackage);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doScan(String basePackage) {
//        String basePackage = prop.getProperty("basePackage");
        String targetPackage = basePackage.replace(".", "/");
        URL resource = this.getClass().getClassLoader().getResource(targetPackage);
        File file = new File(resource.getPath());
        for (File temp : file.listFiles()) {
            if (temp.isDirectory()) {
                doScan(basePackage + "." + temp.getName());
            } else {
                String path = basePackage + "." + temp.getName();
                beanClassNames.add(path.replace(".class", ""));
            }
        }
    }

    public BeanDefination registerBeanDefinition(String beanName) {
        if (beanClassNames.contains(beanName)) {
            BeanDefination beanDefination = new BeanDefination();
            beanDefination.setBeanName(beanName);
            beanDefination.setBeanFactoryName(lowerFirseCase(beanName.substring(beanName.lastIndexOf(".") + 1)));
            return beanDefination;
        }
        return null;
    }

    private String lowerFirseCase(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getProp(){
        return prop;
    }
}
