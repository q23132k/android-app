package me.echeung.moemoekyun.client.api.v5

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.FavoriteMutation
import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.LoginMutation
import me.echeung.moemoekyun.RegisterMutation
import me.echeung.moemoekyun.RequestSongMutation
import me.echeung.moemoekyun.SearchQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.v5.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import okhttp3.OkHttpClient

class APIClient(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) {

    private val client: ApolloClient

    init {
        // Automatically add auth token to requests
        val authClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder().method(original.method(), original.body())
                    if (authUtil.isAuthenticated) {
                        builder.header("Authorization", authUtil.authTokenWithPrefix)
                    }
                    chain.proceed(builder.build())
                }
                .build()

        client = ApolloClient.builder()
            .serverUrl(Library.API_BASE)
            .okHttpClient(authClient)
            .build()
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    fun authenticate(username: String, password: String, callback: LoginCallback) {
        client.mutate(LoginMutation
                .builder()
                .username(username)
                .password(password)
                .build())
                .enqueue(object : ApolloCall.Callback<LoginMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<LoginMutation.Data>) {
                        Log.d("GraphQL response", response.data()?.login()?.token())
                    }
                })
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     * @param callback Listener to handle the response.
     */
    fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        throw RuntimeException("Not implemented yet")
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        client.mutate(RegisterMutation
                .builder()
                .email(email)
                .username(username)
                .password(password)
                .build())
                .enqueue(object : ApolloCall.Callback<RegisterMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<RegisterMutation.Data>) {
                        Log.d("GraphQL response", response.data()?.register()?.uuid())
                    }
                })
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    fun getUserInfo(callback: UserInfoCallback) {
        client.query(UserQuery
                .builder()
                .username("@me")
                .build())
                .enqueue(object : ApolloCall.Callback<UserQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<UserQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.user()?.username())
                    }
                })
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getUserFavorites(callback: UserFavoritesCallback) {
        client.query(FavoritesQuery
                .builder()
                .username("@me")
                .build())
                .enqueue(object : ApolloCall.Callback<FavoritesQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<FavoritesQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.user()?.favorites()?.favorites()?.toString())
                    }
                })
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     * @param isFavorite Whether the song is currently favorited.
     * @param callback Listener to handle the response.
     */
    fun toggleFavorite(songId: String, isFavorite: Boolean, callback: FavoriteSongCallback) {
        client.mutate(FavoriteMutation
                .builder()
                .id(songId.toInt())
                .build())
                .enqueue(object : ApolloCall.Callback<FavoriteMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<FavoriteMutation.Data>) {
                        Log.d("GraphQL response", response.data()?.favoriteSong()?.id()?.toString())
                    }
                })
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId Song to request.
     * @param callback Listener to handle the response.
     */
    fun requestSong(songId: String, callback: RequestSongCallback) {
        client.mutate(RequestSongMutation
                .builder()
                .id(songId.toInt())
//                .kpop()
                .build())
                .enqueue(object : ApolloCall.Callback<RequestSongMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<RequestSongMutation.Data>) {
                        Log.d("GraphQL response", response.data()?.requestSong()?.id()?.toString())
                    }
                })
    }

    /**
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getSongs(callback: SongsCallback) {
//        client.query(FavoritesQuery
//                .builder()
//                .username("@me")
//                .build())
//                .enqueue(object : ApolloCall.Callback<FavoritesQuery.Data>() {
//                    override fun onFailure(e: ApolloException) {
//                        Log.d("GraphQL failure", e.message.toString())
//                    }
//
//                    override fun onResponse(response: Response<FavoritesQuery.Data>) {
//                        Log.d("GraphQL response", response.data()?.user()?.favorites()?.favorites()?.toString())
//                    }
//                })
        throw RuntimeException("Not implemented yet")
    }

    /**
     * Searches for songs.
     *
     * @param query Search query string.
     * @param callback Listener to handle the response.
     */
    fun search(query: String?, callback: SearchCallback) {
        client.query(SearchQuery
                .builder()
                .query(query!!)
                .build())
                .enqueue(object : ApolloCall.Callback<SearchQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<SearchQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.search()?.toString())
                    }
                })
    }
}