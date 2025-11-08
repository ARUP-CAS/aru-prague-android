package cz.visualio.archeologie.androidApp.adapters

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder

class ViewPager2Adapter<T: Any>(
    fragment: Fragment,
    private val createFragment: (T) -> Fragment,
    private val onItemClick: ((T)->Boolean)? = null,
) : FragmentStateAdapter(fragment) {

    private var data: List<T> = emptyList()

    fun update(newData: List<T>) {
        if (data !== newData) {
            data = newData
            DiffCallback(data, newData)
                .let(DiffUtil::calculateDiff)
                .dispatchUpdatesTo(this)
        }
    }

    override fun getItemCount(): Int = data.size
    override fun createFragment(position: Int): Fragment = createFragment(data[position])

    fun getItem(position: Int) = data.getOrNull(position)
    fun <K>getPositionById(id: K, getId: (T)->K) = data.indexOfFirst { getId(it) == id }


    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        val item = getItem(position) ?: return

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
}