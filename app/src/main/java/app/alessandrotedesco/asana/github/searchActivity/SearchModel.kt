package app.alessandrotedesco.asana.github.searchActivity

import android.util.Log
import app.alessandrotedesco.asana.github.api.GraphQlApi
import app.alessandrotedesco.asana.github.api.GraphQlApi.Companion.connectionTimeoutSeconds
import app.alessandrotedesco.asana.github.api.GraphQlApi.Companion.repositoriesPerPage
import app.alessandrotedesco.asana.github.withoutUnnecessaryCharacters
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.security.auth.callback.Callback

class SearchModel {

    private val client = OkHttpClient.Builder()
        .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    /**
     * search repositories
     * @param repositoryName the queried repository name
     * @param cursor the page cursor
     * @param listener the listener to call on success / failure
     */
    fun searchRepositories(repositoryName: String, cursor: String, listener: OnSearchFinishedListener) {
        val graphQlQuery = getQuery(repositoryName, cursor)
        val request = GraphQlApi.getRequest(graphQlQuery)

        // send the request to server
        makeSearchCall(client.newCall(request), listener)
    }

    companion object {
        /**
         * get repositories query
         * @param repositoryName the repository name to search
         * @param cursor (optional) the page cursor
         */
        fun getQuery(repositoryName: String, cursor: String = ""): String {
            // add optional page cursor to the query
            val after = if (cursor == "")
                "null" // first page
            else
                "\\\"$cursor\\\"" // nth page
            return """
                {"query" : "
                    query {
                        search(query:\"$repositoryName\", type: REPOSITORY, first: $repositoriesPerPage, after: $after) {
                            repositoryCount
                            pageInfo {
                                endCursor
                                hasNextPage
                            }
                            edges {
                                node {
                                    ... on Repository {
                                        id
                                        name
                                        description
                                        forkCount
                                        owner {
                                            avatarUrl
                                        }
                                    }
                                }
                            }
                        }
                    }
                "}
            """.withoutUnnecessaryCharacters() // to reduce request body size
        }
    }

    /**
     * makes a call in order to get a list of repositories
     * @param call the call to make / retry
     * @param listener the listener to call after the call
     */
    fun makeSearchCall(call: Call, listener: OnSearchFinishedListener) {
        val callClone = call.clone()
        call.enqueue(object : Callback, okhttp3.Callback {
            // connection failed
            override fun onFailure(call: Call, e: IOException) {
                // Log error
                e.printStackTrace()
                Log.e("SearchModel", "Connection Error: ${e.message}")

                // Show no connection error with a "Retry" option
                listener.onError(callClone)
            }

            // connection successful
            override fun onResponse(call: Call, response: Response) {
                listener.onSuccess(response.body()?.string())
            }
        })
    }

    interface OnSearchFinishedListener {
        fun onSuccess(jsonResponse: String?)
        fun onError(call: Call)
    }
}