package app.alessandrotedesco.asana.github.detailsActivity

import app.alessandrotedesco.asana.github.PageInfoModel

/**
 * GraphQL JSON response model to the subscribers query
 */
data class JsonDetailsResponseModel(
    val data: DataModel
) {
    data class DataModel(
        val node: NodeModel
    ) {
        data class NodeModel(
            val watchers: WatchersModel
        ) {
            data class WatchersModel(
                val pageInfo: PageInfoModel,
                val totalCount: Int,
                val nodes: ArrayList<LoginModel>
            ) {
                data class LoginModel(
                    val login: String
                )
            }
        }
    }
}