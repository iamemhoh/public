/*
Copyright 2015 Shriram Mani, Ironpillars, Inc.

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.ipillars.sorter;

import com.ipillars.common.PojoTool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Shriram Mani, Ironpillars, Inc on 9/18/2015.
 */
public class ListSorter {

    public List sortList(List list, List<String> fieldNames) {

        List retval = null;

        // Get the class name of the objects in the collection
        Class aClass = getElementClassName(list);

        SortHelper sortHelper = new SortHelper();

        Integer recNo = 0;
        for (Object object : list) {

            List sortingKeys = new ArrayList<>();

            // Loop through the field names to sort by and sort at each stage
            for (String fieldName : fieldNames) {

                Boolean useGetter = false, useField = false;
                Method getterMethod = null;
                Field valueField = null;

                // Lets see if there is a method for the field. If so just use it.
                try {

                    String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    getterMethod = aClass.getMethod(methodName);

                    if (getterMethod != null) {

                        // If not set then IllegalAccessException is thrown
                        if (!getterMethod.isAccessible()) {
                            getterMethod.setAccessible(true);
                        }

                        useGetter = true;
                    }
                } catch (NoSuchMethodException e) {
                    useGetter = false;
                }

                // If there is no getter method, then use the field itself to sort
                if (!useGetter) {

                    useField = false;

                    // Get the field first direct.
                    valueField = PojoTool.getFieldByName(aClass, fieldName);

                    // Get the data type of the field
                    Class dataType = valueField.getType();

                    // Simple Object
                    if (PojoTool.isSimpleObject(dataType)) {
                        useField = true;

                        // If not set then IllegalAccessException is thrown
                        if (!valueField.isAccessible()) {
                            valueField.setAccessible(true);
                        }

                    } else {
                        useField = false;
                        throw new RuntimeException("Cannot sort the list on a complex type : " + dataType);
                    }
                }

                try {
                    if (useGetter) {
                        sortingKeys.add(getterMethod.invoke(object));
                    } else if (useField) {
                        sortingKeys.add(valueField.get(object));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            sortHelper.addObjectsToSort(recNo++, sortingKeys);
        }

        sortHelper.sort();

        if (retval == null) {
            retval = new ArrayList<>();
        }

        for (Integer index : sortHelper.getSortedIndexList()) {
            retval.add(list.get(index));
        }
        return retval;
    }

    public Class getElementClassName(Collection list) {

        Class retval = null;

        if (list != null) {
            Iterator iterator = list.iterator();
            Object first = iterator.next();
            retval = first.getClass();
        }

        return retval;
    }
}
