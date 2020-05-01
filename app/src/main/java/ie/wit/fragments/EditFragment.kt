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
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class EditFragment : Fragment(), AnkoLogger {

    lateinit var app: WitParkingApp
    lateinit var loader: AlertDialog
    lateinit var root: View
    var editSubscription: SubscriptionModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as WitParkingApp

        arguments?.let {
            editSubscription = it.getParcelable("editsubscription")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit)
        loader = createLoader(activity!!)

        root.editAmount.setText(editSubscription!!.amount.toString())
        root.editPaymenttype.setText(editSubscription!!.paymenttype)
        root.editMessage.setText(editSubscription!!.message)
        root.editCarreg.setText(editSubscription!!.carreg.toString())

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Subscription on Server...")
            updateSubscriptionData()
            updateSubscription(editSubscription!!.uid, editSubscription!!)
            updateUserSubscription(
                app.currentUser.uid,
                editSubscription!!.uid, editSubscription!!
            )
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(subscription: SubscriptionModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editsubscription", subscription)
                }
            }
    }

    fun updateSubscriptionData() {
        editSubscription!!.amount = root.editAmount.text.toString().toInt()
        editSubscription!!.message = root.editMessage.text.toString()
        editSubscription!!.carreg = root.editCarreg.text.toString().toInt()
    }

    fun updateUserSubscription(userId: String, uid: String?, subscription: SubscriptionModel) {
        app.database.child("user-subscription").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(subscription)
                        activity!!.supportFragmentManager.beginTransaction()
                            .replace(R.id.homeFrame, ReportFragment.newInstance())
                            .addToBackStack(null)
                            .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Subscription error : ${error.message}")
                    }
                })
    }

    fun updateSubscription(uid: String?, subscription: SubscriptionModel) {
        app.database.child("subscriptions").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(subscription)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Subscription error : ${error.message}")
                    }
                })
    }
}
