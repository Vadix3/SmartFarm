package com.example.smartfarm.interfaces

import org.bson.Document

interface DocumentListListener {
    fun getDocuments(result: Boolean, list: ArrayList<Document>?)
}