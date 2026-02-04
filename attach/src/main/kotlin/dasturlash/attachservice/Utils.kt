package dasturlash.attachservice

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class GenerateHash(){
    fun generateHash(): String{
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..10)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
        return randomString
    }
}

@Component
class SecurityUtil{

    fun getCurrentUserId(): Long{
        val authentication = SecurityContextHolder.getContext().authentication

        if(authentication is JwtAuthenticationToken){
            val userId = authentication.tokenAttributes["uid"].toString()
            return userId.toLong()
        }
        throw Exception("Invalid token")
    }

    fun getCurrentUserRole(): UserRole{
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is JwtAuthenticationToken){
            val userRole = authentication.tokenAttributes[JWT_ROLE_KEY] as UserRole
            return userRole
        }
        throw Exception("Invalid token")
    }

}