package com.mrmannwood.hexlauncher.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class Adapter<T>(
    private val context: Context,
    private val idFunc: ((T) -> Long)? = null,
    private val viewId: Int? = null,
    private val viewFunc: ((T) -> Int)? = null,
    private val bindFunc: (ViewDataBinding, T) -> Unit
): RecyclerView.Adapter<Adapter.Holder>() {

    init {
        if (viewId == null && viewFunc == null) {
            throw IllegalArgumentException("Must provide either a ViewId or ViewFunc")
        }
    }

    private var data: List<T>? = null

    fun setData(data: List<T>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun getItem(position: Int) : T? = data?.get(position)

    override fun getItemCount(): Int = data?.size ?: 0

    override fun getItemId(position: Int): Long {
        return idFunc?.invoke(data!![0]) ?: super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return viewFunc?.invoke(data!![position]) ?: viewId!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(context),
                viewType,
                parent,
                false)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        bindFunc(holder.vdb, data!![position])
    }

    class Holder(val vdb: ViewDataBinding): RecyclerView.ViewHolder(vdb.root)
}