package api  // Adjust based on your actual package

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")  // Add the JWT token to the Authorization header
            .build()
        return chain.proceed(request)
    }
}
