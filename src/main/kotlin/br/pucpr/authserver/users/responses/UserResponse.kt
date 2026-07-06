package br.pucpr.authserver.users.responses

import br.pucpr.authserver.users.User

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val bio: String,
    val avatar: String
) {
    constructor(user: User, avatarUrl: String) : this(
        id = user.id!!,
        email = user.email,
        name = user.name,
        bio = user.bio,
        avatar = avatarUrl
    )
}