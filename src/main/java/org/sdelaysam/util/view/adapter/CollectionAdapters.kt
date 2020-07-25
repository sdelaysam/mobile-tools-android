package org.sdelaysam.util.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.functions.Consumer
import org.sdelaysam.util.rx.UiDisposable

/**
 * Created on 7/25/20.
 * @author sdelaysam
 */

interface Identifiable {
    val identity: Int
    val hash: Int
}

fun <T : Identifiable> createDiffCallback() = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem.identity == newItem.identity
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem.hash == newItem.hash
}

interface DataProvider

interface ViewHolderDataProvider: DataProvider, Identifiable {
    val identifier: ViewHolderIdentifier
}

interface ViewHolderIdentifier


abstract class DataViewHolder(layoutId: Int, parent: ViewGroup): RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)) {

    interface Factory {
        val identifier: ViewHolderIdentifier
        fun create(parent: ViewGroup): DataViewHolder
    }

    abstract fun bind(data: DataProvider)

    open fun onAttached() {}

    open fun onDetached() {}
}


class TableAdapter: RecyclerView.Adapter<DataViewHolder>() {

    private val differ = AsyncListDiffer(this, createDiffCallback<ViewHolderDataProvider>())

    private val factories = mutableMapOf<ViewHolderIdentifier, DataViewHolder.Factory>()

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    fun register(factory: DataViewHolder.Factory) {
        factories[factory.identifier] = factory
    }

    fun reload(dataProviders: List<ViewHolderDataProvider>) {
        differ.submitList(dataProviders)
    }

    fun dataProviders(): Consumer<List<ViewHolderDataProvider>> {
        return Consumer { reload(it) }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val dataProvider = differ.currentList[viewType]
        val factory = factories[dataProvider.identifier]
            ?: throw IllegalArgumentException("No factory registered for identifier: ${dataProvider.identifier}")
        return factory.create(parent)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun onViewAttachedToWindow(holder: DataViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttached()
    }

    override fun onViewDetachedFromWindow(holder: DataViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetached()
    }

    override fun onViewRecycled(holder: DataViewHolder) {
        super.onViewRecycled(holder)
        (holder as? UiDisposable)?.dispose()
    }
}