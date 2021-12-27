package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ListDataAdapterBinding
import com.example.myapplication.room.DataEntity

class ListDataAdapter(private val dataList: ArrayList<DataEntity>) : RecyclerView.Adapter<ListDataAdapter.ViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            ListDataAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList.get(position)
        holder.bind(data)

        holder.itemView.setOnLongClickListener { onItemClickCallback.onLongClicked(data)
        true
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder (private var binding: ListDataAdapterBinding) : RecyclerView.ViewHolder(binding.root) {
        internal fun bind(dataList: DataEntity) = with(binding) {
            tvTitle.text = dataList.title
            tvTime.text = dataList.time
            tvData.text = dataList.data
        }
    }

    fun setData(data: List<DataEntity>){
        dataList.clear()
        dataList.addAll(data)
    }

    interface OnItemClickCallback {
        fun onLongClicked (dataList: DataEntity?)
    }
}