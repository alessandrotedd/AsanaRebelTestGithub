package app.alessandrotedesco.asana.github.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

class GraphQlApi {
    companion object {

        /**
         * get github OkHTTP request given a single query
         * @param graphQlQuery the query given
         * @return an OkHTTP request ready to be called
         */
        fun getRequest(graphQlQuery: String): Request {
            val mediaType = MediaType.parse("application/graphql")
            val body = RequestBody.create(mediaType, graphQlQuery)
            return Request.Builder()
                .url(gitHubGraphQlEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        /**
         * connection timeout, in seconds
         */
        const val connectionTimeoutSeconds = 20L
        /**
         * GitHub GraphQL API v4 endpoint
         */
        private const val gitHubGraphQlEndpoint = "https://api.github.com/graphql"
        /**
         * GitHub API token
         */
        private const val token = Secret.token // not added to Git for security reasons
        /**
         * repositories loaded per page
         */
        const val repositoriesPerPage = 20
        /**
         * subscribers loaded per page
         */
        const val subscribersPerPage = 30
    }
}