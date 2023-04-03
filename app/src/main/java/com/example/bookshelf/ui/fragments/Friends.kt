package com.example.bookshelf.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.*
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentFriendsBinding
import com.example.bookshelf.ui.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Friends : Fragment() {
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: FragmentFriendsBinding
    private lateinit var mDBRef: DatabaseReference
    var visible: Boolean = false
    var userNickname: String? = "null"
    lateinit var usersNicknames: ArrayList<String>
    var searchedNickname = ""
    private lateinit var communicator: Communicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        friendsList = ArrayList()
        usersNicknames = ArrayList()
        adapter = UserAdapter(this@Friends.requireContext(), friendsList)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        communicator = activity as Communicator
        binding.icon.visibility = View.GONE
        binding.noFriends.visibility = View.GONE
        friendsRecyclerView = binding.friendsRecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(this@Friends.requireContext())
        friendsRecyclerView.adapter = adapter
        (activity as MainActivity?)!!.setActionBarTitle("Friends")
        userNickname = mAuth.currentUser!!.displayName
        binding.editFriend.visibility = View.GONE
        binding.cancelBtn.visibility = View.GONE
        binding.searchFriendBtn.setOnClickListener {
            var count = 0
            if(!visible){
                visible = true
                binding.editFriend.visibility = View.VISIBLE
                binding.searchFriendBtn.text = "Confirm"
                binding.cancelBtn.visibility = View.VISIBLE
            } else{
                if(binding.editFriend.text.toString().isEmpty()){
                    binding.editFriend!!.error = "Please enter nickname"
                } else{
                    searchedNickname = binding.editFriend.text.toString().lowercase().trim()
                    mDBRef.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(count == 0){
                                for (postSnapshot in snapshot.children){
                                    val currentUser = postSnapshot.getValue(User::class.java)
                                    if(currentUser!!.nickname != userNickname)
                                        usersNicknames?.add(currentUser!!.nickname!!)
                                }

                                var present = false
                                for(nick in usersNicknames){
                                    if(nick == searchedNickname)
                                        present = true
                                }
                                var presentFriends = false
                                for(nick in friendsList)
                                    if(nick.nickname == searchedNickname)
                                        presentFriends = true
                                if(present && !presentFriends){
                                    count++
                                    mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("friend").child(searchedNickname).setValue(
                                        User(searchedNickname))
                                    visible = false
                                    binding.icon.visibility = View.GONE
                                    binding.noFriends.visibility = View.GONE
                                    binding.editFriend.visibility = View.GONE
                                    binding.cancelBtn.visibility = View.GONE
                                    binding.searchFriendBtn.text = "Add new friend"
                                    Toast.makeText(this@Friends.requireContext(), "$searchedNickname added to your friend list", Toast.LENGTH_SHORT).show()
                                } else if(presentFriends && count == 0)
                                    Toast.makeText(this@Friends.requireContext(), "$searchedNickname is already your friend", Toast.LENGTH_SHORT).show()
                                else if(userNickname == searchedNickname && count == 0)
                                    Toast.makeText(this@Friends.requireContext(), "Can't add yourself as a friend!", Toast.LENGTH_SHORT).show()
                                else
                                    if(count == 0)
                                        Toast.makeText(this@Friends.requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                            }

                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@Friends.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
                        }
                    })
                }
                binding.editFriend.text.clear()
            }
        }
        binding.cancelBtn.setOnClickListener {
            visible = false
            binding.editFriend.visibility = View.GONE
            binding.cancelBtn.visibility = View.GONE
            binding.searchFriendBtn.text = "Add new friend"
            binding.editFriend.text.clear()
        }

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("friend").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                friendsList?.clear()
                for (postSnapshot in snapshot.children){
                    val currentUsernick = postSnapshot.getValue(User::class.java)
                    friendsList.add(User(currentUsernick!!.nickname))
                }
                if(friendsList!!.size == 0){
                    binding.icon.visibility = View.VISIBLE
                    binding.noFriends.visibility = View.VISIBLE
                }
                adapter.setOnItemClickListener(object :UserAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        communicator.passNickname(friendsList[position].nickname)
                        (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flWrapper, (activity as MainActivity).getFriendFragment())
                            commit()
                        }
                    }
                })
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Friends.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        return view
    }
}

