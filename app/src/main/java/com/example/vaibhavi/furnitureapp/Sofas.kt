package com.example.vaibhavi.furnitureapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_sofas.*
import kotlinx.android.synthetic.main.cart_sofa.view.*
import java.lang.reflect.Array
import java.util.ArrayList

class Sofas : AppCompatActivity() {

     var list = ArrayList<FurnitureDetails>()
    var list1 = ArrayList<MoreDetails>()

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sofas)


        //var adapter1: RecyclerView.Adapter<FurnitureDetailsAdapter.ViewHolder>? = null


        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver, IntentFilter("message"))

        supportActionBar?.title = "Sofas"

        Log.d("Sofas", "before data fetched.")
        fetchFurnitureType()

        Log.d("Sofas", "data fetched.")



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
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_cart)
        val ref = FirebaseDatabase.getInstance().getReference("/furnitureDataset/Sofas")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            //gets called when all of the data is being retrieved
            override fun onDataChange(p0: DataSnapshot) {
                //val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {

                    Log.d("Sofas",it.toString())

                    val details1 = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)


//                    val type = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.type
//                    val price = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.price
//                    val image = it.getValue<FurnitureDetails>(FurnitureDetails::class.java)!!.image
                     Log.d("Sofas", details1.toString())

                        list.add(details1!!)




                        Log.d("Sofas", this@Sofas.toString())
                        //Log.d("Sofas", "post add $details1")

                }
                list1 = ArrayList<MoreDetails>(list.size)
                recyclerView.setHasFixedSize(true) //every item in the recycler view will have fixed size
                recyclerView.layoutManager = LinearLayoutManager(this@Sofas) as RecyclerView.LayoutManager?
                val adapter1 = FurnitureDetailsAdapter(list, list1, this@Sofas)

                recyclerView.adapter = adapter1 //sets adapter to recycler view

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })



    }



    var currentTotal = 0
    var totalPrice: Int = 0
    val broadCastReceiver = object : BroadcastReceiver() {
        @SuppressLint("WrongViewCast")
        override fun onReceive(contxt: Context?, intent: Intent?) {

            val furniturePrice = intent!!.getIntExtra("furniturePrice", -1)
            val select = intent.getBooleanExtra("select", false)
            val deselect = intent.getBooleanExtra("deselect", false)
            val currentValInt = intent.getIntExtra("currentValInt", -1)
            val increase = intent.getBooleanExtra("increase", false)
            val decrease = intent.getBooleanExtra("decrease", false)
            val position = intent.getIntExtra("position", -1)





//
            if(deselect){
                if (currentTotal > 0) {
                    currentTotal -= currentValInt * furniturePrice

                    list1.add(position, MoreDetails(select, currentValInt,currentTotal))
                }
            }  else

            if(increase && select){
                currentTotal += furniturePrice
                totalPrice = currentValInt*furniturePrice
                list1.add(position, MoreDetails(select, currentValInt,totalPrice))
            }
               else
            if(decrease && select){
                if (currentValInt > 1 && currentTotal > 0){
                    currentTotal -= furniturePrice
                    totalPrice = currentValInt*furniturePrice
                    list1.add(position, MoreDetails(select, currentValInt,totalPrice))
                }
            }  else
            if(select){
                currentTotal += furniturePrice
                  //list1[position].totalPrice = currentTotal
                  //list1[position] =  MoreDetails(select, currentValInt,currentTotal)
                totalPrice = currentValInt*furniturePrice
                  list1.add(position, MoreDetails(select, currentValInt,totalPrice))
            }
            total.text = currentTotal.toString()

            val submitOrder = findViewById<View>(R.id.submit_order) as Button
            submitOrder.setOnClickListener {
                val user = FirebaseAuth.getInstance()
                val userid = user.currentUser
                val u = userid!!.uid
                val username = userid.email
                //UserPost userpost = new UserPost(name);

                val mDatabase1 = FirebaseDatabase.getInstance().getReference("/orders/$u").push()
                 var items = 0
                for (i in 0 until list.size) {
                    if (list1[i].selected!!) {
                        val itemName = list[i].type
                        val itemPrice = list[i].price
                        val quantity = list1[i].quantity
                        val totalprice = list1[i].totalPrice
                        val obj = OrderList(itemName, itemPrice, username!!, quantity!!, totalprice!!)
                        mDatabase1.push().setValue(obj)
                        items++
                    }
                }
                if(items == 0){
                    Toast.makeText(this@Sofas, "No items selected.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@Sofas, "Order submitted.", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }




}


class FurnitureDetailsAdapter: RecyclerView.Adapter<FurnitureDetailsAdapter.ViewHolder> {


     var list = ArrayList<FurnitureDetails>()
    private var context: Context? = null
     var list1 = ArrayList<MoreDetails>()


    constructor(list: ArrayList<FurnitureDetails>, list1: ArrayList<MoreDetails>, context: Context){
        this.list = list
        this.context = context
        this.list1 = list1

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_sofa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var select  = false
        holder.itemName.text = list[position].type
        Log.d("Sofas", list[position].type)
        holder.itemPrice.text = list[position].price.toString()
        Picasso.get().load(list[position].image).into(holder.itemImage)

        holder.itemImage.setOnClickListener {
            Log.d("Sofas", position.toString())

                        val furnitureName = list[position].type
                        val furniturePrice= list[position].price
                        val furnitureImage = list[position].image

                        Log.d("Sofas image", furnitureImage)

                        Log.d("Sofas", "$furnitureName $furniturePrice")
                        val intent = Intent(context, SofasDetails::class.java)

                        intent.putExtra("furnitureName", furnitureName)
                        intent.putExtra("furniturePrice", furniturePrice)
                        intent.putExtra("furnitureImage", furnitureImage)
                        context!!.startActivity(intent)

         }

//        holder.checkbox.isChecked = list1[position].selected!!
//
//        holder.checkbox.tag = position

        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            if(isChecked){

                Log.d("Sofas", "selected checkbox $position")

                val currentVal = holder.count.text.toString()
                var currentValInt = Integer.parseInt(currentVal)
                currentValInt += 1
                select = true
                //list1[pos].selected = select
                
                //list1[pos].quantity = currentValInt
                //list1.add(MoreDetails(select, currentValInt))
                holder.count.text = currentValInt.toString()
                Log.d("Sofas", "currentval $currentVal")
                val furniturePrice = list[position].price
                val intent = Intent("message")

                intent.putExtra("select", select)
                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("currentValInt", currentValInt)
                intent.putExtra("position", position)

                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }

            if(!isChecked){
                Log.d("Sofas", "deselected checkbox $position")

                select = false
                val currentVal = holder.count.text.toString()
                var currentValInt = Integer.parseInt(currentVal)

                val furniturePrice = list[position].price
                val deselect = true
                //list1[pos].selected = select

                val intent = Intent("message")

                intent.putExtra("deselect", deselect)
                intent.putExtra("select", select)
                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("currentValInt", currentValInt)
                intent.putExtra("position", position)

                if(currentValInt!! > 0){
                    currentValInt = 0
                }
                //list1.add(MoreDetails(select, currentValInt))
                holder.count.text = currentValInt.toString()
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

            }
        }


        holder.buttonInc.setOnClickListener {

            if(select){
                val currentVal = holder.count.text.toString()
                var currentValInt = Integer.parseInt(currentVal)

                currentValInt += 1


                holder.count.text = currentValInt.toString()

                val furniturePrice = list[position].price
                val increase = true
                val intent = Intent("message")

                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("increase", increase)
                intent.putExtra("select", select)
                intent.putExtra("currentValInt", currentValInt)
                intent.putExtra("position", position)

                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }


        }
        holder.buttonDec.setOnClickListener {

            if(select){
                val currentVal = holder.count.text.toString()
                var currentValInt = Integer.parseInt(currentVal)

                val furniturePrice = list[position].price
                val decrease = true
                val intent = Intent("message")

                intent.putExtra("furniturePrice", furniturePrice)
                intent.putExtra("decrease", decrease)
                intent.putExtra("select", select)
                intent.putExtra("currentValInt", currentValInt)
                intent.putExtra("position", position)

                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

                if(currentValInt > 1){
                    currentValInt -= 1
                    
                    
                    holder.count.text = currentValInt.toString()
                }
            }

        }
        holder.count.text = "0"

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val itemName = itemView.cartfurniturename as TextView
        val itemPrice = itemView.cartItemPrice as TextView
        val itemImage = itemView.cartfurnitureimage as ImageView
        val buttonInc = itemView.buttonIncrease as Button
        val buttonDec = itemView.buttonDecrease as Button
        val checkbox = itemView.cartCheckbox as CheckBox
        val count = itemView.count as TextView

    }

}



//class FurnitureModel(val type: String, val image: String)

data class FurnitureDetails(val type: String = "", val price: Int = 0, val image: String = "")//, var selected: Boolean?, var quantity: Int? = 0, var totalPrice: Int? = 0)

data class OrderList(val type: String = "", val price: Int = 0, val email: String = "", val quantity: Int = 0, val totalPrice: Int = 0)

data class MoreDetails(var selected: Boolean? = false, var quantity: Int? = 0, var totalPrice: Int? = 0)

//class FurnitureDetails {
//    var type: String? = null
//    var price: Int = 0
//    var image: String? = ""
//    var selected: Boolean = false
//    var quantity: Int = 0
//    var totalPrice: Int = 0
//
//    //    public allMenuMao(){
//    //        return;
//    //    }
//
//    constructor(type: String = "", price: Int? = 0, image: String = "") {
//        this.type = type
//        this.price = price!!
//        this.image = image
//    }
//
//    constructor(type: String = "", price: Int? = 0, image: String = "",  selected: Boolean?, quantity: Int? = 0, totalPrice: Int? = 0) {
//        this.type = type
//        this.price = price!!
//        this.image = image
//        this.selected = selected!!
//        this.quantity = quantity!!
//        this.totalPrice = totalPrice!!
//    }




