package com.example.smartfarm.utils

import android.content.ContentValues

import android.os.ParcelFileDescriptor

import android.content.UriMatcher

import android.content.ContentProvider
import android.database.Cursor
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException


class CachedFileProvider() : ContentProvider() {
    private var uriMatcher: UriMatcher? = null

    override fun onCreate(): Boolean {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher!!.addURI(AUTHORITY, "*", 1)
        return true
    }


    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return when (uriMatcher!!.match(uri)) {
            1 -> {
                val cacheDir = context!!.cacheDir
                val separator = File.separator
                val lastPathSegment = uri.lastPathSegment
                val fileLocation = "${cacheDir}${separator}${lastPathSegment}"
                ParcelFileDescriptor.open(
                    File(fileLocation),
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            }
            else -> throw FileNotFoundException("Unsupported uri: $uri")
        }
    }

    companion object {
        const val AUTHORITY = "com.example.smartfarm.provider"
    }
}