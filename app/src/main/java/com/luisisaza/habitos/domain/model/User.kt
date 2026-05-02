package com.luisisaza.habitos.domain.model

import com.luisisaza.habitos.data.database.entity.UserEntity

data class User(
    val id: Long,
    val name: String,
    val username: String,
    val email: String,
    val photoPath: String?,
    val createdAt: Long
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    username = username,
    email = email,
    photoPath = photoPath,
    createdAt = createdAt
)
