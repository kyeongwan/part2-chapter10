package fastcampus.part2.chapter10.ui.bookmark

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import fastcampus.part2.chapter10.R
import fastcampus.part2.chapter10.data.ArticleModel
import fastcampus.part2.chapter10.databinding.FragmentBookmarkBinding
import fastcampus.part2.chapter10.ui.home.ArticleAdapter

class BookMarkArticleFragment : Fragment(R.layout.fragment_bookmark) {

    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var articleAdapter: BookMarkArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentBookmarkBinding = FragmentBookmarkBinding.bind(view)
        binding = fragmentBookmarkBinding
        binding.toolbar.setupWithNavController(findNavController())
        setupRecyclerView()
        fetchBookmarkData()
    }

    private fun setupRecyclerView() {
        articleAdapter = BookMarkArticleAdapter(onItemClicked = { articleModel ->
            findNavController().navigate(
                BookMarkArticleFragmentDirections.actionBookMarkArticleFragmentToArticleFragment(
                    articleModel.sellerId.orEmpty()
                )
            )
        })

        binding.articleRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.articleRecyclerView.adapter = articleAdapter

        fetchBookmarkData()
    }

    private fun fetchBookmarkData() {
        val uid = Firebase.auth.currentUser?.uid.orEmpty()

        Firebase.firestore.collection("bookmark")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val list = it.get("articleIds") as List<*>
                Log.e("aa", "list $list")

                if (list.isNotEmpty()) {
                    Firebase.firestore.collection("article")
                        .whereIn("sellerId", list)
                        .get()
                        .addOnSuccessListener { result ->
                            Log.e(
                                "aa",
                                "get result  ${result.map { article -> article.toObject<ArticleModel>() }}"
                            )
                            articleAdapter.submitList(result.map { article -> article.toObject() })
                        }
                        .addOnFailureListener { exception ->
                            Log.d("aa", "get failed with ", exception)
                        }
                }


            }.addOnFailureListener {
                it.printStackTrace()
                Log.e("aa", "list fail")
            }
    }
}