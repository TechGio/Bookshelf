package com.example.bookshelf.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.bookshelf.Login
import com.example.bookshelf.MainActivity
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentMyBooksBinding
import com.example.bookshelf.ui.book.BookInfo
import com.example.bookshelf.ui.user.AnnualReadingChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyBooks : Fragment() {
    private lateinit var binding: FragmentMyBooksBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private var num: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("readingChallenge").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    progressReadingChallenge()
                    binding.txtReadingChallenge.visibility = View.GONE
                    binding.setReadingChallengeBtn.visibility = View.GONE
                    binding.goToReadingChallengeBtn.visibility = View.VISIBLE
                } else {
                    binding.goToReadingChallengeBtn.visibility = View.GONE
                    binding.txtReadingChallenge.visibility = View.VISIBLE
                    binding.setReadingChallengeBtn.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.setReadingChallengeBtn.setOnClickListener {
            num = binding.txtReadingChallenge
            if (num!!.text.toString().isEmpty()) {
                num!!.error = "Please enter your goal"
            } else{
                mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("readingChallenge").setValue(
                    AnnualReadingChallenge(num!!.text.toString(), LocalDateTime.now().year.toString())
                )
                (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flWrapper, (activity as MainActivity).getReadingChallengeFragment())
                    addToBackStack(null)
                    commit()
                }
            }
        }

        binding.goToReadingChallengeBtn.setOnClickListener {
            (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as MainActivity).getReadingChallengeFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.read.setOnClickListener {
            (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as MainActivity).getReadFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.currentlyReading.setOnClickListener {
            (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as MainActivity).getCurrentlyReadingFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.wantToRead.setOnClickListener {
            (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as MainActivity).getWantToReadFragment())
                addToBackStack(null)
                commit()
            }
        }
        (activity as MainActivity?)!!.setActionBarTitle("My Books: ${mAuth.currentUser!!.displayName!!.uppercase()}")

        return view
    }

    private fun progressReadingChallenge() {
        var numBookRead = 0
        var numBookChallenge: String?
        var bookRead: ArrayList<BookInfo>? = null

        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Read" && currentBook.getReadDate()!!.contains(LocalDateTime.now().year.toString())){
                        bookRead?.add(currentBook!!)
                        numBookRead++
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyBooks.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })

        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("readingChallenge").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val challenge = snapshot.getValue(AnnualReadingChallenge::class.java)
                    numBookChallenge = challenge!!.getNum()
                    if(challenge!!.getYear() == LocalDateTime.now().year.toString()){
                        numBookChallenge = challenge!!.getNum()
                        binding.goToReadingChallengeBtn.text = "You've read ${numBookRead.toString()} of $numBookChallenge books!"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyBooks.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mAuth.signOut()
        val intent = Intent(this@MyBooks.requireContext(), Login::class.java)
        intent.putExtra("email", "toBeRemoved")
        intent.putExtra("password", "toBeRemoved")
        startActivity(intent)
        (activity as MainActivity?)!!.finish()
        return super.onOptionsItemSelected(item)
    }
}