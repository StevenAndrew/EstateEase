package com.example.propertymanagementapp.mainUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanagementapp.LoginActivities.BaseActivity
import com.example.propertymanagementapp.LoginActivities.IntroActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.adapters.PropertyItemsAdapter
import com.example.propertymanagementapp.data.Property
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.example.propertymanagementapp.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val UPDATE_PROPERTYLIST_REQUEST_CODE : Int = 44
    }

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setupActionBar()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)
        FirestoreClass().loadUserData(this, true)

        findViewById<FloatingActionButton>(R.id.fab_add_property).setOnClickListener {
            val intent = Intent(this, CreatePropertyActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, UPDATE_PROPERTYLIST_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(propertyList: ArrayList<Property>){
        hideProgressDialog()

        if (propertyList.size>0){
            //if there are properties in the database, set list to visible and remove the no properties available text
            findViewById<RecyclerView>(R.id.rv_property_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_properties_available).visibility = View.GONE

            findViewById<RecyclerView>(R.id.rv_property_list).layoutManager = LinearLayoutManager(this)
            findViewById<RecyclerView>(R.id.rv_property_list).setHasFixedSize(true)

            val adapter = PropertyItemsAdapter(this, propertyList)
            findViewById<RecyclerView>(R.id.rv_property_list).adapter = adapter

            adapter.setOnClickListener(object: PropertyItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Property) {
                    val intent = Intent(this@MainActivity, PropertyViewActivity::class.java)
                    intent.putExtra("id", model.id)
                    startActivity(intent)
                }
            })
        }else{
            findViewById<RecyclerView>(R.id.rv_property_list).visibility = View.GONE
            findViewById<TextView>(R.id.tv_no_properties_available).visibility = View.VISIBLE
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(findViewById(R.id.toolbar_main_activity))
        findViewById<Toolbar>(R.id.toolbar_main_activity).setNavigationIcon(R.drawable.ic_icon_navigation_menu)
        findViewById<Toolbar>(R.id.toolbar_main_activity).setNavigationOnClickListener {
            //Toggle Drawer
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(GravityCompat.START)){
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        }else{
            findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(GravityCompat.START)){
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if (resultCode == RESULT_OK && requestCode == UPDATE_PROPERTYLIST_REQUEST_CODE){
            FirestoreClass().getAllPropertyList(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user:User, readPropertyList: Boolean){
        mUserName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<CircleImageView>(R.id.nav_user_image))
        findViewById<TextView>(R.id.tv_username).text = user.name

        if (readPropertyList){
            showProgressDialog()
            FirestoreClass().getAllPropertyList(this)
        }
    }
}
