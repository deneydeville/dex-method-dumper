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
import java.util.zip.ZipInputStream

class MethodDumper {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            println(MethodDumper().dumpMethods(File("/Users/bfarber/Desktop/Scenarios/4 APKs/com.android.chrome-52311111.apk")))
        }
    }

    fun dumpMethods(archiveFile: File): List<String> {
        val result = arrayListOf<String>()
        val zipFile = ZipInputStream(archiveFile.inputStream())

        zipFile.use {
            var dexIndex = 0

            generateSequence { zipFile.nextEntry }.filter { it.name.endsWith(".dex") }.forEach { zipEntry ->
                val tempFile = File.createTempFile("DUMPER_METHODS_classes${dexIndex}", "dex").apply { deleteOnExit() }

                tempFile.outputStream().use { tempOut ->
                    zipFile.forEachBlock { bytes, length ->
                        tempOut.write(bytes, 0, length)
                    }
                }

                result.addAll(fillAnalysis(dexIndex, tempFile.inputStream()))
                dexIndex++
            }

            return result;
        }
    }

    @Throws(IOException::class)
    private fun fillAnalysis(dexIndex: Int, fis: InputStream): List<String> {
        val results = arrayListOf<String>()
        val av = ApkInspectVisitor(results)
        val ar = ApplicationReader(Opcodes.ASM4, fis)
        ar.accept(av, 0)
        return results
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
                    builder.append(" " + getDecName(popReturn(desc)))
                    builder.append(" " + name)

                    // using java class convert + types from ASM
                    val parameterTypes = Type.getArgumentTypes("(${popType(desc)})")

                    builder.append("(")

                    var prefix = ""
                    for (pType in parameterTypes) {
                        builder.append(prefix)
                        prefix = ","
                        builder.append(getDecName(pType.toString()))
                    }

                    builder.append(")\n")
                    methodsList.add(builder.toString())

                    return super.visitMethod(access, name, desc, signature, exceptions)
                }
            }
        }

        private fun getDecName(dexType: String): String {
            return when {
                dexType.startsWith("[") -> getDecName(dexType.substring(1)) + "[]"
                dexType.startsWith("L") -> dexType.substring(1, dexType.length - 1).replace('/', '.')
                DexlibAdapter.primitiveTypes.containsKey(dexType) -> DexlibAdapter.primitiveTypes[dexType]!!
                else -> "void"
            }
        }

        private fun popType(desc: String): String {
            return desc.substring(nextTypePosition(desc, 0))
        }

        private fun popReturn(desc: String): String {
            return desc.substring(0, desc.indexOf(popType(desc)))
        }

        private fun nextTypePosition(desc: String, startPos: Int): Int {
            var pos = startPos
            while (desc[pos] == '[') {
                pos++
            }
            if (desc[pos] == 'L') {
                pos = desc.indexOf(';', pos)
            }
            pos++
            return pos
        }
    }

    // TODO: move to common utilities
    fun ZipInputStream.forEachBlock(blockSize: Int = 1024, func: (ByteArray, Int) -> Unit) {
        val bytes = ByteArray(blockSize)
        generateSequence {
            val readLen = this.read(bytes)
            if (readLen < 0) null
            else Pair(bytes, readLen)
        }.forEach { func(it.first, it.second) }
    }
}