package fastcampus.part2.chapter10.ui.myhome

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fastcampus.part2.chapter10.R
import fastcampus.part2.chapter10.databinding.FragmentMypageBinding


class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        setupSignInButton(view)
        setupSignInOutButton(view)
    }

    override fun onStart() {
        super.onStart()

        if (Firebase.auth.currentUser == null) {
            initViewForSignIn()
        } else {
            initViewForSignOut()

        }
    }

    private fun setupSignInButton(view: View) {
        binding.signUpButton.setOnClickListener {

            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(view, "회원가입에 성공했습니다. 로그인 버튼을 눌러주세요.", Snackbar.LENGTH_SHORT)
                            .show()
                    } else {
                        Snackbar.make(
                            view,
                            "회원가입에 실패했습니다. 이미 가입한 이메일일 수 있습니다.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun setupSignInOutButton(
        view: View
    ) {
        binding.signInOutButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()


            if (Firebase.auth.currentUser == null) {
                //로그인
                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(view, "이메일 또는 패스워드를 입력해주세요", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            successSignIn()
                        } else {
                            Snackbar.make(
                                view,
                                "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Firebase.auth.signOut()
                initViewForSignIn()
            }
        }
    }

    private fun initViewForSignOut() {
        binding.emailEditText.setText(Firebase.auth.currentUser?.email)
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isVisible = false
        binding.signInOutButton.text = "로그아웃"
        binding.signUpButton.isEnabled = false
    }

    private fun initViewForSignIn() {
        binding.emailEditText.text.clear()
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.text.clear()
        binding.passwordEditText.isVisible = true
        binding.signInOutButton.text = "로그인"
        binding.signUpButton.isEnabled = true
    }


    private fun successSignIn() {
        if (Firebase.auth.currentUser == null) {
            Toast.makeText(context, "로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        initViewForSignOut()
    }

}