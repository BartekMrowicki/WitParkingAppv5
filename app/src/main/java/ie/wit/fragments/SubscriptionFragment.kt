package ie.wit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.main.WitParkingApp
import ie.wit.models.SubscriptionModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_subscription.*
import kotlinx.android.synthetic.main.fragment_subscription.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.lang.String.format
import java.util.HashMap


class SubscriptionFragment : Fragment(), AnkoLogger {

    lateinit var app: WitParkingApp
    var totalSubscribed = 0
    lateinit var loader: AlertDialog
    lateinit var eventListener: ValueEventListener
    var favourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as WitParkingApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_subscription, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_subscription)

        root.progressBar.max = 10000
        root.amountPicker.minValue = 1
        root.amountPicker.maxValue = 1000

        root.amountPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected number to paymentAmount
            root.paymentAmount.setText("$newVal")
        }
        setButtonListener(root)
        setFavouriteListener(root)
        return root;
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SubscriptionFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener(layout: View) {
        layout.subscriptionButton.setOnClickListener {
            val amount = if (layout.paymentAmount.text.isNotEmpty())
                layout.paymentAmount.text.toString().toInt() else layout.amountPicker.value
            if (totalSubscribed >= layout.progressBar.max)
                activity?.toast("Subscription Amount Exceeded!")
            else {
                val paymentmethod =
                    if (layout.paymentMethod.checkedRadioButtonId == R.id.Direct) "Direct" else "Paypal"
                writeNewSubscription(
                    SubscriptionModel(
                        paymenttype = paymentmethod, amount = amount,
                        profilepic = app.userImage.toString(),
                        isfavourite = favourite,
                        latitude = app.currentLocation.latitude,
                        longitude = app.currentLocation.longitude,
                        email = app.currentUser.email
                    )
                )
            }
        }
    }

    fun setFavouriteListener(layout: View) {
        layout.imagefavourite.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (!favourite) {
                    layout.imagefavourite.setImageResource(android.R.drawable.star_big_on)
                    favourite = true
                } else {
                    layout.imagefavourite.setImageResource(android.R.drawable.star_big_off)
                    favourite = false
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getTotalSubscribed(app.currentUser.uid)
    }

    override fun onPause() {
        super.onPause()
        if (app.currentUser.uid != null)
            app.database.child("user-parkings")
                .child(app.currentUser.uid)
                .removeEventListener(eventListener)
    }

    fun writeNewSubscription(subscription: SubscriptionModel) {
        // Create new subscritpion at /subscritpions & /subscriptions/$uid
        showLoader(loader, "Adding subscription to Firebase")
        info("Firebase DB Reference : $app.database")
        val uid = app.currentUser.uid
        val key = app.database.child("subscriptions").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        subscription.uid = key
        val subscriptionValues = subscription.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/substriptions/$key"] = subscriptionValues
        childUpdates["/user-subscritpions/$uid/$key"] = subscriptionValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }

    fun getTotalSubscribed(userId: String?) {
        eventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                info("Firebase Subscription error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                totalSubscribed = 0
                val children = snapshot.children
                children.forEach {
                    val subscription = it.getValue<SubscriptionModel>(SubscriptionModel::class.java)
                    totalSubscribed += subscription!!.amount
                }
                progressBar.progress = totalSubscribed
                totalSoFar.text = format("$ ${totalSubscribed}")
            }
        }

        app.database.child("user-subscriptions").child(userId!!)
            .addValueEventListener(eventListener)
    }
}
