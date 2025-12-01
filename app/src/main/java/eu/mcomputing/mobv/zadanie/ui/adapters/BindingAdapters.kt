package eu.mcomputing.mobv.zadanie.ui.adapters

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import eu.mcomputing.mobv.zadanie.R

@BindingAdapter("imageUrl")
fun loadImage(view: ShapeableImageView, url: String?) {
    val placeholder = R.drawable.profile_avatar_placeholder

    if (!url.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(url)
            .apply(RequestOptions().centerCrop().placeholder(placeholder).error(placeholder))
            .into(view)
    } else {
        view.setImageResource(placeholder)
    }
}