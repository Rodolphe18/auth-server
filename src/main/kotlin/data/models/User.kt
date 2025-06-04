package com.francotte.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class User(val userId:Long, val username:String, @Transient val password:String = "",@Transient @BsonId val bsonId:ObjectId = ObjectId(), val favoriteIds:List<String> = emptyList())