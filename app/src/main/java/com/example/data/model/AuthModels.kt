package com.example.data.model

data class UserAccount(
    val id: Long = 0,
    val employeeId: Long,
    val email: String,
    val fullName: String,
    val employeeCode: String,
    val createdAt: String
)

sealed class AuthResult {
    data class Success(val user: UserAccount, val employee: Employee) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class RegisterResult {
    data class Success(val user: UserAccount, val employee: Employee) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class ResetPasswordResult {
    data class CodeSent(val message: String, val simulatedCode: String) : ResetPasswordResult()
    data class Success(val message: String) : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}
