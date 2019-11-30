package app.alessandrotedesco.asana.github.detailsActivity

import android.util.Log
import app.alessandrotedesco.asana.github.api.GraphQlApi
import app.alessandrotedesco.asana.github.withoutUnnecessaryCharacters
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.security.auth.callback.Callback
import app.alessandrotedesco.asana.github.api.GraphQlApi.Companion.connectionTimeoutSeconds
import app.alessandrotedesco.asana.github.api.GraphQlApi.Companion.subscribersPerPage

class DetailsModel {

    private val client = OkHttpClient.Builder()
        .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    /**
     * search subscribers
     * @param repositoryId the queried repository id
     * @param cursor the page cursor
     * @param listener the listener to call on success / failure
     */
    fun searchSubscribers(repositoryId: String, cursor: String, listener: OnDetailsFinishedListener) {
        val graphQlQuery = getQuery(repositoryId, cursor)
        val request = GraphQlApi.getRequest(graphQlQuery)

        // send the request to server
        makeDetailsCall(client.newCall(request), listener)
    }

    companion object {
        /**
         * get subscribers query
         * @param repositoryId the repository ID to search
         * @param cursor (optional) the page cursor
         */
        fun getQuery(repositoryId: String, cursor: String = ""): String {
            // add optional page cursor to the query
            val after = if (cursor == "")
                "null" // first page
            else
                "\\\"$cursor\\\"" // nth page
            return """
            {"query":
                "query {
                    node(id: \"$repositoryId\") {
                        ... on Repository {
                            watchers(first: $subscribersPerPage, after: $after) {
                                pageInfo {
                                    endCursor
                                    hasNextPage
                                }
                                totalCount
                                nodes {
                                    login
                                }
                            }
                        }
                    }
                }"
            }
        """.withoutUnnecessaryCharacters() // to reduce request body size
        }
    }

    /**
     * makes a call in order to get a list of subscribers
     * @param call the call to make / retry
     * @param listener the listener to call after the call
     */
    fun makeDetailsCall(call: Call, listener: OnDetailsFinishedListener) {
        // clone call in order to call it again with a "retry" button
        val callClone = call.clone()
        call.enqueue(object : Callback, okhttp3.Callback {
            // connection failed
            override fun onFailure(call: Call, e: IOException) {
                // Log error
                e.printStackTrace()
                Log.e("DetailsModel", "Connection Error: ${e.message}")

                // Show no connection error with a "Retry" option
                listener.onError(callClone)
            }

            // connection successful
            override fun onResponse(call: Call, response: Response) {
                listener.onSuccess(response.body()?.string())
            }
        })
    }

    interface OnDetailsFinishedListener {
        fun onSuccess(jsonResponse: String?)
        fun onError(call: Call)
    }
}