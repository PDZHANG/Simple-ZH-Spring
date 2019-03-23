package com.vps.webmvc.servlet;

import com.vps.webmvc.annotaion.ZHAutowired;
import com.vps.webmvc.annotaion.ZHController;
import com.vps.webmvc.annotaion.ZHRequestMapping;
import com.vps.webmvc.annotaion.ZHService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class ZHDispatcherServlet extends HttpServlet {


    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handleMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        requestURI = requestURI.replaceAll(contextPath,"").replaceAll("/+","/");
        if (!handleMapping.containsKey(requestURI)){
            resp.setStatus(404);
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = this.handleMapping.get(requestURI);
        System.out.println("do Post method : " + method);

        //获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i = 0;i<parameterTypes.length;i++){
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class){
                paramValues[i]= req;
                continue;
            }else if (parameterType == HttpServletResponse.class){
                paramValues[i]= resp;
                continue;
            }  else if(parameterType == String.class){
                for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
                    String value = Arrays.toString(stringEntry.getValue()).replaceAll("\\[|\\]","").replaceAll(",\\s",",");
                    paramValues[i] = value;
                }
            }else if(parameterType == Integer.class){
                for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
                    String value = Arrays.toString(stringEntry.getValue()).replaceAll("\\[|\\]","").replaceAll(",\\s",",");
                    paramValues[i] =Integer.valueOf( value);
                }
            }

        }
        //获取到方法对应的bean
        String beanName =lowcaseFirstChar(method.getDeclaringClass().getSimpleName());
        try {
            method.invoke(this.ioc.get(beanName),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1. load config
        doLoadConfig(config);
        //2. scan packages
        doScanPackages(properties.getProperty("scanPackages"));
        //3. load instance
        doInstances();
        //4. autowired instance field
        doAutowired();
        //5. init mvc handle mapping
        initMvcHandlerMapping();

        System.out.println("Spring IOC And Mvc Init Success");

    }

    private void initMvcHandlerMapping() {
        if (ioc.isEmpty()) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if(!aClass.isAnnotationPresent(ZHController.class)){
                continue;
            }
            String baseUrl = "";
            if (aClass.isAnnotationPresent(ZHRequestMapping.class)){
                ZHRequestMapping annotation = aClass.getAnnotation(ZHRequestMapping.class);
                baseUrl = annotation.value();
            }

            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(ZHRequestMapping.class)) continue;
                ZHRequestMapping annotation = method.getAnnotation(ZHRequestMapping.class);
                String url =  (baseUrl + annotation.value()).replaceAll("/+","/");
                handleMapping.put(url,method);
                System.out.println("Mapped "+ url + " into " + method);
            }
        }

    }

    private void doAutowired() {
        if (ioc.isEmpty()) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (!declaredField.isAnnotationPresent(ZHAutowired.class)) continue;
                ZHAutowired annotation = declaredField.getAnnotation(ZHAutowired.class);
                String beanName = annotation.value().trim();
                if ("".equals(beanName)) {
                    beanName = declaredField.getType().getName();
                }
                declaredField.setAccessible(true);//允许操作
                try {
                    declaredField.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstances() {
        if (classNames.isEmpty()) {
            return;
        } else {
            //获取类名
            for (String className : classNames) {
                try {
                    Class<?> aClass = Class.forName(className);
                    // 通过反射创建类的实例
                    if (aClass.isAnnotationPresent(ZHController.class)) {
                        try {
                            Object instance = aClass.newInstance();
                            String key = lowcaseFirstChar(aClass.getSimpleName());
                            ioc.put(key, instance);
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    } else if (aClass.isAnnotationPresent(ZHService.class)) {
                        ZHService annotation = aClass.getAnnotation(ZHService.class);
                        String beanName = annotation.value();
                        if ("".equals(beanName)) {
                            beanName = lowcaseFirstChar(aClass.getSimpleName());

                        }
                        Object instance = aClass.newInstance();
                        ioc.put(beanName, instance);
                        //根据接口来进行实例化
                        Class<?>[] interfaces = aClass.getInterfaces();
                        for (Class<?> anInterface : interfaces) {
                            ioc.put(anInterface.getName(), instance);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String lowcaseFirstChar(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanPackages(String scanPackages) {
        URL resource = this.getClass().getClassLoader().getResource(scanPackages.replaceAll("\\.", "/"));
        File file = new File(resource.getFile());

        for (File file1 : file.listFiles()) {
            if (file1.isDirectory()) {
                doScanPackages(scanPackages + "." + file1.getName());
            } else {
                String className = (scanPackages + "." + file1.getName()).replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(ServletConfig config) {
        //1.get application.properties config
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");

        //2.
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            properties.load(is);
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            try {
                if (is!=null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
