package com.google.classyshark.silverghost.translator.dex

import java.util.HashMap

/**
 * Created by bfarber on 10/02/2016.
 */
object DexlibAdapter {

    val primitiveTypes: MutableMap<String, String>

    init {
        primitiveTypes = HashMap<String, String>()
        primitiveTypes.put("I", "int")
        primitiveTypes.put("V", "void")
        primitiveTypes.put("C", "char")
        primitiveTypes.put("D", "double")
        primitiveTypes.put("F", "float")
        primitiveTypes.put("J", "long")
        primitiveTypes.put("S", "short")
        primitiveTypes.put("Z", "boolean")
        primitiveTypes.put("B", "byte")
    }
}
