package uz.zero.notification.dtos

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}