package com.github.duanyashu.easyvobus.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring上下文工具类，用于获取Bean和项目扫描包路径
 *
 * @author duanyashu
 */
public final class SpringContextUtil implements BeanFactoryPostProcessor, ApplicationContextAware
{

    /** Spring应用上下文环境 */
    private static ConfigurableListableBeanFactory beanFactory;

    /** Spring应用上下文 */
    public static ApplicationContext applicationContext;

    /**
     * 项目自身的 ComponentScan 包路径
     */
    public static volatile  Set<String> componentScanPackages;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        SpringContextUtil.applicationContext = applicationContext;
        componentScanPackages = getComponentScanPackages();
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        SpringContextUtil.beanFactory = beanFactory;
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return result
     * @throws org.springframework.beans.BeansException
     *
     */
    public static <T> T getBean(Class<T> clz) throws BeansException
    {
        T result = (T) beanFactory.getBean(clz);
        return result;
    }

    /**
     * 获取项目自身的 ComponentScan 包路径
     */
    private static Set<String> getComponentScanPackages() {
        Set<String> packages = new HashSet<>();
        // 获取主启动类（通常是被@SpringBootApplication注解的类）
        String[] startupBeanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class);
        for (String beanName : startupBeanNames) {
            Class<?> mainClass = applicationContext.getType(beanName);
            if (mainClass == null) {
                continue;
            }
            Class<?> clazz = ClassUtils.getUserClass(mainClass);
            SpringBootApplication bootApp = clazz.getAnnotation(SpringBootApplication.class);
            if (bootApp.scanBasePackages().length > 0) {
                packages.addAll(Arrays.stream(bootApp.scanBasePackages())
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()));
            }else if (bootApp.scanBasePackageClasses().length > 0) {
                List<String> scanBasePackage = Arrays.asList(bootApp.scanBasePackageClasses()).stream().map(pkgClass -> pkgClass.getPackage().getName()).collect(Collectors.toList());
                packages.addAll(scanBasePackage);
            }else {
                String basePackageName = clazz.getPackage().getName();
                packages.add(basePackageName);
                collectComponentScanPackages(basePackageName, packages);
                collectComponentScansPackages(basePackageName,packages);
            }
        }
        return packages;
    }

    /**
     * 搜集componentScan注解的包
     * @param basePackage 启动类所在的基础包
     * @param packages
     */
    private static void collectComponentScanPackages(String basePackage, Set<String> packages) {
        String[] startupBeanNames = applicationContext.getBeanNamesForAnnotation(ComponentScan.class);
        for (String beanName : startupBeanNames) {
            Class<?> clazz = getMatchedConfigClass(basePackage, beanName);
            if (clazz == null) {
                continue;
            }
            ComponentScan scan = AnnotationUtils.findAnnotation(clazz, ComponentScan.class);
            if (scan == null) {
                continue;
            }
            addScanPackages(packages, scan);
        }
    }

    /**
     * 搜集componentScans注解的包
     * @param basePackage 启动类所在的基础包
     * @param packages
     */
    private static void collectComponentScansPackages(String basePackage, Set<String> packages) {
        String[] startupBeanNames = applicationContext.getBeanNamesForAnnotation(ComponentScans.class);
        for (String beanName : startupBeanNames) {
            Class<?> clazz = getMatchedConfigClass(basePackage, beanName);
            if (clazz == null) {
                continue;
            }
            ComponentScans scans = AnnotationUtils.findAnnotation(clazz, ComponentScans.class);
            if (scans == null) {
                continue;
            }
            ComponentScan[] componentScans = scans.value();
            for (ComponentScan scan : componentScans) {
                addScanPackages(packages, scan);
            }
        }
    }

    /**
     * 获取与启动类同包/子包的配置类
     * @param basePackage
     * @param beanName
     * @return
     */
    private static Class<?> getMatchedConfigClass(String basePackage, String beanName) {
        Class<?> mainClass = applicationContext.getType(beanName);
        if (mainClass == null) {
            return null;
        }
        Class<?> clazz = ClassUtils.getUserClass(mainClass);
        // 只处理与主启动类同包或子包的配置类
        String pkgName = clazz.getPackage().getName();
        if (!(pkgName.equals(basePackage) || pkgName.startsWith(basePackage + "."))) {
            return null;
        }
        return clazz;
    }

    /**
     * 收集包路径
     * @param packages
     * @param scan
     */
    private static void addScanPackages(Set<String> packages, ComponentScan scan) {
        packages.addAll(Arrays.stream(scan.value()).filter(StringUtils::hasText).collect(Collectors.toList()));
        packages.addAll(Arrays.stream(scan.basePackages()).filter(StringUtils::hasText).collect(Collectors.toList()));
        for (Class<?> pkgClass : scan.basePackageClasses()) {
            packages.add(pkgClass.getPackage().getName());
        }
    }
}