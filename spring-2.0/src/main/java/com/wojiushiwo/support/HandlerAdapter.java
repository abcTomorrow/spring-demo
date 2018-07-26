package com.wojiushiwo.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {
    private Map<String, Integer> paramMapping;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");

            if (!this.paramMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = this.paramMapping.get(param.getKey());

            //单个赋值是不行的
            paramValues[index] = castStringValue(value, paramTypes[index]);
        }
        //request 和 response 要赋值
        String reqName = HttpServletRequest.class.getName();
        if (this.paramMapping.containsKey(reqName)) {
            int reqIndex = this.paramMapping.get(reqName);
            paramValues[reqIndex] = request;
        }


        String resqName = HttpServletResponse.class.getName();
        if (this.paramMapping.containsKey(resqName)) {
            int respIndex = this.paramMapping.get(resqName);
            paramValues[respIndex] = response;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
        Object r = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
        if (isModelAndView) {
            return (ModelAndView) r;
        } else {
            return null;
        }
    }

    private Object castStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }
}
