package fastcampus.part2.chapter10.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fastcampus.part2.chapter10.R
import fastcampus.part2.chapter10.data.ArticleModel
import fastcampus.part2.chapter10.databinding.ItemArticleBinding

class ArticleAdapter(val onItemClicked: (ArticleItem) -> Unit, val bookMarkClicked: (String, Boolean) -> Unit) :
    ListAdapter<ArticleItem, ArticleAdapter.ViewHolder>(
        diffUtil
    ) {

    inner class ViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(articleModel: ArticleItem) {
            binding.titleTextView.text = articleModel.title

            Glide.with(binding.thumbnailImageView)
                .load(articleModel.imageUrl)
                .into(binding.thumbnailImageView)


            binding.root.setOnClickListener {
                onItemClicked(articleModel)
            }

            if (articleModel.isBookMark) {
                binding.bookMarkImageButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
            } else {
                binding.bookMarkImageButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
            }

            binding.bookMarkImageButton.setOnClickListener {
                bookMarkClicked(articleModel.sellerId, articleModel.isBookMark.not())
                articleModel.isBookMark = articleModel.isBookMark.not()

                if (articleModel.isBookMark) {
                    binding.bookMarkImageButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
                } else {
                    binding.bookMarkImageButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
                }
            }



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ArticleItem>() {
            override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
                return oldItem.sellerId == newItem.sellerId
            }

            override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
                return oldItem == newItem
            }


        }
    }

}