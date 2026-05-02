package com.luisisaza.habitos.data.repository

import com.luisisaza.habitos.data.database.dao.UserDao
import com.luisisaza.habitos.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {

    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun getAllUsersOnce(): List<UserEntity> = dao.getAllUsersOnce()

    suspend fun getUserById(id: Long): UserEntity? = dao.getUserById(id)

    suspend fun getUserByUsername(username: String): UserEntity? = dao.getUserByUsername(username)

    suspend fun getUserByCredentials(username: String, passwordHash: String): UserEntity? =
        dao.getUserByCredentials(username, passwordHash)

    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = dao.deleteUser(user)

    suspend fun getUserCount(): Int = dao.getUserCount()
}
