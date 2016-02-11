/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.silverghost.translator.dex

import org.objectweb.asm.Type
import org.ow2.asmdex.*
import java.io.*
import java.lang.reflect.Modifier
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 */
object MethodDumper {

    fun dumpMethods(archiveFile: File): List<String> {
        val result = ArrayList<String>()
        val zipFile = ZipInputStream(FileInputStream(archiveFile))

        zipFile.use {
            var zipEntry: ZipEntry?
            var dexIndex = 0

            while (true) {
                zipEntry = zipFile.nextEntry

                if (zipEntry == null) {
                    break
                }

                if (zipEntry.name.endsWith(".dex")) {
                    val file = File.createTempFile("DUMPER_METHODS_classes" + dexIndex, "dex")
                    file.deleteOnExit()

                    val fos = FileOutputStream(file)
                    val bytes = ByteArray(1024)
                    var length: Int

                    length = zipFile.read(bytes)

                    while (length >= 0) {
                        fos.write(bytes, 0, length)
                        length = zipFile.read(bytes)
                    }

                    fos.close()
                    val methodsList = fillAnalysis(dexIndex, file)
                    result.addAll(methodsList)

                    dexIndex++
                } else {

                }
            }
            zipFile.close()
        }

        return result;
    }

    @Throws(IOException::class)
    private fun fillAnalysis(dexIndex: Int, file: File): List<String> {
        val result = ArrayList<String>()

        val fis = FileInputStream(file)
        val av = ApkInspectVisitor(result)
        val ar = ApplicationReader(Opcodes.ASM4, fis)
        ar.accept(av, 0)

        return result
    }

    fun writeAllMethods(file: File, allStrings: List<String>) {
        try {
            val writer = FileWriter(file)
            for (str in allStrings) {
                writer.write(str)
            }
            writer.close()
        } catch (ioe: IOException) {

        }
    }

    private class ApkInspectVisitor(private val methodsList: MutableList<String>) : ApplicationVisitor(Opcodes.ASM4) {

        override fun visitClass(access: Int, name: String, signature: Array<String?>?,
                                superName: String, interfaces: Array<String?>?): ClassVisitor {
            return object : ClassVisitor(Opcodes.ASM4) {

                override fun visit(version: Int, access: Int, name: String, signature: Array<String?>?,
                                   superName: String, interfaces: Array<String?>?) {
                    super.visit(version, access, name, signature, superName, interfaces)
                }

                override fun visitMethod(access: Int, name: String, desc: String,
                                         signature: Array<String?>?, exceptions: Array<String?>?): MethodVisitor? {

                    // class format (XYZ)R
                    // dex format RXYZ
                    val builder = StringBuilder()
                    builder.append(Modifier.toString(access))
                    builder.append(" " + ApkInspectVisitor.getDecName(popReturn(desc)))
                    builder.append(" " + name)

                    // using java class convert + types from ASM
                    val parameterTypes = Type.getArgumentTypes("(" + popType(desc) + ")")

                    builder.append("(")

                    var prefix = ""
                    for (pType in parameterTypes) {
                        builder.append(prefix)
                        prefix = ","
                        builder.append(ApkInspectVisitor.getDecName(pType.toString()))
                    }

                    builder.append(")\n")
                    methodsList.add(builder.toString())

                    return super.visitMethod(access, name, desc, signature, exceptions)
                }
            }
        }

        companion object {

            internal fun getDecName(dexType: String): String {
                if (dexType.startsWith("[")) {
                    return getDecName(dexType.substring(1)) + "[]"
                }
                if (dexType.startsWith("L")) {
                    val name = dexType.substring(1, dexType.length - 1)

                    return name.replace('/', '.')
                }

                if (DexlibAdapter.primitiveTypes.containsKey(dexType)) {
                    return DexlibAdapter.primitiveTypes[dexType]!!
                } else {
                    return "void"
                }
            }

            internal fun popType(desc: String): String {
                return desc.substring(nextTypePosition(desc, 0))
            }

            internal fun popReturn(desc: String): String {
                return desc.substring(0, desc.indexOf(popType(desc)))
            }

            internal fun nextTypePosition(desc: String, pos: Int): Int {
                var pos = pos
                while (desc[pos] == '[') pos++
                if (desc[pos] == 'L') pos = desc.indexOf(';', pos)
                pos++
                return pos
            }
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        println(MethodDumper.dumpMethods(File("/Users/bfarber/Desktop/Scenarios/4 APKs/com.android.chrome-52311111.apk")))
    }
}