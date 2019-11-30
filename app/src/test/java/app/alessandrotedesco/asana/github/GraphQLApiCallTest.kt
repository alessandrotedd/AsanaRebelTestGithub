package app.alessandrotedesco.asana.github

import app.alessandrotedesco.asana.github.api.GraphQlApi
import app.alessandrotedesco.asana.github.detailsActivity.DetailsModel
import app.alessandrotedesco.asana.github.detailsActivity.JsonDetailsResponseModel
import app.alessandrotedesco.asana.github.searchActivity.JsonSearchResponseModel
import app.alessandrotedesco.asana.github.searchActivity.SearchModel
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class GraphQLApiCallTest {

    private val repositorySearchQueryTest = "Hello-World"
    private val repositoryIdTest = "MDEwOlJlcG9zaXRvcnkxMTk0NjAyMzI="
    private val gitHubMediaTypeHeaderResponse = "github.v4; format=json"
    private val httpStatusCode = 200
    private val client = OkHttpClient.Builder()
        .connectTimeout(GraphQlApi.connectionTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(GraphQlApi.connectionTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(GraphQlApi.connectionTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    @Test
    fun testRepositorySearch() {
        val graphQlQuery = SearchModel.getQuery(repositorySearchQueryTest)
        val request = GraphQlApi.getRequest(graphQlQuery)

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            // check status code
            checkStatusCode(response.code())
            // check response body
            checkRepositorySearchResponseBody(response.body())
            // check header
            checkResponseContentType(response.header("X-GitHub-Media-Type",""))
        } else {
            // Log error
            fail("Connection Error: ${request.url()}")
        }
    }

    private fun checkResponseContentType(contentTypeHeader: String?) {
        assertEquals(contentTypeHeader, gitHubMediaTypeHeaderResponse)
    }

    private fun checkRepositorySearchResponseBody(body: ResponseBody?) {
        val response = Gson().fromJson(
            body?.string(),
            JsonSearchResponseModel::class.java
        )

        // check for null values
        assertNotNull(response)
        assertNotNull(response.data)
        assertNotNull(response.data.search)
        assertNotNull(response.data.search.pageInfo)
        assertNotNull(response.data.search.pageInfo.endCursor)
        assertNotNull(response.data.search.pageInfo.hasNextPage)
        assertNotNull(response.data.search.edges)
        assertNotNull(response.data.search.repositoryCount)

        // check for empty list
        assertNotEquals(response.data.search.edges.size, 0)
        // check for null values
        response.data.search.edges.forEach {
            assertNotNull(it.node.description)
            assertNotNull(it.node.forkCount)
            assertNotNull(it.node.id)
            assertNotNull(it.node.name)
            assertNotNull(it.node.owner)
            assertNotNull(it.node.owner.avatarUrl)
        }
    }

    @Test
    fun testSubscribersSearch() {
        val graphQlQuery = DetailsModel.getQuery(repositoryIdTest)
        val request = GraphQlApi.getRequest(graphQlQuery)

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            // check status code
            checkStatusCode(response.code())
            // check response body
            checkSubscribersSearchResponseBody(response.body())
            // check header
            checkResponseContentType(response.header("X-GitHub-Media-Type",""))
        } else {
            // Log error
            fail("Connection Error: ${request.url()}")
        }
    }

    private fun checkSubscribersSearchResponseBody(body: ResponseBody?) {
        val response = Gson().fromJson(
            body?.string(),
            JsonDetailsResponseModel::class.java
        )

        // check for null values
        assertNotNull(response)
        assertNotNull(response.data)
        assertNotNull(response.data.node)
        assertNotNull(response.data.node.watchers)
        assertNotNull(response.data.node.watchers.nodes)
        assertNotNull(response.data.node.watchers.pageInfo)
        assertNotNull(response.data.node.watchers.pageInfo.hasNextPage)
        assertNotNull(response.data.node.watchers.pageInfo.endCursor)
        assertNotNull(response.data.node.watchers.totalCount)

        // check for null or empty values
        response.data.node.watchers.nodes.forEach {
            assertNotNull(it.login)
            assertNotEquals(it.login, "")
        }
    }

    private fun checkStatusCode(responseCode: Int) {
        assertEquals(responseCode, httpStatusCode)
    }
}