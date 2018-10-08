package com.example.vaibhavi.furnitureapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.gridblock.view.*

class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        supportActionBar?.title = "Home Page"
        verifyIfUserIsLoggedIn()


        fetchFurnitureType()

    }
    private fun verifyIfUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.signout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navbarmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun fetchFurnitureType(){
        val ref = FirebaseDatabase.getInstance().getReference("/furnitureGrid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            //gets called when all of the data is being retrieved
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("HomePage",it.toString())

                    val type1 = it.getValue<FurnitureModel>(FurnitureModel::class.java)

                    if(type1 != null){
                        Log.d("HomePage", type1.toString())
                        adapter.add(FurnitureAdapter(type1))
                    }
                }
                recyclerview.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


}


class FurnitureAdapter(private val type1: FurnitureModel): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.type.text = type1.type
        Picasso.get().load(type1.image).into(viewHolder.itemView.furnitureimage)
    }

    override fun getLayout() : Int{
        return R.layout.gridblock
    }
}

//class FurnitureModel(val type: String, val image: String)

data class FurnitureModel(val type: String = "", val image: String = "")

