package org.soraworld.treasure.util;

import java.util.ArrayList;
import java.util.Collections;

public class ListUtils {

    @SafeVarargs
    public static <T> ArrayList<T> arrayList(T... elements) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }
}
