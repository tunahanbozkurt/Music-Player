package com.tunahan.musicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tunahan.musicplayer.R
import com.tunahan.musicplayer.databinding.GridListBinding
import com.tunahan.musicplayer.model.Musics


class RecyclerViewAdapter(private val ModelList:List<Musics>,private val listener: ClickListener) :RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){



    class ViewHolder(var binding:GridListBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<GridListBinding>(inflater,R.layout.grid_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {

        holder.binding.musics = ModelList[position]


        holder.binding.constraint.setOnClickListener{
            listener.buttonClicked(holder.binding.constraint,ModelList[position],position)
        }
    }

    override fun getItemCount(): Int {
        return ModelList.size
    }

}