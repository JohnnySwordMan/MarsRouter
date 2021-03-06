package com.mars.infra.router.plugin

import com.mars.infra.router.plugin.visitor.DowngradeImplClassVisitor
import com.mars.infra.router.plugin.visitor.RouterClassVisitor
import com.mars.infra.router.plugin.visitor.ServiceImplClassVisitor
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by JohnnySwordMan on 2022/1/12
 */
object RegisterCodeGenerator {

    fun insertInitCode(routerSet: Set<String>, serviceImplSet: Set<String>, jarFile: File) {
        val oprJar = File(jarFile.parent, jarFile.name.toString() + ".opt")
        if (oprJar.exists()) {
            oprJar.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(oprJar))
        // 操作Router.class所在的jar
        val file = JarFile(jarFile)
        val entries: Enumeration<JarEntry> = file.entries()
        while (entries.hasMoreElements()) {
            val jarEntry: JarEntry = entries.nextElement()
            val entryName: String = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream: InputStream = file.getInputStream(zipEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (entryName == RouterCollector.ROUTER_PATH) {
                // 字节码插桩
                val bytes: ByteArray = createCodeInRouter(routerSet, inputStream)
                jarOutputStream.write(bytes)
            } else if (entryName == RouterCollector.SERVICE_MANAGER_PATH) {
                // 字节码插桩
                val bytes: ByteArray = createServiceImpl(serviceImplSet, inputStream)
                jarOutputStream.write(bytes)
            } else if (entryName == RouterCollector.DOWNGRADE_MANAGER_PATH) {
                val bytes2: ByteArray = createDowngradeImpl(inputStream)
                jarOutputStream.write(bytes2)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        if (jarFile.exists()) {
            jarFile.delete()
        }
        oprJar.renameTo(jarFile)
    }

    private fun createCodeInRouter(routerSet: Set<String>, inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
        val routerClassVisitor = RouterClassVisitor(Opcodes.ASM7, classWriter, routerSet)
        classReader.accept(routerClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    private fun createServiceImpl(
        serviceImplSet: Set<String>,
        inputStream: InputStream
    ): ByteArray {
        val classReader = ClassReader(inputStream)
//        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
        // flag改成0， https://stackoverflow.com/questions/11292701/error-while-instrumenting-class-files-asm-classwriter-getcommonsuperclass
        val classWriter = ClassWriter(classReader, 0)
        val serviceImplClassVisitor = ServiceImplClassVisitor(Opcodes.ASM7, classWriter, serviceImplSet)
        classReader.accept(serviceImplClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    private fun createDowngradeImpl(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, 0)
        val serviceImplClassVisitor = DowngradeImplClassVisitor(Opcodes.ASM7, classWriter)
        classReader.accept(serviceImplClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    fun insertServiceImplMap(serviceImplSet: Set<String>, jarFile: File) {
        val oprJar = File(jarFile.parent, jarFile.name.toString() + ".opt")
        if (oprJar.exists()) {
            oprJar.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(oprJar))
        // 操作ServiceManager.class所在的jar
        val file = JarFile(jarFile)
        val entries: Enumeration<JarEntry> = file.entries()
        while (entries.hasMoreElements()) {
            val jarEntry: JarEntry = entries.nextElement()
            val entryName: String = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream: InputStream = file.getInputStream(zipEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (entryName == RouterCollector.SERVICE_MANAGER_PATH) {
                // 字节码插桩
                val bytes: ByteArray = createServiceImpl(serviceImplSet, inputStream)
                jarOutputStream.write(bytes)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        if (jarFile.exists()) {
            jarFile.delete()
        }
        oprJar.renameTo(jarFile)
    }

    fun insertDowngradeImplMap(jarFile: File) {
        val oprJar = File(jarFile.parent, jarFile.name.toString() + ".opt1")
        if (oprJar.exists()) {
            oprJar.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(oprJar))
        // 操作DowngradeManager.class所在的jar
        val file = JarFile(jarFile)
        val entries: Enumeration<JarEntry> = file.entries()
        while (entries.hasMoreElements()) {
            val jarEntry: JarEntry = entries.nextElement()
            val entryName: String = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream: InputStream = file.getInputStream(zipEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (entryName == RouterCollector.DOWNGRADE_MANAGER_PATH) {
                // 字节码插桩
                val bytes: ByteArray = createDowngradeImpl(inputStream)
                jarOutputStream.write(bytes)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        if (jarFile.exists()) {
            jarFile.delete()
        }
        oprJar.renameTo(jarFile)
    }

    fun insertServiceImplAndDowngradeImplCode(
        serviceImplSet: Set<String>,
        jarFile: File
    ) {
        val oprJar = File(jarFile.parent, jarFile.name.toString() + ".opt2")
        if (oprJar.exists()) {
            oprJar.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(oprJar))
        val file = JarFile(jarFile)
        val entries: Enumeration<JarEntry> = file.entries()
        while (entries.hasMoreElements()) {
            val jarEntry: JarEntry = entries.nextElement()
            val entryName: String = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream: InputStream = file.getInputStream(zipEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (entryName == RouterCollector.SERVICE_MANAGER_PATH) {
                // 字节码插桩
                val bytes: ByteArray = createServiceImpl(serviceImplSet, inputStream)
                jarOutputStream.write(bytes)
            } else if (entryName == RouterCollector.DOWNGRADE_MANAGER_PATH) {
                val bytes2: ByteArray = createDowngradeImpl(inputStream)
                jarOutputStream.write(bytes2)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        if (jarFile.exists()) {
            jarFile.delete()
        }
        oprJar.renameTo(jarFile)
    }
}