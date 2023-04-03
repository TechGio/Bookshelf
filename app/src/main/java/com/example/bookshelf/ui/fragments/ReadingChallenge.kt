package com.example.bookshelf.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.BookAdapter
import com.example.bookshelf.Communicator
import com.example.bookshelf.MainActivity
import com.example.bookshelf.databinding.FragmentReadingChallengeBinding
import com.example.bookshelf.ui.book.BookInfo
import com.example.bookshelf.ui.user.AnnualReadingChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.LocalDateTime


private lateinit var binding: FragmentReadingChallengeBinding
private lateinit var readingChallengeRecyclerView: RecyclerView
private lateinit var adapter: BookAdapter
private var bookReadingChallenge: ArrayList<BookInfo>? = null
private lateinit var mAuth: FirebaseAuth
private var uid: String? = null
private lateinit var mDBRef: DatabaseReference
private lateinit var communicator: Communicator
var visible: Boolean = false
var yearReadingChallenge: String? = null

class ReadingChallenge : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookReadingChallenge = ArrayList()
        adapter = BookAdapter(this@ReadingChallenge.requireContext(), bookReadingChallenge!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReadingChallengeBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        communicator = activity as Communicator
        readingChallengeRecyclerView = binding.readingChallengeRecyclerView
        readingChallengeRecyclerView.layoutManager = LinearLayoutManager(this@ReadingChallenge.requireContext(), RecyclerView.VERTICAL, false)
        readingChallengeRecyclerView.adapter = adapter
        (activity as MainActivity?)!!.setActionBarTitle("Your Reading Challenge")
        mDBRef = FirebaseDatabase.getInstance().getReference()
        binding.readingChallengeBanner.text = LocalDateTime.now().year.toString() + " Reading Challenge"

        binding.editGoalTxt.visibility = View.GONE
        binding.cancelBtn.visibility = View.GONE
        binding.editGoalBtn.setOnClickListener {
            if(!visible){
                visible = true
                binding.editGoalTxt.visibility = View.VISIBLE
                binding.editGoalBtn.text = "Save New Goal"
                binding.cancelBtn.visibility = View.VISIBLE
            }
            else{
                if(binding.editGoalTxt.text.toString().isEmpty()){
                    binding.editGoalTxt!!.error = "Please enter your new goal"
                } else{
                    visible = false
                    binding.editGoalTxt.visibility = View.GONE
                    binding.cancelBtn.visibility = View.GONE
                    binding.editGoalBtn.text = "Edit Goal"
                    mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("readingChallenge").setValue(
                        AnnualReadingChallenge(binding.editGoalTxt.text.toString(), yearReadingChallenge))
                }
                binding.editGoalTxt.text.clear()
            }
        }
        binding.cancelBtn.setOnClickListener {
            visible = false
            binding.editGoalTxt.visibility = View.GONE
            binding.cancelBtn.visibility = View.GONE
            binding.editGoalBtn.text = "Edit Goal"
            binding.editGoalTxt.text.clear()
        }

        progressReadingChallenge()
        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookReadingChallenge?.clear()
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Read" && currentBook!!.getReadDate()!!.contains(LocalDate.now().year.toString()))
                        bookReadingChallenge?.add(currentBook!!)
                }
                adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {}
                })
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadingChallenge.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        return view
    }

    private fun progressReadingChallenge() {
        var numBookRead: Int = 0
        var numBookChallenge: String?
        var bookRead: ArrayList<BookInfo>? = null

        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Read" && currentBook.getReadDate()!!.contains(
                            LocalDateTime.now().year.toString())){
                        bookRead?.add(currentBook!!)
                        numBookRead++
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadingChallenge.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })

        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("readingChallenge").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val challenge = snapshot.getValue(AnnualReadingChallenge::class.java)
                    yearReadingChallenge = challenge!!.getYear()
                    numBookChallenge = challenge!!.getNum()
                    binding.progressReadingChallenge.text = "You've read ${numBookRead.toString()} of $numBookChallenge books!"
                    binding.progressBar.max = numBookChallenge!!.toInt()
                    binding.progressBar.progress = numBookRead
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadingChallenge.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
    }
}