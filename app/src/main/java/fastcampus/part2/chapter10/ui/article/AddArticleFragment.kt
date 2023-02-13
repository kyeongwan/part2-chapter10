package fastcampus.part2.chapter10.ui.article

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import fastcampus.part2.chapter10.R
import fastcampus.part2.chapter10.data.ArticleModel
import fastcampus.part2.chapter10.databinding.FragmentAddArticleBinding
import java.util.UUID

class AddArticleFragment : Fragment(R.layout.fragment_add_article) {

    private lateinit var binding: FragmentAddArticleBinding
    private lateinit var viewModel: AddArticleViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false) -> {
                // fine location 권한이 있다.
                startContentProvider()
            }
            permissions.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false) -> {
                startContentProvider()
            }
            else -> {
                Toast.makeText(context, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                // 권한 팝업 생성
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddArticleBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get<AddArticleViewModel>()


        if (viewModel.selectedUri.value == null) {
            permissionCheckAndStartContentProvider()
        }


        binding.photoImageView.setOnClickListener {
            if (viewModel.selectedUri.value == null) {
                permissionCheckAndStartContentProvider()
            }
        }

        binding.deleteButton.setOnClickListener {
            binding.photoImageView.setImageDrawable(null)
            viewModel.updateSelectedUri(null)
            binding.plusIconView.isVisible = true
            binding.deleteButton.isVisible = false
        }

        binding.submitButton.setOnClickListener {
            val descriptionText = binding.descriptionEditText.text.toString()
            showProgress()

            // 중간에 이미지가 있으면 업로드 과정을 추가
            if (viewModel.selectedUri.value != null) {
                val photoUri = viewModel.selectedUri.value ?: return@setOnClickListener
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadArticle(descriptionText, uri)
                    },
                    errorHandler = {
                        Toast.makeText(context, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else {
                Toast.makeText(context, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
                hideProgress()
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(AddArticleFragmentDirections.actionBack())
        }
    }

    private fun permissionCheckAndStartContentProvider() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) -> {
                startContentProvider()
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                startContentProvider()
            }
            else -> {
                requestStoragePermission()
            }
        }
    }

    private fun uploadPhoto(
        uri: Uri,
        successHandler: (String) -> Unit,
        errorHandler: () -> Unit
    ) {
        val fileName = "${UUID.randomUUID()}.png"
        Firebase.storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Firebase.storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }.addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(description: String, imageUrl: String) {
        val articleId = UUID.randomUUID().toString()
        val model = ArticleModel(articleId, description, System.currentTimeMillis(), imageUrl)

        Firebase.firestore.collection("article").document(articleId)
            .set(model)
            .addOnSuccessListener {
                findNavController().navigate(AddArticleFragmentDirections.actionAddArticleFragmentToHomeFragment())
                hideProgress()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                view?.let {
                    Snackbar.make(it, "글 작성에 실패했습니다.", Snackbar.LENGTH_LONG).show()
                }
                hideProgress()
            }
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {

                val uri = it?.data?.data
                if (uri != null) {
                    binding.photoImageView.setImageURI(uri)
                    viewModel.updateSelectedUri(uri)
                    binding.plusIconView.isVisible = false
                    binding.deleteButton.isVisible = true
                } else {
                    Toast.makeText(context, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }.launch(intent)

    }

    private fun showProgress() {
        binding.progressBar.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBar.isVisible = false
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

}