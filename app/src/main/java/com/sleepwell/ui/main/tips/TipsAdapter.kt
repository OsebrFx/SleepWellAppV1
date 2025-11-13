package com.sleepwell.ui.main.tips

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sleepwell.R
import com.sleepwell.data.model.Tip
import com.sleepwell.databinding.ItemTipBinding

class TipsAdapter(private val context: Context) :
    ListAdapter<Tip, TipsAdapter.TipViewHolder>(TipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TipViewHolder(private val binding: ItemTipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: Tip) {
            // Get string resource by key
            val titleResId = context.resources.getIdentifier(tip.titleKey, "string", context.packageName)
            val descResId = context.resources.getIdentifier(tip.descriptionKey, "string", context.packageName)

            binding.tvTitle.text = if (titleResId != 0) {
                context.getString(titleResId)
            } else {
                tip.titleKey
            }

            binding.tvDescription.text = if (descResId != 0) {
                context.getString(descResId)
            } else {
                tip.descriptionKey
            }

            binding.ivIcon.setImageResource(tip.iconResId)

            // Set category color
            val categoryColor = when (tip.category) {
                "sleep_hygiene" -> context.getColor(R.color.category_sleep_hygiene)
                "lifestyle" -> context.getColor(R.color.category_lifestyle)
                "diet" -> context.getColor(R.color.category_diet)
                "exercise" -> context.getColor(R.color.category_exercise)
                "environment" -> context.getColor(R.color.category_environment)
                "relaxation" -> context.getColor(R.color.category_relaxation)
                else -> context.getColor(R.color.primary)
            }

            binding.categoryIndicator.setBackgroundColor(categoryColor)
        }
    }

    private class TipDiffCallback : DiffUtil.ItemCallback<Tip>() {
        override fun areItemsTheSame(oldItem: Tip, newItem: Tip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tip, newItem: Tip): Boolean {
            return oldItem == newItem
        }
    }
}
