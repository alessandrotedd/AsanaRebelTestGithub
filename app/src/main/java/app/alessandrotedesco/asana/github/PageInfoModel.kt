package app.alessandrotedesco.asana.github

import com.google.gson.annotations.SerializedName

/**
 * the model to interpret a JSON page info response
 */
data class PageInfoModel(
    @SerializedName("endCursor")
    private val _endCursor: String? = "", // to avoid null value
    val hasNextPage: Boolean
) {
    val endCursor get() = _endCursor ?: ""
}