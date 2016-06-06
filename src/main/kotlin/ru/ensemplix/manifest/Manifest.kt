package ru.ensemplix.manifest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.util.*

val MANIFESTS = arrayOf("assets", "libraries", "clients", "natives")
val RESOURCES_DIR = File("resources")
val MANIFEST_DIR = File("manifest")
val FILES_DIR = File("files")

fun main(args: Array<String>) {
    println("Creating manifests")

    RESOURCES_DIR.mkdir()
    MANIFEST_DIR.mkdir()
    FILES_DIR.mkdir()

    for(name in MANIFESTS) {
        val dir = File(FILES_DIR, name)
        val manifest = File(MANIFEST_DIR, "$name.json")
        if(!dir.exists()) continue
        val resources = HashMap<File, Resource>()

        for(file in dir.walkTopDown().iterator()) {
            if(file.isDirectory) continue
            val resourceName = file.absolutePath.replace(FILES_DIR.absolutePath, "").replace("\\", "/")

            file.inputStream().use {
                resources.put(file, Resource(resourceName, DigestUtils.md5Hex(it), file.length().toInt()))
            }
        }

        FileWriter(manifest).use {
            GsonBuilder().setPrettyPrinting().create().toJson(resources.values, it)
        }

        for((file, resource) in resources) {
            file.copyTo(File(RESOURCES_DIR, resource.name.replace(file.name, resource.hash)), true)
        }

        println("\rCreated $name manifest from ${resources.size} resources")
    }

    println("Finished")
}

private data class Resource(val name: String, val hash: String, val size: Int)
