package app.alessandrotedesco.asana.github.detailsActivity

import app.alessandrotedesco.asana.github.api.ApiView

interface DetailsView : ApiView {
    fun initRepositoryID()
}