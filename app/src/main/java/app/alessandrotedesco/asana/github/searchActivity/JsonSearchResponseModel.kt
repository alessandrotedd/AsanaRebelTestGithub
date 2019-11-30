package app.alessandrotedesco.asana.github.searchActivity

import app.alessandrotedesco.asana.github.PageInfoModel

/**
 * GraphQL JSON response model to the search query
 */
data class JsonSearchResponseModel(
    val data: DataModel
) {
    data class DataModel(
        val search: DataSearchModel
    ) {
        data class DataSearchModel(
            val repositoryCount: Int,
            val pageInfo: PageInfoModel,
            val edges: ArrayList<EdgeModel>
        ) {
            data class EdgeModel(
                val node: RepositoryModel
            ) {
                data class RepositoryModel(
                    val id: String,
                    val name: String,
                    val description: String,
                    val forkCount: Int,
                    val owner: OwnerModel
                ) {
                    data class OwnerModel(
                        val avatarUrl: String
                    )
                }
            }
        }
    }
}