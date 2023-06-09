package com.example.bookshelf.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.bookshelf.MainActivity
import com.example.bookshelf.databinding.FragmentBookDetailsBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BookDetails : Fragment() {

    var title: String? = null; var subtitle: String? = null; var publisher: String? = null; var publishedDate: String? = null
    var description: String? = null; var thumbnail: String? = null; var previewLink:  String? = null; var infoLink: String? = null
    var buyLink: String? = null; var pageCount: String? = null; private var authors: String? = null; var bid: String? = null; var readDate: String? = null
    private lateinit var binding: FragmentBookDetailsBinding
    var shelf: String? = null
    private var stars: String = "noStars"
    private lateinit var mDBRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var visible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

     override fun onCreateView(
         inflater: LayoutInflater, container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? {
         super.onCreate(savedInstanceState)
         binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
         val view: View = binding.getRoot()

         readDate = "noReadDate"
         binding.ratingBar.visibility = View.GONE
         binding.calendar.visibility = View.GONE
         binding.calendarBtn.visibility = View.GONE

         binding.ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
             stars = binding.ratingBar.rating.toString()
         }

         binding.calendarBtn.setOnClickListener {
             if(!visible){
                 visible = true
                 binding.calendar.visibility = View.VISIBLE
                 binding.calendarBtn.text = "Remove Read Date"
             }
             else{
                 visible = false
                 binding.calendar.visibility = View.GONE
                 readDate = "noReadDate"
                 binding.calendarBtn.text = "Add Read Date"
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
                 }
             })
         }

         binding.read.setOnCheckedChangeListener { buttonView, isChecked ->
             if (isChecked) {
                 binding.ratingBar.visibility = View.VISIBLE
                 binding.calendarBtn.visibility = View.VISIBLE
             }
         }
         binding.currentlyReading.setOnCheckedChangeListener { buttonView, isChecked ->
             if (isChecked) {
                 binding.ratingBar.visibility = View.GONE
                 binding.calendarBtn.visibility = View.GONE
                 binding.calendar.visibility = View.GONE
                 binding.ratingBar.rating = 0F
             }
         }
         binding.wantToRead.setOnCheckedChangeListener { buttonView, isChecked ->
             if (isChecked) {
                 binding.ratingBar.visibility = View.GONE
                 binding.calendarBtn.visibility = View.GONE
                 binding.calendar.visibility = View.GONE
                 binding.ratingBar.rating = 0F
             }
         }
         addListenerOnButton(view)

         title = arguments?.getString("title")
         subtitle = arguments?.getString("subtitle")
         publisher= arguments?.getString("publisher")
         publishedDate= arguments?.getString("publishedDate")
         description= arguments?.getString("description")
         thumbnail= arguments?.getString("thumbnail")
         previewLink= arguments?.getString("previewLink")
         infoLink= arguments?.getString("infoLink")
         buyLink= arguments?.getString("buyLink")
         pageCount= arguments?.getString("pageCount")
         authors= arguments?.getString("authors")
         bid = arguments?.getString("bid")
         shelf = arguments?.getString("shelf")

         if(shelf == "Currently Reading")
             binding.currentlyReading.visibility = View.GONE
         else if(shelf == "Want To Read")
             binding.wantToRead.visibility = View.GONE

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
         if(authors != "[]" && authors != "[, ]" && authors != "[, , ]" && authors != "[, , , ]" && authors != "[, , , , ]")
             binding.idTVAuthors.setText("by " + authors?.replace("[", "")?.replace("]", ""))
         else
             binding.idTVAuthors.visibility = View.GONE
         Picasso.get().load(thumbnail?.replace("http", "https")).placeholder(com.example.bookshelf.R.drawable.library).noFade().into(binding.idIVbook)

         (activity as MainActivity?)!!.setActionBarTitle(title)
         return view
    }

    fun addListenerOnButton(view: View) {
        var radioGroup: RadioGroup = binding.radioGroup
        var savebtn: Button = binding.saveBtn

        savebtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val selectedId: Int = radioGroup.getCheckedRadioButtonId()

                if(selectedId != -1){
                    var radioButton = radioGroup.findViewById(selectedId) as RadioButton
                    shelf = radioButton.getText().toString()
                } else
                    shelf = "noShelf"
                if(selectedId == 1 || selectedId == 2)
                    stars = "noStars"
                if(checkDate(readDate)){
                    if(binding.ratingBar.rating == 0F)
                        addBookToDatabase(title, subtitle, publisher, publishedDate, description, thumbnail, pageCount, authors, bid, shelf, "noStars", mAuth.currentUser?.uid!!, readDate)
                    else
                        addBookToDatabase(title, subtitle, publisher, publishedDate, description, thumbnail, pageCount, authors, bid, shelf, stars, mAuth.currentUser?.uid!!, readDate)
                    Toast.makeText(this@BookDetails.requireContext(), "$title saved in $shelf", Toast.LENGTH_LONG)
                        .show()
                    (activity as MainActivity).supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    (activity as MainActivity).makeCurrentFragment((activity as MainActivity).getmyBooksFragment())
                    (activity as MainActivity).selectMyBooks()
                } else
                    Toast.makeText(this@BookDetails.requireContext(), "Insert a valid date!", Toast.LENGTH_SHORT)
                        .show()
            }
        })
    }

    override fun onStart(){
        super.onStart()
        binding.ratingBar.rating = 0F
    }

    private fun addBookToDatabase(
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