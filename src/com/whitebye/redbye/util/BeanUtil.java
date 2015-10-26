package com.whitebye.redbye.util;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/21 0021.
 */
public class BeanUtil {

    private static final String CLASS_FILE_SUFFIX = "class";

    public static List<Class> scanClassFromPackage(String packageName) {
        List<Class> result = new ArrayList<Class>();
        if (packageName == null || "".equals(packageName)){
            return result;
        }

        String packageDir = packageName.replace('.', '/');
        File packageFile = new File(BeanUtil.class.getClassLoader().getResource(packageDir).getPath());
        if (!packageFile.exists() || !packageFile.isDirectory()){
            return result;
        }

        List<String> classFileNames = new ArrayList<String>();
        scanSubFiles(packageFile, classFileNames, CLASS_FILE_SUFFIX);

        try {
            if (!classFileNames.isEmpty()){
                for (int i=0; i < classFileNames.size(); i++){
                    Class clazz = BeanUtil.class.getClassLoader().loadClass(classFileNames.get(i));
                    result.add(clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void scanSubFiles(File file, List<String> scanResult, String fileSuffix) {
        if (file == null || !file.exists()){
            return;
        }

        if (file.isDirectory()){
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++ ){
                scanSubFiles(files[i], scanResult, fileSuffix);
            }
        }else{
            if (file.getName().endsWith(fileSuffix)){
                String path = getClassNameWithPkgName(file);
                scanResult.add(path.replace('\\', '.'));
            }
        }
    }

    private static String getClassNameWithPkgName(File file) {
        if (file == null || !file.exists()){
            return null;
        }
        String path = file.getPath();
        String splitWord = "WEB-INF\\classes\\";
        path = path.substring(path.indexOf(splitWord) + splitWord.length(), path.length() - 6);
        return path;
    }

    public static List<Class> filterContainAnnotation(List<Class> classList, Class annotationClass) {
        List<Class> result = new ArrayList<Class>();

        if (classList == null || classList.size() == 0){
            return result;
        }

        for (Class clazz : classList){
            if (clazz.getAnnotation(annotationClass) != null){
                result.add(clazz);
            }
        }

        return result;
    }

    public static String[] getLocalVariableNames(Class clazz, Method method) throws NotFoundException, ClassNotFoundException {
        // instance class pool
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new ClassClassPath(clazz));
        CtClass ctClass = classPool.get(clazz.getName());

        // get method
        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
        // judge the method is static or not
        int staticIndex = Modifier.isStatic(method.getModifiers()) ? 0 : 1;

        // get method parameters
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute)codeAttribute.getAttribute(LocalVariableAttribute.tag);

        String result[] = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            result[i] = localVariableAttribute.variableName(staticIndex + i);
        }
        return result;
    }

    public static String[] getVariableMethodNames(Class requestAction) {
        Method methods[] = requestAction.getDeclaredMethods();
        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods){
            if (method.getName().startsWith("set")){
                methodNames.add(method.getName());
            }
        }
        return methodNames.toArray(new String[0]);
    }

    public static void setFieldByMethod(Object object, String methodName, Class variableType, Object value) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, variableType);
            method.invoke(object, value);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
