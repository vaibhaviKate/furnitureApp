package com.example.vaibhavi.furnitureapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_sofas.*
import kotlinx.android.synthetic.main.activity_sofas.view.*
import kotlinx.android.synthetic.main.cart_sofa.view.*

class Sofas : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sofas)

        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver, IntentFilter("message"))

        supportActionBar?.title = "Sofas"

        fetchFurnitureType()



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
        var countItems: Int = 0
        val ref = FirebaseDatabase.getInstance().getReference("/furnitureDataset/Sofas")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            //gets called when all of the data is being retrieved
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {

                    Log.d("Sofas",it.toString())

                    val type = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.type
                    val price = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.price
                    val image = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.image



                        Log.d("Sofas", type)
                        adapter.add(FurnitureDetailsAdapter(type, price, image, this@Sofas, ++countItems))
                        Log.d("Sofas", this@Sofas.toString())
                        Log.d("Sofas", "post add $type")

                }
                recycler_cart.adapter = adapter

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }



    var currentTotal = 0
    val broadCastReceiver = object : BroadcastReceiver() {
        @SuppressLint("WrongViewCast")
        override fun onReceive(contxt: Context?, intent: Intent?) {

            val furniturePrice = intent!!.getIntExtra("furniturePrice", -1)
            val select = intent.getBooleanExtra("select", false)
            val deselect = intent.getBooleanExtra("deselect", false)
            val currentValInt = intent.getIntExtra("currentValInt", -1)
            val increase = intent.getBooleanExtra("increase", false)
            val decrease = intent.getBooleanExtra("decrease", false)
            val finalCount = intent.getIntExtra("finalCount", -1)


            if(select){
                currentTotal += furniturePrice
            }

            if(increase){
                currentTotal += furniturePrice
            }

            if(decrease){
                if (currentValInt > 1 && currentTotal > 0){
                    currentTotal -= furniturePrice
                }
            }

            if(deselect){
                currentTotal -= currentValInt*furniturePrice
            }

            total.text = currentTotal.toString()

            val submitOrder = findViewById<View>(R.id.submit_order) as Button
            
        }
    }




}


class FurnitureDetailsAdapter(private val type: String, private val price: Int, private val image: String, private val context: Context
                                , private var countItems: Int): Item<ViewHolder>(){


    override fun bind(viewHolder: ViewHolder, position: Int) {

        var currentVal : Int? = 0
        var select = false
        var finalCount = 0
        viewHolder.itemView.cartfurniturename.text = type
        Picasso.get().load(image).into(viewHolder.itemView.cartfurnitureimage)
        viewHolder.itemView.cartItemPrice.text = price.toString()

        //viewHolder.itemView.cartCheckbox.isSelected

        viewHolder.itemView.buttonIncrease.setOnClickListener {

            if(select){
                currentVal = currentVal!! + 1
                viewHolder.itemView.count.text = currentVal.toString()

                val furniturePrice = price
                val increase = true
                val intent = Intent("message")

                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("increase", increase)

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }


        }
        viewHolder.itemView.buttonDecrease.setOnClickListener {

            if(select){
                val currentValInt = currentVal
                val furniturePrice = price
                val decrease = true
                val intent = Intent("message")

                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("decrease", decrease)
                intent.putExtra("currentValInt", currentValInt)

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                if(currentVal!! > 1){
                    currentVal = currentVal!! - 1
                    viewHolder.itemView.count.text = currentVal.toString()
                }
            }

        }
        viewHolder.itemView.cartCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked){
                finalCount += 1
                currentVal = 1
                viewHolder.itemView.count.text = currentVal.toString()
                Log.d("Sofas", "currentval $currentVal")
                val furniturePrice = price
                select = true
                val intent = Intent("message")

                intent.putExtra("select", select)
                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("finalCount", finalCount)

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

            if(!isChecked){
                finalCount -= 1
                select = false
                val currentValInt = currentVal
                if(currentVal!! > 0){
                    currentVal = 0
                }
                viewHolder.itemView.count.text = currentVal.toString()

                val furniturePrice = price
                val deselect = true
                val intent = Intent("message")

                intent.putExtra("deselect", deselect)
                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("currentValInt", currentValInt)
                intent.putExtra("finalCount", finalCount)

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

            }
        }
        viewHolder.itemView.count.text = "0"

        viewHolder.itemView.cart_sofa.setOnClickListener {

            Log.d("Sofas", position.toString())

            val furnitureName = type
            val furniturePrice= price
            val furnitureImage = image

            Log.d("Sofas image", furnitureImage)

            Log.d("Sofas", "$furnitureName $furniturePrice")
            val intent = Intent(context, SofasDetails::class.java)

            intent.putExtra("furnitureName", furnitureName)
            intent.putExtra("furniturePrice", furniturePrice)
            intent.putExtra("furnitureImage", furnitureImage)
            context.startActivity(intent)

        }

    }

    override fun getLayout() : Int{
        return R.layout.cart_sofa
    }
}

//class FurnitureModel(val type: String, val image: String)

data class FurnitureDetails(val type: String = "", val price: Int = 0, val image: String = "" )

data class OrderList(val type: String = "", val price: Int = 0, val image: String = "", val email: String = "", val quantity: Int = 0)