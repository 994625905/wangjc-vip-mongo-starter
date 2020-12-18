package vip.wangjc.mongo.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author wangjc
 * @title: MongoUtil
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 15:03
 */
public class MongoUtil {

    /**
     * 获取类的字段数组,循环遍历
     * @param withSuperClassFields：(是否直接带有父类)
     * @param clazz
     * @return
     */
    public static Field[] getFieldsDirectly(boolean withSuperClassFields,Class<?> clazz){
        Field[] allFields = null;
        Class<?> searchType = clazz;
        Field[] declaredFields;
        while (searchType != null){
            declaredFields = searchType.getDeclaredFields();
            if(allFields == null){
                allFields = declaredFields;
            }else{
                allFields = append(allFields,declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass():null;
        }
        return allFields;
    }
    /**
     * 追加字段
     * @param buffer
     * @param newElements
     * @return
     */
    private static Field[] append(Field[] buffer,Field... newElements){
        if(isEmpty(buffer)){
            return newElements;
        }else{
            return insert(buffer,buffer.length,newElements);
        }
    }
    /**
     * 字段添加
     * @param array
     * @param index
     * @param newFieldlements
     * @return
     */
    private static Field[] insert(Field[] array,int index,Field... newFieldlements){
        if(isEmpty(newFieldlements)){
            return array;
        }
        if(isEmpty(array)){
            return newFieldlements;
        }
        final int len = length(array);
        if(index < 0){
            index = (index % len) + len;
        }

        Field[] result = newArray(array.getClass().getComponentType(),Math.max(len,index)+newFieldlements.length);
        System.arraycopy(array,0,result,0,Math.min(len,index));
        System.arraycopy(newFieldlements,0,result,index,newFieldlements.length);
        if(index < len){
            System.arraycopy(array,index,result,index+newFieldlements.length,len - index);
        }
        return result;
    }
    /**
     * 获取新的字段数组
     * @param componentType
     * @param newSize
     * @return
     */
    private static Field[] newArray(Class<?> componentType,int newSize){
        return (Field[]) Array.newInstance(componentType,newSize);
    }
    /**
     * 判断类的字段非空
     * @param array
     * @return
     */
    private static boolean isEmpty(Field... array){
        return array == null || array.length == 0;
    }
    /**
     * 获取指定数组类长度，私有
     * @param array
     * @return
     */
    private static int length(Object array) throws IllegalArgumentException{
        if(null == array){
            return 0;
        }else {
            return Array.getLength(array);
        }
    }

}
