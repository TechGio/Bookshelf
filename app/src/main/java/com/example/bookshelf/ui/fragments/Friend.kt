package com.example.bookshelf.ui.fragments

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.BookAdapter
import com.example.bookshelf.Communicator
import com.example.bookshelf.MainActivity
import com.example.bookshelf.databinding.FragmentFriendBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var binding: FragmentFriendBinding
private lateinit var friendRecyclerView: RecyclerView
private lateinit var adapter: BookAdapter
private var booksRead: ArrayList<BookInfo>? = null
private lateinit var mAuth: FirebaseAuth
private var uid: String? = null
private var uidFriend: String? = null
private lateinit var mDBRef: DatabaseReference
private lateinit var communicator: Communicator
var nickname: String? = null

class Friend : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booksRead = ArrayList()
        adapter = BookAdapter(this@Friend.requireContext(), booksRead!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        friendRecyclerView = binding.friendRecyclerView
        friendRecyclerView.layoutManager = LinearLayoutManager(this@Friend.requireContext(), RecyclerView.VERTICAL, false)
        friendRecyclerView.adapter = adapter
        communicator = activity as Communicator
        val items = arrayOf("Sort By", "Title", "Author")
        val arrayList = ArrayAdapter<String>(this@Friend.requireContext(),
            R.layout.simple_spinner_dropdown_item, items)
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortBy.adapter = arrayList
        binding.sortBy.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                orderBookList()
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        mDBRef = FirebaseDatabase.getInstance().getReference()
        nickname = arguments?.getString("nickname")
        (activity as MainActivity?)!!.setActionBarTitle(nickname!!.uppercase())

        binding.removeFriendBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                removeFriendFromDatabase()
                Toast.makeText(this@Friend.requireContext(), "$nickname removed from friend list", Toast.LENGTH_LONG)
                    .show()
                (activity as MainActivity).supportFragmentManager.popBackStackImmediate()
            }
        })
        mDBRef.child("user").orderByChild("nickname").equalTo(nickname).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children)
                    uidFriend = data.child("uid").getValue(String::class.java)
                var numBook = 0
                mDBRef.child("user").child(uidFriend!!).child("book").addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(binding.sortBy.selectedItemPosition == 0){
                            booksRead?.clear()
                            for (postSnapshot in snapshot.children){
                                val currentBook = postSnapshot.getValue(BookInfo::class.java)
                                if(currentBook!!.getShelf() == "Read"){
                                    booksRead?.add(currentBook!!)
                                    numBook++
                                }
                            }
                        } else
                            orderBookList()

                        binding.bookReadTitle.text = "Books Read: ${booksRead!!.size}"
                        adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                communicator.passSearchBook(
                                    booksRead!![position]?.getTitle(), booksRead!![position]?.getSubtitle(), booksRead!![position]?.getAuthors()!!.toString(),
                                    booksRead!![position]?.getPublisher(),  booksRead!![position]?.getPublishedDate(),  booksRead!![position]?.getDescription(),
                                    booksRead!![position]?.getPageCount(),  booksRead!![position]?.getThumbnail(), "noShelf",
                                    booksRead!![position]?.getBid(), "noStars", "noReadDate"
                                )
                            }
                        })
                        adapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@Friend.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Friend.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        return view
    }

    fun removeFriendFromDatabase(){
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("friend").child(nickname!!).removeValue()
    }

    fun orderBookList(){
        var bookReadOld: ArrayList<BookInfo>? = ArrayList()
        for (i in 0 until booksRead!!.size)
            bookReadOld!!.add(booksRead!![i])
        for (i in 0 until booksRead!!.size)
            booksRead!!.removeLast()
        if(binding.sortBy.selectedItemPosition == 1)
            bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getTitle)))
        else if(binding.sortBy.selectedItemPosition == 2)
            bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getAuthors)))
        for (i in 0 until bookReadOld!!.size)
            booksRead!!.add(bookReadOld!!.get(i))
    }
}