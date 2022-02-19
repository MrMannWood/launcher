package com.mrmannwood.hexlauncher.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

@MainThread
class Adapter<T : Any>(
        private val context: Context,
        private val order: Array<KClass<out T>>,
        private val idFunc: ((T) -> Long)? = null,
        private val viewFunc: ((T) -> Int),
        private val bindFunc: (ViewDataBinding, T) -> Unit
): RecyclerView.Adapter<Adapter.Holder>() {

    init {
        if (idFunc != null) {
            setHasStableIds(true)
        }
    }

    private val data: MutableList<List<T>> = (order.indices).map { emptyList<T>() }.toMutableList()
    private var expandedData : List<T> = emptyList()

    fun setData(clazz: KClass<out T>, data: List<T>) {
        this.data[order.indexOf(clazz)] = data

        expandedData = this.data.flatten()
        notifyDataSetChanged()
    }

    fun getItem(position: Int) : T = expandedData[position]

    override fun getItemCount(): Int = expandedData.size

    override fun getItemId(position: Int): Long {
        return idFunc?.invoke(expandedData[position]) ?: super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return viewFunc(expandedData[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                viewType,
                parent,
                false /* attachToParent */)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        bindFunc(holder.vdb, expandedData[position])
    }

    class Holder(val vdb: ViewDataBinding): RecyclerView.ViewHolder(vdb.root)

    class IdGenerator<T : Any>(defs: List<Pair<KClass<out T>, (T) -> Any>>) {

        private var id = 0L
        private val map : Map<KClass<out T>, Pair<(T) -> Any, MutableMap<Any, Long>>> =
                defs.map { (clazz, func) ->
                    clazz to (func to mutableMapOf<Any, Long>())
                }.toMap()

        fun genId(result : T) : Long {
            val (keyGet, idMap) = map.getOrElse(result::class, { throw IllegalArgumentException("Unknown type ${result::class}") })
            val key = keyGet(result)
            return getOrCreateId(key, idMap)
        }

        private fun <T> getOrCreateId(key: T, map : MutableMap<T, Long>) : Long {
            var id = map[key]
            if (id == null) {
                id = genNextId()
                map[key] = id
            }
            return id
        }

        private fun genNextId() : Long = id++
    }
}