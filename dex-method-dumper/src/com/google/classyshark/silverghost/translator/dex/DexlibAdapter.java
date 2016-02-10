package com.google.classyshark.silverghost.translator.dex;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bfarber on 10/02/2016.
 */
public class DexlibAdapter {

    public static final Map<String, String> primitiveTypes;
    static {
        primitiveTypes = new HashMap<>();
        primitiveTypes.put("I", "int");
        primitiveTypes.put("V", "void");
        primitiveTypes.put("C", "char");
        primitiveTypes.put("D", "double");
        primitiveTypes.put("F", "float");
        primitiveTypes.put("J", "long");
        primitiveTypes.put("S", "short");
        primitiveTypes.put("Z", "boolean");
        primitiveTypes.put("B", "byte");
    }
}
