package ie.wit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.fragments.ReportAllFragment
import ie.wit.fragments.ReportFragment
import ie.wit.main.WitParkingApp
import ie.wit.models.SubscriptionModel
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_subscription.view.*
import kotlinx.android.synthetic.main.card_subscription.view.*

interface SubscriptionListener {
    fun onSubscriptionClick(subscription: SubscriptionModel)
}

class SubscriptionAdapter(
    options: FirebaseRecyclerOptions<SubscriptionModel>,
    private val listener: SubscriptionListener?
) : FirebaseRecyclerAdapter<SubscriptionModel,
        SubscriptionAdapter.SubscriptionViewHolder>(options) {

    class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(subscription: SubscriptionModel, listener: SubscriptionListener) {
            with(subscription) {
                itemView.tag = subscription
                itemView.paymentamount.text = subscription.amount.toString()
                itemView.paymentmethod.text = subscription.paymenttype

                if (listener is ReportAllFragment)
                    ; // Do Nothing, Don't Allow 'Clickable' Rows
                else
                    itemView.setOnClickListener { listener.onSubscriptionClick(subscription) }

                if (subscription.isfavourite) itemView.imagefavourite.setImageResource(android.R.drawable.star_big_on)

                if (!subscription.profilepic.isEmpty()) {
                    Picasso.get().load(subscription.profilepic.toUri())
                        //.resize(180, 180)
                        .transform(CropCircleTransformation())
                        .into(itemView.imageIcon)
                } else
                    itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_homer_round)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {

        return SubscriptionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_subscription, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: SubscriptionViewHolder,
        position: Int,
        model: SubscriptionModel
    ) {
        holder.bind(model, listener!!)
    }

    override fun onDataChanged() {
        // Called each time there is a new data snapshot. You may want to use this method
        // to hide a loading spinner or check for the "no documents" state and update your UI.
        // ...
    }


}
