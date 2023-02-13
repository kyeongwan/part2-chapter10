package fastcampus.part2.chapter10.ui.home

data class ArticleItem(
    val sellerId: String,
    val title: String,
    var isBookMark: Boolean,
    val imageUrl: String
)