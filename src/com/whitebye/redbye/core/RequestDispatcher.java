package com.whitebye.redbye.core;

import com.whitebye.redbye.annotation.Forward;
import com.whitebye.redbye.annotation.HandleMethod;
import com.whitebye.redbye.annotation.RequestAction;
import com.whitebye.redbye.annotation.Return;
import com.whitebye.redbye.util.BeanUtil;
import javassist.NotFoundException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Administrator on 2015/10/20 0020.
 */

@WebServlet(name="RequestDispatcher",urlPatterns={"/"},
        initParams={@WebInitParam(name="action_package",value="com.whitebye.test")})
public class RequestDispatcher extends HttpServlet {

    /**
     * all action with RequestAction Annotation
     */
    private List<Class> requestActionList = new ArrayList<Class>(0);

    /**
     * default "/"
     */
    private String actionPath = "/";

    /**
     * GET,POST,PUT,DELETE default GET
     */
    private RequestMethod method = RequestMethod.GET;

    /**
     * the field must fill
     */
    private String handleMethodPath = "";

    /**
     * FORWARD or REDIRECT
     */
    private ForwardType forwardType = ForwardType.FORWARD;

    /**
     * the url is userful to forward
     */
    private String forwardUrl = "";

    /**
     * return type enum
     */
    private ReturnType returnType = ReturnType.OBJECT;

    /**
     * choose fill, while ReturnType is OBJECT
     */
    private String returnAttrName = "";

    private static String ACTION_PACKAGE_NAME = "action_package";

    @Override
    public void init() throws ServletException {
        super.init();

        //scan all RequestAction
        System.out.println("初始化一次");
        requestActionList = scanRequestActions(getInitParameter(ACTION_PACKAGE_NAME));
    }

    /**
     * scan all RequestAction
     */
    private List<Class> scanRequestActions(String packageName) {
        List<Class> classList = BeanUtil.scanClassFromPackage(packageName);
        return BeanUtil.filterContainAnnotation(classList, RequestAction.class);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //get servletPath
        String servletPath = request.getServletPath();

        //match RequestAction
        Class requestAction = matchRequestAction(servletPath);
        //match handle method
        String actionPath = getRequestActionPath(requestAction);

        Method handleMethod = matchHandleMethod(requestAction, getMethodPath(servletPath, actionPath));

        Object methodParamsAfterCast[] = initMethodParams(request, handleMethod, requestAction);

        //initial Parameters
        initAnnotationParams(requestAction, handleMethod);

        //invoke handle method
        try {
            if(handleMethod != null) {
                Object returnObj = handleMethod.invoke(requestAction.newInstance(), methodParamsAfterCast);
                //judge returnObj is Object, Map or String
                switch (returnType) {
                    case MAP:
                        if (returnObj instanceof Map) {
                            handleReturnObjForMap(request, (Map) returnObj);
                        } else {
                            //throw parameter exception, parameter is not matched
                        }
                        break;
                    case STRING:
                        if (returnObj instanceof String) {
                            handleReturnObjForString(response, String.valueOf(returnObj));
                        } else {
                            //throw parameter exception, parameter is not matched
                        }
                        break;
                    case OBJECT:
                        handleReturnObjForObject(request, returnObj);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        //handel return
        handleReturn(request, response);

    }

    private Object[] initMethodParams(HttpServletRequest request, Method handleMethod, Class requestAction) {

        try {
            String variableNames[] = BeanUtil.getLocalVariableNames(requestAction, handleMethod);

            Class variableTypes[] = handleMethod.getParameterTypes();
            if(variableNames == null || variableTypes == null || variableNames.length != variableTypes.length || variableNames.length == 0){
                //throw argumentException
            }
            Object result[] = new Object[variableTypes.length];

            for (int i = 0; i < variableTypes.length; i++){
                Class variableType = variableTypes[i];

                if(variableType == int.class || variableType == Integer.class){
                    String intTemp = request.getParameter(variableNames[i]);
                    if (intTemp != null){
                        result[i] = Integer.valueOf(intTemp);
                    }else{
                        result[i] = 0;
                    }
                }else if(variableType == float.class || variableType == Float.class){
                    String floatTemp = request.getParameter(variableNames[i]);
                    if (floatTemp != null){
                        result[i] = Float.valueOf(floatTemp);
                    }else{
                        result[i] = 0.0f;
                    }
                }else if(variableType == double.class || variableType == Double.class){
                    String doubleTemp = request.getParameter(variableNames[i]);
                    if (doubleTemp != null){
                        result[i] = Double.valueOf(doubleTemp);
                    }else{
                        result[i] = 0.0;
                    }
                }else if(variableType == short.class || variableType == Short.class){
                    String shortTemp = request.getParameter(variableNames[i]);
                    if (shortTemp != null){
                        result[i] = Short.valueOf(shortTemp);
                    }else{
                        result[i] = (short)0;
                    }
                }else if(variableType == long.class || variableType == Long.class){
                    String longTemp = request.getParameter(variableNames[i]);
                    if (longTemp != null){
                        result[i] = Long.valueOf(longTemp);
                    }else{
                        result[i] = 0l;
                    }
                }else if(variableType == boolean.class || variableType == Boolean.class){
                    String booleanTemp = request.getParameter(variableNames[i]);
                    if (booleanTemp != null){
                        result[i] = Boolean.valueOf(booleanTemp);
                    }else{
                        result[i] = false;
                    }
                }else if(variableType == char.class || variableType == Character.class){
                    String charTemp = request.getParameter(variableNames[i]);
                    if (charTemp != null){
                        result[i] = charTemp.toCharArray()[0];
                    }
                }else if(variableType == byte.class || variableType == Byte.class){
                    String byteTemp = request.getParameter(variableNames[i]);
                    if (byteTemp != null){
                        result[i] = byteTemp.getBytes()[0];
                    }
                }else if(variableType == String.class){
                    result[i] = request.getParameter(variableNames[i]);
                }else{
                    result[i] = getObjectByRecursion(request, variableType, variableNames[i]);
                }
            }
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getObjectByRecursion(HttpServletRequest request, Class paramType, String paramName) throws IllegalAccessException, InstantiationException {

        Field fields[] = paramType.getDeclaredFields();

        Object object = paramType.newInstance();
        for (int i = 0; i < fields.length; i++){
            Field field = fields[i];
            Class variableType = field.getType();
            String temp = request.getParameter(paramName + "." + field.getName());

            if(variableType == int.class || variableType == Integer.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Integer.valueOf(temp));
            }else if(variableType == float.class || variableType == Float.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Float.valueOf(temp));
            }else if(variableType == double.class || variableType == Double.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Double.valueOf(temp));
            }else if(variableType == short.class || variableType == Short.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Short.valueOf(temp));
            }else if(variableType == long.class || variableType == Long.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Long.valueOf(temp));
            }else if(variableType == boolean.class || variableType == Boolean.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, Boolean.valueOf(temp));
            }else if(variableType == char.class || variableType == Character.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, temp.toCharArray()[0]);
            }else if(variableType == byte.class || variableType == Byte.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, temp.getBytes()[0]);
            }else if(variableType == String.class){
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, temp);
            }else{
                Object obj = getObjectByRecursion(request, field.getType(), field.getName());
                BeanUtil.setFieldByMethod(object, getMethodName(field.getName()), variableType, obj);
            }

        }
        return object;
    }

    private String getMethodName(String fieldName) {
        return new StringBuffer("set").append(firstCharToUpper(fieldName)).toString();
    }

    private String firstCharToUpper(String fieldName) {
        char[] chars = fieldName.toCharArray();
        chars[0]-=32;
        return String.valueOf(chars);
    }

    private String[] getRequestNames(String[] variableMethodNames, String variableName) {

        return new String[0];
    }

    private String getMethodPath(String servletPath, String actionPath) {
        if (servletPath == null || actionPath == null){
            return null;
        }
        return servletPath.substring(actionPath.length());
    }

    private String getRequestActionPath(Class requestAction) {
        if(requestAction == null){
            return null;
        }

        Annotation annotation = requestAction.getAnnotation(RequestAction.class);
        if (annotation != null){
            return ((RequestAction) annotation).path();
        }
        return null;
    }

    private void handleReturn(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"".equals(forwardUrl)){
            switch (forwardType){
                case FORWARD:
                    request.getRequestDispatcher(forwardUrl).forward(request, response);
                    break;
                case REDIRECT:
                    response.sendRedirect(forwardUrl);
                    break;
            }
        }
    }

    private void initAnnotationParams(Class requestAction, Method handleMethod) {
        if(handleMethod == null){
            return;
        }

        Annotation requestActionAnnotationTemp = requestAction.getAnnotation(RequestAction.class);
        if (requestActionAnnotationTemp instanceof RequestAction){
            RequestAction requestActionAnnotation = (RequestAction) requestActionAnnotationTemp;
            actionPath = requestActionAnnotation.path();
        }

        Annotation handleMethodAnnotationTemp = handleMethod.getAnnotation(HandleMethod.class);
        if (handleMethodAnnotationTemp instanceof HandleMethod){
            HandleMethod handleMethodAnnotation = (HandleMethod) handleMethodAnnotationTemp;
            method = handleMethodAnnotation.method();
            handleMethodPath = handleMethodAnnotation.path();
        }

        Annotation forwardAnnotationTemp = handleMethod.getAnnotation(Forward.class);
        if (forwardAnnotationTemp instanceof Forward){
            Forward forwardAnnotation = (Forward) forwardAnnotationTemp;
            forwardType = forwardAnnotation.type();
            forwardUrl = forwardAnnotation.url();
        }

        Annotation returnAnnotationTemp = handleMethod.getAnnotation(Return.class);
        if (returnAnnotationTemp instanceof Return){
            Return returnAnnotation = (Return) returnAnnotationTemp;
            returnType = returnAnnotation.type();
            returnAttrName = returnAnnotation.name();
        }
    }

    private void handleReturnObjForObject(HttpServletRequest request, Object returnObj) {
        request.setAttribute(returnAttrName, returnObj);
    }

    private void handleReturnObjForString(HttpServletResponse response, String resultStr) throws IOException {
        response.getWriter().print(resultStr);
    }

    private void handleReturnObjForMap(HttpServletRequest request, Map returnMap) {
        Iterator<Entry> attrIter = returnMap.entrySet().iterator();
        while (attrIter.hasNext()){
            Entry attr = attrIter.next();
            request.setAttribute(String.valueOf(attr.getKey()), attr.getValue());
        }
    }

    private Method matchHandleMethod(Class requestAction, String methodPath) {
        if (requestAction == null || methodPath == null){
            return null;
        }

        Method[] methods = requestAction.getMethods();
        for (Method method : methods){
            Annotation methodAnnotation = method.getAnnotation(HandleMethod.class);
            if (methodAnnotation != null){
                HandleMethod handleMethod = (HandleMethod) methodAnnotation;
                if (handleMethod.path().equals(methodPath)){
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * match dest RequestAction
     */
    private Class matchRequestAction(String servletPath) {
        if(requestActionList == null || requestActionList.isEmpty()){
            return null;
        }

        for (Class requestActionClass : requestActionList){
            Annotation annotation = requestActionClass.getAnnotation(RequestAction.class);
            if (annotation != null){
                RequestAction requestActionAnnotation = (RequestAction) annotation;
                if (servletPath.startsWith(requestActionAnnotation.path())){
                    return requestActionClass;
                }
            }
        }
        return null;
    }
}
