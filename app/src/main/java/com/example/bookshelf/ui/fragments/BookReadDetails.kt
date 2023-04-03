package com.example.bookshelf.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.bookshelf.Communicator
import com.example.bookshelf.MainActivity
import com.example.bookshelf.databinding.BookReadDetailsBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BookReadDetails : Fragment() {
    var title: String? = null; var subtitle: String? = null; var publisher: String? = null; var publishedDate: String? = null; var readDate: String? = "noReadDate"
    var description: String? = null; var thumbnail: String? = null; var previewLink:  String? = null; var infoLink: String? = null
    var buyLink: String? = null; var pageCount: String? = null; private var authors: String? = null; var bid: String? = null; var shelf: String? = null
    private lateinit var binding: BookReadDetailsBinding
    private var stars: String? = null
    private var newValue: String? = "no"
    private lateinit var mDBRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var visible: Boolean = false
    private lateinit var communicator: Communicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

     override fun onCreateView(
         inflater: LayoutInflater, container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? {
         super.onCreate(savedInstanceState)
         binding = BookReadDetailsBinding.inflate(inflater, container, false)
         val view: View = binding.getRoot()
         communicator = activity as Communicator
         shelf = arguments?.getString("shelf")
         binding.ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
             newValue = binding.ratingBar.rating.toString()
         }

         binding.calendarBtn.setOnClickListener {
             if(!visible){
                 visible = true
                 binding.calendar.visibility = View.VISIBLE
             }
             else{
                 visible = false
                 binding.calendar.visibility = View.GONE
                 if(arguments?.getString("readDate")!= "noReadDate")
                     readDate = arguments?.getString("readDate")
                 else
                     readDate = "noReadDate"
             }
             binding.calendar.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
                 override fun onSelectedDayChange(p0: CalendarView, p1: Int, p2: Int, p3: Int) {
                     var day = p3.toString()
                     var month = (p2+1).toString()
                     if(p3<=9)
                         day = "0" + p3
                     if(p2 <9)
                         month = "0" + (p2 + 1).toString()
                     readDate = "$day/$month/$p1"
                     binding.readDateTV.text = "READ DATE: $readDate"
                 }
             })
         }

         addListenerOnButton()
         title = arguments?.getString("title")
         subtitle = arguments?.getString("subtitle")
         publisher = arguments?.getString("publisher")
         publishedDate = arguments?.getString("publishedDate")
         description = arguments?.getString("description")
         thumbnail = arguments?.getString("thumbnail")
         pageCount = arguments?.getString("pageCount")
         authors = arguments?.getString("authors")
         bid = arguments?.getString("bid")
         stars = arguments?.getString("stars")
         readDate = arguments?.getString("readDate")

         binding.changeShelfBtn.setOnClickListener(object : View.OnClickListener {
             override fun onClick(v: View?) {
                 communicator.passSearchBook(
                     title,subtitle, authors,
                     publisher,  publishedDate,  description,
                     pageCount,  thumbnail, shelf,
                     bid, stars, readDate)
             }
         })

         if(readDate != "noReadDate")
             binding.readDateTV.text = "Read date: $readDate"
         else
             binding.readDateTV.text = "Read date: no read date available"

         binding.idTVTitle.setText(title)
         if(subtitle != "" && subtitle != null)
             binding.idTVSubTitle.setText(subtitle)
         else
             binding.idTVSubTitle.visibility = View.GONE
         if(publisher != "" && publisher != null)
             binding.idTVpublisher.setText("Publisher: " + publisher)
         else
             binding.idTVpublisher.visibility = View.GONE
         if(publishedDate != "" && publishedDate != null)
             binding.idTVPublishDate.setText("Published on: " + publishedDate)
         else
             binding.idTVPublishDate.visibility = View.GONE
         if(description != "" && description != null)
             binding.idTVDescription.setText("Description: \n" + description)
         else
             binding.idTVDescription.visibility = View.GONE
         if(pageCount != "" && pageCount != null)
             binding.idTVNoOfPages.setText("No Of Pages: " + pageCount)
         else
             binding.idTVNoOfPages.visibility = View.GONE
         if(authors != "[]" && authors != "[, ]" && authors != "[, , ]" && authors != "[, , , ]" && authors != "[, , , , ]" && authors != null)
             binding.idTVAuthors.setText("by " + authors?.replace("[", "")?.replace("]", ""))
         else
             binding.idTVAuthors.visibility = View.GONE
         if(stars != "noStars")
             binding.ratingBar.rating = stars?.toFloat()!!
         else
             binding.ratingBar.rating = 0F
         Picasso.get().load(thumbnail?.replace("http", "https")).placeholder(com.example.bookshelf.R.drawable.library).noFade().into(binding.idIVbook)

         (activity as MainActivity?)!!.setActionBarTitle(title)
         if(shelf == "Read"){
             binding.ratingBar.visibility = View.VISIBLE
             binding.line.visibility = View.VISIBLE
             binding.yourRating.visibility = View.VISIBLE
             binding.saveBtn.visibility = View.VISIBLE
             binding.calendar.visibility = View.GONE
             binding.changeShelfBtn.visibility = View.GONE

         }
         else if(shelf == "Currently Reading"){
             binding.ratingBar.visibility = View.GONE
             binding.line.visibility = View.GONE
             binding.yourRating.visibility = View.GONE
             binding.saveBtn.visibility = View.GONE
             binding.calendar.visibility = View.GONE
             binding.calendarBtn.visibility = View.GONE
             binding.readDateTV.visibility = View.GONE
         }
         else{
             binding.ratingBar.visibility = View.GONE
             binding.line.visibility = View.GONE
             binding.yourRating.visibility = View.GONE
             binding.saveBtn.visibility = View.GONE
             binding.calendar.visibility = View.GONE
             binding.calendarBtn.visibility = View.GONE
             binding.readDateTV.visibility = View.GONE
         }

         return view
    }

    override fun onStart(){
        super.onStart()
        if(stars != "noStars")
            binding.ratingBar.rating = stars?.toFloat()!!
        else
            binding.ratingBar.rating = 0F
    }

    fun addListenerOnButton() {
        binding.removeBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                removeBookFromDatabase()
                Toast.makeText(this@BookReadDetails.requireContext(), "$title removed from $shelf", Toast.LENGTH_LONG)
                    .show()
                (activity as MainActivity).supportFragmentManager.popBackStackImmediate()
            }
        })

        binding.saveBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(binding.ratingBar.rating.toString() == "0.0")
                    stars = "noStars"
                if(checkDate(readDate)){
                    if(newValue == "no")
                        saveBookToDatabase(title, subtitle, publisher, publishedDate, description, thumbnail, pageCount, authors, bid, shelf, stars, mAuth.currentUser?.uid!!, readDate)
                    else
                        saveBookToDatabase(title, subtitle, publisher, publishedDate, description, thumbnail, pageCount, authors, bid, shelf, newValue, mAuth.currentUser?.uid!!, readDate)
                    Toast.makeText(this@BookReadDetails.requireContext(), "Changes saved correctly", Toast.LENGTH_SHORT)
                        .show()
                    (activity as MainActivity).supportFragmentManager.popBackStackImmediate()
                } else
                    Toast.makeText(this@BookReadDetails.requireContext(), "Insert a valid date!", Toast.LENGTH_SHORT)
                        .show()
            }
        })
    }

    private fun removeBookFromDatabase(){
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser?.uid!!).child("book").child(bid!!).removeValue()
    }

    private fun saveBookToDatabase(
        title: String?,
        subtitle: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        thumbnail: String?,
        pageCount: String?,
        authors: String?,
        bid: String?,
        shelf: String?,
        stars: String?,
        uid: String?,
        readDate: String?
    ) {
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(uid!!).child("book").child(bid!!).setValue(
            BookInfo(title, subtitle, authors, publisher, publishedDate, description, pageCount, thumbnail, bid, shelf, stars, uid, readDate)
        )
    }

    private fun checkDate(readDate: String?): Boolean{
        if(readDate == "noReadDate")
            return true
        if(LocalDate.parse(readDate,  DateTimeFormatter.ofPattern("dd/MM/yyyy")).isAfter(LocalDate.now()))
            return false
        return true
    }
}