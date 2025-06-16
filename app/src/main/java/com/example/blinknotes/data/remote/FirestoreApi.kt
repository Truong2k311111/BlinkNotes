package com.example.blinknotes.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

interface FirestoreApi {
    val db: FirebaseFirestore

    suspend fun <T> addDocument(collection: String, data: T): String
    suspend fun <T> setDocument(collection: String, documentId: String, data: T)
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>)
    suspend fun deleteDocument(collection: String, documentId: String)
    suspend fun <T> getDocument(collection: String, documentId: String, type: Class<T>): T?
    suspend fun <T> getDocuments(collection: String, type: Class<T>): List<T>
    suspend fun <T> queryDocuments(
        collection: String,
        type: Class<T>,
        field: String,
        operator: Query.Direction,
        value: Any
    ): List<T>
}

class FirestoreApiImpl(
    override val db: FirebaseFirestore
) : FirestoreApi {
    private val gson = Gson()

    private fun <T> convertToMap(data: T): Map<String, Any> {
        val json = gson.toJson(data)
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }

    override suspend fun <T> addDocument(collection: String, data: T): String {
        val docRef = db.collection(collection).document()
        docRef.set(convertToMap(data)).await()
        return docRef.id
    }

    override suspend fun <T> setDocument(collection: String, documentId: String, data: T) {
        db.collection(collection).document(documentId).set(convertToMap(data)).await()
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ) {
        db.collection(collection).document(documentId).update(data).await()
    }

    override suspend fun deleteDocument(collection: String, documentId: String) {
        db.collection(collection).document(documentId).delete().await()
    }

    override suspend fun <T> getDocument(collection: String, documentId: String, type: Class<T>): T? {
        val doc = db.collection(collection).document(documentId).get().await()
        return doc.toObject(type)
    }

    override suspend fun <T> getDocuments(collection: String, type: Class<T>): List<T> {
        val snapshot = db.collection(collection).get().await()
        return snapshot.toObjects(type)
    }

    override suspend fun <T> queryDocuments(
        collection: String,
        type: Class<T>,
        field: String,
        operator: Query.Direction,
        value: Any
    ): List<T> {
        val query = db.collection(collection)
            .orderBy(field, operator)
            .whereEqualTo(field, value)
        val snapshot = query.get().await()
        return snapshot.toObjects(type)
    }
} 