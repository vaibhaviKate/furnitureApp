package com.example.vaibhavi.furnitureapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_sofas.*
import kotlinx.android.synthetic.main.cart_sofa.view.*

class SofasDetails : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sofas_details)

        val intent = intent
        val furnitureName = intent!!.getStringExtra("furnitureName")
        val furniturePrice = intent!!.getIntExtra("furniturePrice", -1)
        val furnitureImage = intent!!.getStringExtra("furnitureImage")
        Log.d("Details", "$furnitureImage")

        supportActionBar?.title = furnitureName
        //LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver, IntentFilter("message"))

        val itemName = findViewById<View>(R.id.detailItemName) as TextView
        val itemImage = findViewById<View>(R.id.detailItemImage) as ImageView
        val itemPrice = findViewById<View>(R.id.detailItemPrice) as TextView

        fetchDetails()

        itemPrice.text = furniturePrice.toString()
        itemName.text = furnitureName
        Picasso.get().load(furnitureImage).into(itemImage)

    }

    private fun fetchDetails(){
        val intent = intent
        val furnitureName = intent!!.getStringExtra("furnitureName")


        val ref = FirebaseDatabase.getInstance().getReference("/furnitureDataset/Sofas")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            //gets called when all of the data is being retrieved
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {

                    if(furnitureName == it.getValue(FurnitureDesc::class.java)!!.type){
                        val desc = it.getValue(FurnitureDesc::class.java)!!.description.toLong()

                        val itemDesc = findViewById<View>(R.id.detailItemDetails) as TextView
                        itemDesc.text = desc.toString()
                    }

                }


            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


}

data class FurnitureDesc(val type: String = "", val price: String = "", val image: String = "",  val description: String = "")