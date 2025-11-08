package cz.visualio.archeologie.androidApp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect



@ExperimentalCoroutinesApi
class RecyclerAdapter<in ENTITY, VH>(
    coroutineScope: CoroutineScope,
    private val layout: Int,
    private val viewHolderFactory: (View) -> VH,
    private val onDataChanged: (() -> Unit)? = null,
    private val onItemClick: ((View, ENTITY, Int) -> Unit)? = null,
    private val onItemLongClick: ((View, ENTITY, Int) -> Unit)? = null,
    private val diffCallbackFactory: (old: List<ENTITY>, new: List<ENTITY>) -> DiffUtil.Callback = ::DiffCallback
) : RecyclerView.Adapter<VH>()
        where VH : RecyclerView.ViewHolder, VH : ViewHolder<ENTITY> {

    init {
        coroutineScope.launch {
            store.collect { (old, new) ->
                diffCallbackFactory(old, new)
                    .let(DiffUtil::calculateDiff)
                    .let {
                        updateList(it)
                        onDataChanged?.invoke()
                    }
            }
        }
    }

    private val store = MutableStateFlow(emptyList<ENTITY>() to emptyList<ENTITY>())
    private val data
        get() = store.value.second

    fun update(newData: List<ENTITY>) {
        store.value = data to newData
    }

    private suspend fun updateList(result: DiffUtil.DiffResult) {
        withContext(Dispatchers.Main) {
            result.dispatchUpdatesTo(this@RecyclerAdapter)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        parent.context
            .let(LayoutInflater::from)
            .inflate(layout, parent, false)
            .let(viewHolderFactory)


    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.apply {
            val model = data[position]

            bindModel(model)

            if (onItemClick != null)
                itemView.setOnClickListener {
                    onItemClick.invoke(it, model, position)
                }
            else itemView.setOnClickListener(null)

            if (onItemLongClick != null)
                itemView.setOnLongClickListener {
                    onItemLongClick.invoke(it, model, position)
                    true
                }
            else itemView.setOnLongClickListener(null)
        }
    }
}

internal class DiffCallback<in T>(private val oldList: List<T>, private val newList: List<T>) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}

interface ViewHolder<in T> {
    fun bindModel(model: T)
}