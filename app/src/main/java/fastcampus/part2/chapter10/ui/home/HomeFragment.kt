package fastcampus.part2.chapter10.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import fastcampus.part2.chapter10.R
import fastcampus.part2.chapter10.databinding.FragmentHomeBinding
import fastcampus.part2.chapter10.data.ArticleModel
import fastcampus.part2.chapter10.ui.article.AddArticleFragmentDirections

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var articleAdapter: ArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        setupArticleWriteButton(view)
        setupRecyclerView()
        setupBookMarkButton()
    }

    private fun setupRecyclerView(
    ) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToArticleFragment(
                    articleModel.sellerId
                )
            )
        }, bookMarkClicked = { articleId, isBookMark ->
            Firebase.firestore.collection("bookmark").document(uid)
                .update(
                    "articleIds",
                    if (isBookMark) {
                        FieldValue.arrayUnion(articleId)
                    } else {
                        FieldValue.arrayRemove(articleId)
                    }
                )
        })
        binding.articleRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.articleRecyclerView.adapter = articleAdapter

        getArticleList(uid)
    }

    private fun setupBookMarkButton() {
        binding.bookMarkImageButton.setOnClickListener {
            val uid = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBookMarkArticleFragment())
        }
    }

    private fun getArticleList(uid: String) {
        Firebase.firestore.collection("bookmark")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val bookMarkLists = it.get("articleIds") as List<*>


                Firebase.firestore.collection("article")
                    .get()
                    .addOnSuccessListener { result ->

                        val list = result.map { it.toObject<ArticleModel>() }
                            .map { model ->
                                ArticleItem(
                                    sellerId = model.sellerId.orEmpty(),
                                    title = model.title.orEmpty(),
                                    isBookMark = bookMarkLists.contains(model.sellerId),
                                    imageUrl = model.imageUrl.orEmpty(),
                                )
                            }
                        articleAdapter.submitList(list)
                    }
                    .addOnFailureListener { exception ->
                        Log.d("aa", "get failed with ", exception)
                    }


            }
            .addOnFailureListener { exception ->
                Log.d("aa", "get failed with ", exception)
            }
    }

    private fun setupArticleWriteButton(
        view: View
    ) {
        binding.addFloatingButton.setOnClickListener {
            context?.let {
                if (Firebase.auth.currentUser != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToAddArticleFragment()
                    findNavController().navigate(action)
                } else {
                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
